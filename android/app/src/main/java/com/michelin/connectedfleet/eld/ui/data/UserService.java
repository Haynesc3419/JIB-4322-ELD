package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserRequest;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface UserService {
    @POST("login")
    Call<LoginUserResponse> login(@Body LoginUserRequest request);
}
