package com.michelin.connectedfleet.eld.ui.data;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that handles authentication w/ login credentials and retrieves user information using Volley
 */
public class WebLoginDataSource implements LoginDataSource {

    private static final String LOGIN_URL = "http://localhost:8080/users/login"; 
    private final RequestQueue requestQueue;

    public WebLoginDataSource(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @Override
    public Result<LoggedInUser> login(String username, String password) {
        final ResultCallback callback = new ResultCallback();

        JSONObject payload = new JSONObject();
        try {
            payload.put("username", username);
            payload.put("password", password);
        } catch (JSONException e) {
            return new Result.Error(new IOException("Error creating JSON payload", e));
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            callback.onSuccess(new Result.Success<>(null));
                        } catch (JSONException e) {
                            callback.onError(new Result.Error<>(IOException("Error parsing server response", e)));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(new Result.Error<>(IOException("Login request failed: " + error.getMessage(), error)));
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
        return callback.getResult();
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
