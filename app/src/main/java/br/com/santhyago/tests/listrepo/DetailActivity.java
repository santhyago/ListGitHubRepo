package br.com.santhyago.tests.listrepo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    public static final String REPO_ID_KEY = "repo_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            long repoID = getIntent().getLongExtra(REPO_ID_KEY, 0);

            Bundle arguments = new Bundle();
            arguments.putLong(DetailActivity.REPO_ID_KEY, repoID);

            FragmentRepoDetail fragment = new FragmentRepoDetail();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.repo_detail_container, fragment)
                    .commit();
        }
    }
}
