package com.michelin.connectedfleet.eld.ui.data;

import android.content.Context;
import android.os.StrictMode;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserRequest;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserResponse;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Class that handles authentication w/ login credentials and retrieves user information using Volley
 */
public class WebLoginDataSource implements LoginDataSource {

    private static final String LOGIN_URL = "http://localhost:8080/users/login";
    private final RequestQueue requestQueue;

    private final UserService userService;

    public WebLoginDataSource(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:8080/users/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        userService = retrofit.create(UserService.class);
    }

    @Override
    public Result<LoggedInUser> login(String username, String password) {
        Call<LoginUserResponse> login = userService.login(new LoginUserRequest(username, password));

        // TODO: Don't do this
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        LoggedInUser user;
        try {
            Response<LoginUserResponse> almostResponse = login.execute();
            LoginUserResponse response = almostResponse.body();
            if (response == null) {
                return new Result.Error<>(new NullPointerException());
            }
            user = new LoggedInUser(response.token(), response.username());
        } catch (IOException e) {
            return new Result.Error<>(e);
        }
        return new Result.Success<>(user);
    }

    @Override
    public void logout() {
        // TODO
    }

    /**
     * Helper class to handle async callback results
     */
    private static class ResultCallback {
        private Result<LoggedInUser> result;

        synchronized void onSuccess(Result.Success<LoggedInUser> success) {
            this.result = success;
            notifyAll();
        }

        synchronized void onError(Result.Error error) {
            this.result = error;
            notifyAll();
        }

        synchronized Result<LoggedInUser> getResult() {
            while (result == null) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    return new Result.Error(new IOException("Error waiting for result", e));
                }
            }
            return result;
        }
    }
}
