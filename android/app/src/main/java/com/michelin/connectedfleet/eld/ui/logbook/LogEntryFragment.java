package com.michelin.connectedfleet.eld.ui.logbook;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LogEntry;

public class LogEntryFragment extends DialogFragment {

    String logEntry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_entry, container, false);

        Button closeButton = view.findViewById(R.id.button_close);
        closeButton.setOnClickListener(v -> dismiss());

        TextView logEntryTextView = view.findViewById(R.id.entry_text);

        Bundle args = getArguments();

        if (args != null) {
            logEntry = args.getString("log_entry");
            logEntryTextView.setText(logEntry);
        }

        return view;
    }
}
