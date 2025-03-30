package com.michelin.connectedfleet.eld.ui.logbook;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
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

import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LogEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntryFragment extends DialogFragment {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_entry, container, false);

        Button closeButton = view.findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> dismiss());

        TextView logEntryTextView = view.findViewById(R.id.entry_text);

        Bundle args = getArguments();


        if (args != null) {
            String logEntries = args.getString("log_entries");
            StringBuilder logEntryText = new StringBuilder();

            // Regex to extract status and dateTime
            String regex = "GetLogEntryResponseItem\\[status=(.*?), dateTime=(.*?)\\]";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(logEntries);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm a");

            int counter = 0;
            while (matcher.find()) {
                counter++;
                String status = matcher.group(1);
                String dateTimeString = matcher.group(2);

                // Convert full timestamp to just HH:mm
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, inputFormatter);
                String formattedTime = dateTime.format(outputFormatter);

                logEntryText.append(counter).append(". ")
                        .append("Status: ").append(status).append("\n")
                        .append("Time: ").append(formattedTime).append("\n\n");
            }

            logEntryTextView.setText(logEntryText.toString().trim());
        }



        return view;
    }
}
