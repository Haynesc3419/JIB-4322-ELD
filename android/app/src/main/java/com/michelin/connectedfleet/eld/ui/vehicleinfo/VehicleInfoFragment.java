package com.michelin.connectedfleet.eld.ui.vehicleinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.databinding.FragmentVehicleInfoBinding;
import com.michelin.connectedfleet.eld.ui.data.MetricConversionHelper;

import java.text.DecimalFormat;

public class VehicleInfoFragment extends Fragment {

    private FragmentVehicleInfoBinding binding;

    private static final DecimalFormat oneDecimalPoint = new DecimalFormat("#.#");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVehicleInfoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupVehicleDisplay();

        return root;
    }

    private void setupVehicleDisplay() {
        final TextView vehicleInformation = binding.truckInformation;
        final TextView vehicleOdometer = binding.odometerReading;

        VehicleInfoViewModel vm = new ViewModelProvider(this).get(VehicleInfoViewModel.class);

        vm.getVehicle().observe(getViewLifecycleOwner(), newVehicle -> {
            vehicleInformation.setText(newVehicle.getShortIdentifier());
            if (newVehicle.getClass() == MockVehicle.class) {
                MockVehicle mv = (MockVehicle) newVehicle;
                mv.registerTimerTrigger(() -> {
                    if (!isAdded()) return;

                    vehicleOdometer.setText(MetricConversionHelper.getDistanceWithUnit(newVehicle.getOdometer(), ((MainActivity) requireActivity()).getUnitSettings().isMetric()));
                });
            }
            vehicleOdometer.setText(MetricConversionHelper.getDistanceWithUnit(newVehicle.getOdometer(), ((MainActivity) requireActivity()).getUnitSettings().isMetric()));
        });
    }

    @Override
    public void onDestroyView() {
        VehicleInfoViewModel vm = new ViewModelProvider(getActivity()).get(VehicleInfoViewModel.class);
        vm.storeVehicleData();
        super.onDestroyView();
        binding = null;
    }
}