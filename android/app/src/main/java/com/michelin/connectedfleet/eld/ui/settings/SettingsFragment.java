package com.michelin.connectedfleet.eld.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentSettingsBinding;
import com.michelin.connectedfleet.eld.ui.data.util.UnitSettings;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private UnitSettings unitSettings;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        unitSettings = ((MainActivity) requireActivity()).getUnitSettings();
        setupUnitToggle();

        return root;
    }

    private void setupUnitToggle() {
        Switch metricSwitch = binding.switchMetricUnits;
        
        // Set initial state
        metricSwitch.setChecked(unitSettings.isMetric());

        // Listen for changes
        metricSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            unitSettings.setUseMetric(isChecked);
        });

        // Observe changes from other parts of the app
        unitSettings.getUseMetric().observe(getViewLifecycleOwner(), isMetric -> {
            if (metricSwitch.isChecked() != isMetric) {
                metricSwitch.setChecked(isMetric);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}