package com.michelin.connectedfleet.eld.ui.logbook;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChangeRequestDialog extends DialogFragment {

    private static final String ARG_LOG_IDS = "log_ids";
    private LogEntryService logsService;
    private List<String> logIds;

    private TextInputLayout changeRequestInputLayout;
    private TextInputEditText changeRequestEditText;
    private MaterialButton submitButton;
    private MaterialButton cancelButton;
    private ProgressBar progressBar;

    public static ChangeRequestDialog newInstance(ArrayList<String> logIds) {
        ChangeRequestDialog fragment = new ChangeRequestDialog();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_LOG_IDS, logIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            logIds = getArguments().getStringArrayList(ARG_LOG_IDS);
        }
        // Initialize Retrofit
        initializeRetrofit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_change_request, container, false);

        changeRequestInputLayout = view.findViewById(R.id.change_request_input_layout);
        changeRequestEditText = view.findViewById(R.id.change_request_edit_text);
        submitButton = view.findViewById(R.id.button_submit_change);
        cancelButton = view.findViewById(R.id.button_cancel);
        progressBar = view.findViewById(R.id.progress_bar_change_request);

        cancelButton.setOnClickListener(v -> dismiss());
        submitButton.setOnClickListener(v -> submitChangeRequest());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = getResources().getDimensionPixelSize(R.dimen.dialog_min_width); // Ensure this dimension exists
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void initializeRetrofit() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        // Potentially register LocalDateTime adapter if needed by the API
        // gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
        //         LocalDateTime.parse(json.getAsString()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/logs/") // Base URL should end with /
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        logsService = retrofit.create(LogEntryService.class);
    }

    private void submitChangeRequest() {
        String changeRequestText = changeRequestEditText.getText() != null ? changeRequestEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(changeRequestText)) {
            changeRequestInputLayout.setError("Change request description cannot be empty");
            return;
        } else {
            changeRequestInputLayout.setError(null); // Clear error
        }

        showLoading(true);

        // Format the request body as needed by your backend
        String requestBody = "Change request for IDs: " + logIds.toString() + " - " + changeRequestText;

        Call<Void> changeRequestCall = logsService.changeRequest(requestBody);

        changeRequestCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Log.d("ChangeRequestDialog", "Change request submitted successfully.");
                    Toast.makeText(getContext(), "Change request submitted successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Log.e("ChangeRequestDialog", "Failed to submit change request. Code: " + response.code());
                    Toast.makeText(getContext(), "Failed to submit change request: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("ChangeRequestDialog", "Error submitting change request", t);
                Toast.makeText(getContext(), "Error submitting change request: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            submitButton.setEnabled(false);
            cancelButton.setEnabled(false);
            changeRequestEditText.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
            changeRequestEditText.setEnabled(true);
        }
    }
} 