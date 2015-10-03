package br.com.santhyago.tests.listrepo.model;

import java.util.List;

import br.com.santhyago.tests.listrepo.model.Repo;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by santhyago on 10/2/15.
 */
public interface GitHubService {
    @GET("/users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}
