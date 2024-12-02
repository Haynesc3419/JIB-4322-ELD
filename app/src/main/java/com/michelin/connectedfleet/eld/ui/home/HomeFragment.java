package com.michelin.connectedfleet.eld.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentHomeBinding;
import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.status.StatusViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewLogs;
        ListAdapter<Date, LogsViewHolder> adapter = new LogsAdapter(getResources().getConfiguration().getLocales().get(0));
        recyclerView.setAdapter(adapter);
        homeViewModel.getDates().observe(getViewLifecycleOwner(), adapter::submitList);

        homeViewModel.hoursRemaining.breakHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingBreak, binding.textHoursRemainingBreak)
        );
        homeViewModel.hoursRemaining.drivingHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingDriving, binding.textHoursRemainingDriving)
        );
        homeViewModel.hoursRemaining.dayResetHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingDayReset, binding.textHoursRemainingDayReset)
        );

        TextView currentStatus = binding.textviewHomeCurrentStatus;
        StatusViewModel.getStatus().observe(getViewLifecycleOwner(), driverStatus -> {
            currentStatus.setText(getResources().getTextArray(R.array.statuses)[driverStatus.ordinal()]);
        });

        Button changeStatusButton = binding.homeButtonChangeStatus;
        changeStatusButton.setOnClickListener(v -> {
            MainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_status);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class LogsAdapter extends androidx.recyclerview.widget.ListAdapter<Date, LogsViewHolder> {
        private static Locale locale;

        private static String convertDate(Date date) {
            String convertedDate = DateFormat.getDateInstance().format(date);

            long timeSince = new Date().getTime() - date.getTime();
            if (TimeUnit.DAYS.convert(timeSince, TimeUnit.MILLISECONDS) < 7) {
                String dayOfWeek = new SimpleDateFormat("EEEE", locale).format(date);
                convertedDate = dayOfWeek + " (" + convertedDate + ")";
            }

            return convertedDate;
        }


        protected LogsAdapter(Locale locale) {
            super(new DiffUtil.ItemCallback<Date>() {
                @Override
                public boolean areItemsTheSame(@NonNull Date oldItem, @NonNull Date newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Date oldItem, @NonNull Date newItem) {
                    return oldItem.equals(newItem);
                }
            });

            LogsAdapter.locale = locale;
        }

        @NonNull
        @Override
        public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogsBinding binding = ItemLogsBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new LogsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
            holder.textView.setText(convertDate(getItem(position)));
        }
    }

    private static class LogsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public LogsViewHolder(ItemLogsBinding binding) {
            super(binding.getRoot());
            textView = binding.textViewItemLogsDate;
        }
    }
}