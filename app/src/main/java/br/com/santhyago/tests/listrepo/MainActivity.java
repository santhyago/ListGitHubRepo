package br.com.santhyago.tests.listrepo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import br.com.santhyago.tests.listrepo.model.Repo;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.github.com";
    private static final String TAG = "SAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubService service = retrofit.create(GitHubService.class);

        Call<List<Repo>> call = service.listRepos("torvalds");
        call.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Response<List<Repo>> response, Retrofit retrofit) {
                List<Repo> repos = response.body();
                for (Repo r : repos) {

                    Log.d(TAG, r.getName());
                    Log.d(TAG, r.getDescription());
                    Log.d(TAG, " " + r.getStargazersCount());
                    Log.d(TAG, " " + r.getWatchersCount());
                    Log.d(TAG, r.getLanguage());
                }
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });

    }


}
