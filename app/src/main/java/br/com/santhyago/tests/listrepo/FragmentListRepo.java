package br.com.santhyago.tests.listrepo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.com.santhyago.tests.listrepo.data.GitHubContract;
import br.com.santhyago.tests.listrepo.model.GitHubService;
import br.com.santhyago.tests.listrepo.model.Repo;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class FragmentListRepo extends Fragment {
	private static final String API_URL = "https://api.github.com";
	private static final String TAG = "SBG";

	private SharedPreferences pref;
	private SharedPreferences.Editor prefEditor;

	private long timeToUpdate = 50000; // 5 min.

	private ContentResolver mContentResolver;

	private static final String[] REPO_COLUMNS = {
			GitHubContract.Repo._ID,
			GitHubContract.Repo.COL_USER,
			GitHubContract.Repo.COL_NAME,
			GitHubContract.Repo.COL_DESC,
			GitHubContract.Repo.COL_STAR_COUNT,
			GitHubContract.Repo.COL_WTCH_COUNT,
			GitHubContract.Repo.COL_LANG
	};

	public static final int COL_REPO_ID = 0;
	public static final int COL_REPO_USER = 1;
	public static final int COL_REPO_NAME = 2;
	public static final int COL_REPO_DESC = 3;
	public static final int COL_REPO_STAR_COUNT = 4;
	public static final int COL_REPO_WTCH_COUNT = 5;
	public static final int COL_REPO_LANG = 6;

	private ListView mListView;
	private ListRepoAdapter mListRepoAdapter;

	private int mPosition = ListView.INVALID_POSITION;
	private static final String SELECTED_KEY = "selected_position";
	private Context mContext;
	private boolean inLandMode;

	public FragmentListRepo() {
	}

	public void setInLandMode(boolean inLandMode) {
		this.inLandMode = inLandMode;
	}

	public boolean isInLandMode() {
		return inLandMode;
	}

	public interface Callback {
		void onItemSelected(long repoID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		mContentResolver = mContext.getContentResolver();

		pref = mContext.getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);

		if (!hasUpdatedData()) {
			updateLocalData();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mListRepoAdapter = new ListRepoAdapter(getActivity(), mContentResolver.query(GitHubContract.Repo.CONTENT_URI, null, null, null, null, null), 0);

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);

		// Get a reference to the ListView, and attach this adapter to it.
		mListView = (ListView) rootView.findViewById(R.id.listview_repos);
		mListView.setAdapter(mListRepoAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
				Cursor cursor = mListRepoAdapter.getCursor();
				if (cursor != null && cursor.moveToPosition(position)) {
					((Callback)getActivity())
							.onItemSelected(cursor.getLong(COL_REPO_ID));
				}
				mPosition = position;
			}
		});

		// If there's instance state, mine it for useful information.
		// The end-goal here is that the user never knows that turning their device sideways
		// does crazy lifecycle related things.  It should feel like some stuff stretched out,
		// or magically appeared to take advantage of room, but data or place in the app was never
		// actually *lost*.
		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
			// The listview probably hasn't even been populated yet.  Actually perform the
			// swapout in onLoadFinished.
			mPosition = savedInstanceState.getInt(SELECTED_KEY);
		}

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// When tablets rotate, the currently selected list item needs to be saved.
		// When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
		// so check for that before storing.
		if (mPosition != ListView.INVALID_POSITION) {
			outState.putInt(SELECTED_KEY, mPosition);
		}
		super.onSaveInstanceState(outState);
	}

	private boolean hasUpdatedData() {
		long timeLastUpdate = pref.getLong(getString(R.string.time_last_update), 0);
		long currentlyTime = Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis();

		return (currentlyTime - timeLastUpdate) < timeToUpdate;
	}

	private void updateLocalData() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(API_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		GitHubService service = retrofit.create(GitHubService.class);

		Call<List<Repo>> call = service.listRepos("torvalds");
		call.enqueue(new retrofit.Callback<List<Repo>>() {
			@Override
			public void onResponse(Response<List<Repo>> response, Retrofit retrofit) {
				List<Repo> repos = response.body();
				ContentValues[] values = GitHubContract.Repo.buildContentValues(repos);

				int totRepo = mContentResolver.bulkInsert(GitHubContract.Repo.CONTENT_URI, values);
				Log.d(TAG, "Total: " + totRepo + " / Repo Size: " + repos.size());

				mListRepoAdapter.swapCursor(mContentResolver.query(GitHubContract.Repo.CONTENT_URI, null, null, null, null, null));
				mListRepoAdapter.notifyDataSetChanged();

				if (totRepo == repos.size()) {
					prefEditor = pref.edit();
					prefEditor.putLong(getString(R.string.time_last_update), Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis());
					prefEditor.apply();
				}

				if (mPosition != ListView.INVALID_POSITION) {
					mListView.smoothScrollToPosition(mPosition);
					mListView.setSelection(mPosition);
				}
			}

			@Override
			public void onFailure(Throwable t) {

			}
		});
	}

}
