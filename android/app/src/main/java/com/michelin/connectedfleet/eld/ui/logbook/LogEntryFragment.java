package com.michelin.connectedfleet.eld.ui.logbook;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.MetricConversionHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogEntryFragment extends DialogFragment {
    private static final String TAG = "LogEntryFragment";
    private static final String ARG_LOG_ENTRIES = "log_entries";

    private LogEntryService logEntryService;
    private List<String> ids = new ArrayList<>();
    private List<Boolean> signedStatus = new ArrayList<>(); // Keep track of signed status
    private LinearLayout logEntriesContainer;
    private ProgressBar progressBarSigning;
    private MaterialButton signButton;
    private MaterialButton changeRequestButton;
    private MaterialButton closeButton;
    private String rawLogEntriesString;

    public static LogEntryFragment newInstance(String logEntries) {
        LogEntryFragment fragment = new LogEntryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOG_ENTRIES, logEntries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            rawLogEntriesString = getArguments().getString(ARG_LOG_ENTRIES);
        }
        initializeRetrofit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Use the layout defined in layout-sw600dp for tablets
        View view = inflater.inflate(R.layout.fragment_log_entry, container, false);

        logEntriesContainer = view.findViewById(R.id.log_entries_container);
        progressBarSigning = view.findViewById(R.id.progress_bar_signing);
        closeButton = view.findViewById(R.id.button_close);
        signButton = view.findViewById(R.id.button_sign);
        changeRequestButton = view.findViewById(R.id.button_change_request);

        closeButton.setOnClickListener(v -> dismiss());
        signButton.setOnClickListener(v -> onSignClick());
        changeRequestButton.setOnClickListener(v -> onChangeRequestClick());

        parseAndDisplayLogEntries(rawLogEntriesString);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            // Use WRAP_CONTENT for height and a specific width or MATCH_PARENT based on design
            // Let's use wrap_content for height and match_parent for width in this case
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            // You might want to set a minWidth using dimens as done in ChangeRequestDialog if needed
        }
    }

    private void initializeRetrofit() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                LocalDateTime.parse(json.getAsString()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/logs/") // Ensure base URL ends with /
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        logEntryService = retrofit.create(LogEntryService.class);
    }

    private void parseAndDisplayLogEntries(String logEntries) {
        if (logEntries == null || logEntriesContainer == null) {
            Log.e(TAG, "Log entries string or container is null.");
            return;
        }

        logEntriesContainer.removeAllViews(); // Clear previous entries/placeholder
        ids.clear();
        signedStatus.clear();

        Log.d(TAG, "Parsing log entries: " + logEntries);

        // Regex to extract id, status, dateTime, verifiedByDriver
        String regex = "GetLogEntryResponseItem\\[id=([^,]*?), odometerReading=(.+?), status=([^,]*?), dateTime=([^,]*?), verifiedByDriver=(.*?)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(logEntries);

        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // Assuming standard ISO format
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a"); // Use hh for 12-hour format with AM/PM

        boolean entriesFound = false;
        int counter = 0;
        while (matcher.find()) {
            entriesFound = true;
            counter++;
            try {
                String id = matcher.group(1).trim();
                float odometerReading = Float.parseFloat(matcher.group(2).trim());
                String status = matcher.group(3).trim();
                String dateTimeString = matcher.group(4).trim();
                String verifiedByDriverStr = matcher.group(4) != null ? matcher.group(5).trim() : "0"; // Default to 0 if null
                boolean isVerified = "1".equals(verifiedByDriverStr);

                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
                String formattedTime = dateTime.format(outputFormatter);

                this.ids.add(id);
                this.signedStatus.add(isVerified);

                addLogEntryView(counter, id, odometerReading, status, formattedTime, isVerified);

            } catch (Exception e) {
                Log.e(TAG, "Error parsing log entry match: " + matcher.group(0), e);
            }
        }

        if (!entriesFound) {
            Log.w(TAG, "No log entries found matching the pattern.");
            TextView noEntriesText = new TextView(getContext());
            noEntriesText.setText("No log entries to display.");
            noEntriesText.setPadding(16, 16, 16, 16);
            logEntriesContainer.addView(noEntriesText);
        }

        // Update button state based on whether all entries are signed
        updateSignButtonState();
    }

    private void addLogEntryView(int index, String id, float odometerReading, String status, String time, boolean isVerified) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        // Inflate a dedicated layout for each log entry item for better structure (optional but recommended)
        // For now, creating TextViews dynamically
        LinearLayout entryLayout = new LinearLayout(getContext());
        entryLayout.setOrientation(LinearLayout.VERTICAL);
        entryLayout.setPadding(0, 8, 0, 8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        entryLayout.setLayoutParams(params);

        String truncatedId = truncateId(id);
        String signedText = isVerified ? "✓ Signed" : "Needs Signing";
        int signedTextColor = isVerified ? R.color.status_valid : R.color.status_warning; // Use your color resources

        TextView entryDetails = new TextView(getContext());
        SharedPreferences unitSettings = getActivity().getSharedPreferences("unit_settings", Context.MODE_PRIVATE);
        boolean useMetric = unitSettings.getBoolean("use_metric", false);
        entryDetails.setText(String.format("%d. ID: %s\n   Odometer: %s\n   Status: %s\n   Time: %s",
                index, truncatedId, MetricConversionHelper.getDistanceWithUnit(odometerReading, useMetric), status, time));
        // Use AppCompat style as a fallback
        entryDetails.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body1);
        entryLayout.addView(entryDetails);

        TextView signedStatusView = new TextView(getContext());
        signedStatusView.setText(signedText);
        signedStatusView.setTag("status_" + id); // Set tag to find this view later
        signedStatusView.setTextColor(ContextCompat.getColor(requireContext(), signedTextColor));
        // Use AppCompat style as a fallback
        signedStatusView.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Body2);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        statusParams.setMarginStart(16); // Indent status
        signedStatusView.setLayoutParams(statusParams);
        entryLayout.addView(signedStatusView);

        logEntriesContainer.addView(entryLayout);
    }

    private String truncateId(String id) {
        if (id == null || id.length() <= 6) {
            return id;
        }
        return id.substring(0, 3) + "..." + id.substring(id.length() - 3);
    }

    private void onChangeRequestClick() {
        if (ids.isEmpty()) {
            Toast.makeText(getContext(), "No log entries to request changes for.", Toast.LENGTH_SHORT).show();
            return;
        }
        FragmentManager fm = getParentFragmentManager();
        ChangeRequestDialog changeRequestDialog = ChangeRequestDialog.newInstance(new ArrayList<>(ids));
        changeRequestDialog.show(fm, "ChangeRequestDialog");
        // Don't dismiss this dialog here
    }

    private void onSignClick() {
        if (ids.isEmpty()) {
            Toast.makeText(getContext(), "No logs to sign.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allSigned = true;
        for (boolean signed : signedStatus) {
            if (!signed) {
                allSigned = false;
                break;
            }
        }
        if (allSigned) {
            Toast.makeText(getContext(), "All logs are already signed.", Toast.LENGTH_SHORT).show();
            return;
        }

        showSigningLoading(true);

        // Use AtomicInteger to track completion of async calls
        final AtomicInteger pendingCalls = new AtomicInteger(ids.size());
        final AtomicInteger successfulCalls = new AtomicInteger(0);

        for (int i = 0; i < ids.size(); i++) {
            final String currentId = ids.get(i);
            final int index = i;

            // Only sign if not already signed
            if (!signedStatus.get(index)) {
                Call<Void> verifyRequest = logEntryService.verify(currentId);

                verifyRequest.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully signed log ID: " + currentId);
                            successfulCalls.incrementAndGet();
                            signedStatus.set(index, true); // Update local status
                            updateSignedStatusView(currentId, true);
                        } else {
                            Log.e(TAG, "Failed to sign log ID: " + currentId + ", Code: " + response.code());
                        }
                        checkCompletion();
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.e(TAG, "Error signing log ID: " + currentId, t);
                        checkCompletion();
                    }

                    private void checkCompletion() {
                        if (pendingCalls.decrementAndGet() == 0) {
                            // All calls finished
                            showSigningLoading(false);
                            handleSignAllResult(successfulCalls.get(), ids.size());
                        }
                    }
                });
            } else {
                // If already signed, just decrement pending calls count
                if (pendingCalls.decrementAndGet() == 0) {
                    showSigningLoading(false);
                    handleSignAllResult(successfulCalls.get(), ids.size());
                }
            }
        }
        // Do NOT dismiss the dialog here
    }

    private void updateSignedStatusView(String id, boolean isSigned) {
        if (logEntriesContainer != null) {
            TextView statusView = logEntriesContainer.findViewWithTag("status_" + id);
            if (statusView != null) {
                String signedText = isSigned ? "✓ Signed" : "Needs Signing";
                int signedTextColor = isSigned ? R.color.status_valid : R.color.status_warning;
                statusView.setText(signedText);
                statusView.setTextColor(ContextCompat.getColor(requireContext(), signedTextColor));
            }
        }
    }

    private void handleSignAllResult(int successCount, int totalCount) {
        if (getView() == null) return; // Fragment not attached

        if (successCount > 0) {
            Snackbar.make(getView(), String.format("Successfully signed %d log(s)", successCount), Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(getView(), "Failed to sign logs. Please try again.", Snackbar.LENGTH_LONG).show();
        }
        updateSignButtonState(); // Update button state after signing attempt
    }

    private void showSigningLoading(boolean isLoading) {
        if (progressBarSigning != null && signButton != null && changeRequestButton != null && closeButton != null) {
            progressBarSigning.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            signButton.setEnabled(!isLoading);
            changeRequestButton.setEnabled(!isLoading);
            closeButton.setEnabled(!isLoading);
        }
    }

    private void updateSignButtonState() {
        boolean allSigned = true;
        if(signedStatus.isEmpty()) { // Handle case where parsing failed or no logs
            allSigned = false;
        } else {
            for (boolean signed : signedStatus) {
                if (!signed) {
                    allSigned = false;
                    break;
                }
            }
        }
        if (signButton != null) {
            signButton.setEnabled(!allSigned);
            signButton.setText(allSigned ? "All Signed" : "Sign");
        }
    }
}
