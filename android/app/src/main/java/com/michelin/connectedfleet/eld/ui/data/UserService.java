package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetUserInfoResponse;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserRequest;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.LoginUserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;


public interface UserService {
    @POST("login")
    Call<LoginUserResponse> login(@Body LoginUserRequest request);

    @GET("info")
    Call<GetUserInfoResponse> getInfo(@Header("cookie") String cookie);
}
