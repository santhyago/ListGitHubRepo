package br.com.santhyago.tests.listrepo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements FragmentListRepo.Callback {
    private boolean mTwoPane;

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
        }

        FragmentListRepo fragmentListRepo =  ((FragmentListRepo)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        fragmentListRepo.setInLandMode(mTwoPane);
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
}