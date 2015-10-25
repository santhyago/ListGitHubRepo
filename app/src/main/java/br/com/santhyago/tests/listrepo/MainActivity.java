package br.com.santhyago.tests.listrepo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import br.com.santhyago.tests.listrepo.adapter.ProjectAdapter;
import br.com.santhyago.tests.listrepo.data.GitHubContract;
import br.com.santhyago.tests.listrepo.model.GitHubService;
import br.com.santhyago.tests.listrepo.model.Repo;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity implements FragmentListRepo.Callback {
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
    //private ListRepoAdapter mListRepoAdapter;
    private ProjectAdapter mProjectAdapter;

    CoordinatorLayout.Behavior behavior;

    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private Context mContext;
    private RecyclerView recyclerView;
    public boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.repo_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.repo_detail_container, new FragmentRepoDetail())
                        .commit();
            }
        } else {
            mTwoPane = false;
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mContext = this;
            mContentResolver = mContext.getContentResolver();

            pref = mContext.getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE);

            if (!hasUpdatedData()) {
                updateLocalData();
            }

            mProjectAdapter = new ProjectAdapter(mContext, mContentResolver.query(GitHubContract.Repo.CONTENT_URI, null, null, null, null, null), onClickListener());

            setTitle("Repository list");

            recyclerView = (RecyclerView) findViewById(R.id.listview_repos);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(mProjectAdapter);

            if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
                mPosition = savedInstanceState.getInt(SELECTED_KEY);
            }
        }
    }

    @Override
    public void onItemSelected(long repoID) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putLong(FragmentRepoDetail.REPO_ID_KEY, repoID);

            FragmentRepoDetail fragment = new FragmentRepoDetail();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.repo_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(DetailActivity.REPO_ID_KEY, repoID);
            startActivity(intent);
        }
    }

    private ProjectAdapter.ProjectOnClickListener onClickListener() {
        return new ProjectAdapter.ProjectOnClickListener() {
            @Override
            public void onClickProject(View view, int idx) {
                selectItem(idx);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Boolean.parseBoolean(getResources().getString(R.string.two_panel))) {
            if (mPosition == ListView.INVALID_POSITION) {
                selectItem(0);
                mListView.setItemChecked(0, true);
            }
        }
    }

    private void selectItem(int position) {
        if (mProjectAdapter == null) {
            mProjectAdapter = new ProjectAdapter(mContext, mContentResolver.query(GitHubContract.Repo.CONTENT_URI, null, null, null, null, null), onClickListener());
        }
        Cursor cursor = mProjectAdapter.getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            onItemSelected(cursor.getLong(COL_REPO_ID));
        }
        mPosition = position;
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
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

                mProjectAdapter.swapCursor(mContentResolver.query(GitHubContract.Repo.CONTENT_URI, null, null, null, null, null));
                mProjectAdapter.notifyDataSetChanged();

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