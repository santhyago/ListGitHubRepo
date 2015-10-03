package br.com.santhyago.tests.listrepo;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.santhyago.tests.listrepo.data.GitHubContract;

public class FragmentRepoDetail extends Fragment {
	public static final String REPO_ID_KEY = "repo_id";

	private long mRepoID;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(REPO_ID_KEY, mRepoID);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Bundle arguments = getArguments();
		if (arguments != null) {
			mRepoID = arguments.getLong(DetailActivity.REPO_ID_KEY);
		}

		if (savedInstanceState != null) {
			mRepoID = savedInstanceState.getLong(REPO_ID_KEY);
		}

		View rootView = inflater.inflate(R.layout.fragment_repo_detail, container, false);
		TextView mRepoNameView = (TextView) rootView.findViewById(R.id.fragment_detail_repo_name);
		TextView mLanguageView = (TextView) rootView.findViewById(R.id.fragment_detail_language);
		TextView mStarsCount = (TextView) rootView.findViewById(R.id.fragment_detail_stargazers_count);
		TextView mWatchersCount = (TextView) rootView.findViewById(R.id.fragment_detail_watchers_count);

		Cursor cursor = getActivity().getContentResolver().query(GitHubContract.Repo.buildRepoUri(mRepoID), null, null, null, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			mRepoNameView.setText(cursor.getString(cursor.getColumnIndex(GitHubContract.Repo.COL_NAME)));
			mLanguageView.setText(cursor.getString(cursor.getColumnIndex(GitHubContract.Repo.COL_LANG)));
			mStarsCount.setText(cursor.getString(cursor.getColumnIndex(GitHubContract.Repo.COL_STAR_COUNT)));
			mWatchersCount.setText(cursor.getString(cursor.getColumnIndex(GitHubContract.Repo.COL_WTCH_COUNT)));
			cursor.close();
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			mRepoID = savedInstanceState.getLong(REPO_ID_KEY);
		}
	}
}
