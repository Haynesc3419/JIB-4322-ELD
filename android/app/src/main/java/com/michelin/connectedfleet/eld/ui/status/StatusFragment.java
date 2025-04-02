package com.michelin.connectedfleet.eld.ui.status;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;

import com.michelin.connectedfleet.eld.DriverStatus;
import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentStatusBinding;
import com.michelin.connectedfleet.eld.ui.data.util.TimeZoneManager;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class StatusFragment extends Fragment {

    private FragmentStatusBinding binding;
    private TimeZoneManager timeZoneManager;
    private Handler timeUpdateHandler;
    private static final long TIME_UPDATE_INTERVAL = 1000; // 1 second

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        timeZoneManager = ((MainActivity) requireActivity()).getTimeZoneManager();
        timeUpdateHandler = new Handler(Looper.getMainLooper());
        
        updateTimeZoneDisplay();
        startTimeUpdates();

        final TextView textView = binding.textviewStatusCurrentStatus;

        StatusViewModel.getStatus().observe(getViewLifecycleOwner(), driverStatus ->
                textView.setText(getResources().getTextArray(R.array.statuses)[driverStatus.ordinal()])
        );

        Spinner chooseStatus = binding.spinnerChangeStatus;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.statuses, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseStatus.setAdapter(adapter);

        chooseStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StatusViewModel.setStatus(DriverStatus.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return root;
    }

    private void updateTimeZoneDisplay() {
        if (binding.textviewStatusTimezone != null) {
            binding.textviewStatusTimezone.setText(timeZoneManager.getCurrentTimeZoneDisplay());
        }
    }

    private void updateTimeDisplay() {
        if (binding.textviewStatusCurrentTime != null) {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault());
            binding.textviewStatusCurrentTime.setText(currentTime.format(formatter));
        }
    }

    private void startTimeUpdates() {
        timeUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTimeDisplay();
                timeUpdateHandler.postDelayed(this, TIME_UPDATE_INTERVAL);
            }
        });
    }

    private void stopTimeUpdates() {
        timeUpdateHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTimeZoneDisplay();
        startTimeUpdates();
        
        timeZoneManager.getCurrentTimeZone().observe(getViewLifecycleOwner(), timezone -> {
            if (binding.textviewStatusTimezone != null) {
                binding.textviewStatusTimezone.setText(timezone);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimeUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimeUpdates();
        binding = null;
    }
}