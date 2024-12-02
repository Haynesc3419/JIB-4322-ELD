package com.michelin.connectedfleet.eld.ui.status;

import android.os.Bundle;
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

import java.util.Objects;

public class StatusFragment extends Fragment {

    private FragmentStatusBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}