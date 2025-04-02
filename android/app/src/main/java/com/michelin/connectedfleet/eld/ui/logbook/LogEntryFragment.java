package com.michelin.connectedfleet.eld.ui.logbook;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LogEntry;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.util.UnitSettings;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogEntryFragment extends DialogFragment {
    LogEntryService logsService;
    LogEntry logEntry;
    private UnitSettings unitSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_entry, container, false);

        // Get UnitSettings from MainActivity
        unitSettings = ((MainActivity) requireActivity()).getUnitSettings();

        Button closeButton = view.findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> dismiss());

        Button submitButton = view.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(v -> dismiss());

        TextView logEntryTextView = view.findViewById(R.id.entry_text);

        Bundle args = getArguments();

        if (args != null) {
            String logEntries = args.getString("log_entries");
            StringBuilder logEntryText = new StringBuilder();
            Log.d("logEntries", logEntries.toString());

            // Regex to extract status, dateTime, distance, and speed
            String regex = "GetLogEntryResponseItem\\[status=(.*?), dateTime=(.*?), distanceMiles=(.*?), speedMph=(.*?)\\]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(logEntries);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm a");

            int counter = 0;
            while (matcher.find()) {
                counter++;
                String status = matcher.group(1);
                String dateTimeString = matcher.group(2);
                double distanceMiles = Double.parseDouble(matcher.group(3));
                double speedMph = Double.parseDouble(matcher.group(4));

                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
                String formattedTime = dateTime.format(outputFormatter);

                // Format distance and speed according to user's unit preference
                String distanceStr = unitSettings.formatDistance(distanceMiles);
                String speedStr = unitSettings.formatSpeed(speedMph);

                logEntryText
                        .append(counter).append(". ")
                        .append("Status: ").append(status).append("\n")
                        .append("Time: ").append(formattedTime).append("\n")
                        .append("Distance: ").append(distanceStr).append("\n")
                        .append("Speed: ").append(speedStr).append("\n");

                logEntryText.append("\n\n");
            }

            logEntryTextView.setText(logEntryText.toString().trim());

            // Observe unit changes and update display
            unitSettings.getUseMetric().observe(getViewLifecycleOwner(), isMetric -> {
                // Refresh the display when unit preference changes
                logEntryTextView.setText(logEntryText.toString().trim());
            });
        }

        return view;
    }
}
