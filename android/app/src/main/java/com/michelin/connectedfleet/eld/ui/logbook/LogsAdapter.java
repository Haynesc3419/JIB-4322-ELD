package com.michelin.connectedfleet.eld.ui.logbook;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.data.LoggedDay;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LogsAdapter extends ListAdapter<LoggedDay, LogsAdapter.LogsViewHolder> {
    private List<LoggedDay> days;
    private static Locale locale;
    private final OnLogClickListener onLogClickListener;

    protected LogsAdapter(Locale locale, OnLogClickListener onLogClickListener) {
        super(new DiffUtil.ItemCallback<>() {
            @Override
            public boolean areItemsTheSame(@NonNull LoggedDay oldItem, @NonNull LoggedDay newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areContentsTheSame(@NonNull LoggedDay oldItem, @NonNull LoggedDay newItem) {
                return oldItem.equals(newItem);
            }
        });

        LogsAdapter.locale = locale;
        this.onLogClickListener = onLogClickListener;
    }

    @Override
    public void submitList(List<LoggedDay> list) {
        days = list;
        super.submitList(list);
    }

    @Override
    public int getItemCount() {
        return (days == null) ? 0 : days.size();
    }

    @NonNull
    @Override
    public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLogsBinding binding = ItemLogsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LogsViewHolder(binding, onLogClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
        LoggedDay item = days.get(position);
        holder.calendarDayView.setText(String.valueOf(item.date().getDayOfMonth()));
        holder.calendarMonthView.setText(item.getMonthAbbreviation());
        holder.detailsView.setText("Drove for " + item.getTimeDriven());

        // Set click listener
        holder.itemView.setOnClickListener(v -> onLogClickListener.onLogClick(item));
    }

    public static class LogsViewHolder extends RecyclerView.ViewHolder {
        private final TextView calendarDayView;
        private final TextView calendarMonthView;
        private final TextView detailsView;

        public LogsViewHolder(ItemLogsBinding binding, OnLogClickListener clickListener) {
            super(binding.getRoot());
            calendarDayView = binding.textViewItemLogsDateNum;
            calendarMonthView = binding.textViewItemLogsDateMonth;
            detailsView = binding.textViewDetails;
        }
    }
}
