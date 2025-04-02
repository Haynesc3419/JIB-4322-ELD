package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.CreateLogEntryRequest;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.CreateLogEntryResponse;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface LogEntryService {
    @POST("insertEntry")
    Call<CreateLogEntryResponse> insert(@Body CreateLogEntryRequest request);

    @GET("/logs")
    Call<List<GetLogEntryResponseItem>> getLogEntries(@Header("cookie") String cookie);

    @PUT("verify")
    Call<Void> verify(@Body String id);

    @POST("changeRequest")
    Call<Void> changeRequest(@Body String changeBody);
}
