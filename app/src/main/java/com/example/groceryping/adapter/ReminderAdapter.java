package com.example.groceryping.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groceryping.R;
import com.example.groceryping.data.Reminder;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<Reminder> reminders = new ArrayList<>();
    private final OnReminderActionListener listener;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnReminderActionListener {
        void onEditReminder(Reminder reminder);
        void onCancelReminder(Reminder reminder);
    }

    public ReminderAdapter(OnReminderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);
        holder.bind(reminder);
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView reminderItemName;
        private final TextView reminderMessage;
        private final TextView reminderTime;
        private final TextView reminderDate;
        private final TextView reminderRepeatInfo;
        private final MaterialButton buttonEdit;
        private final MaterialButton buttonCancel;

        ReminderViewHolder(View itemView) {
            super(itemView);
            reminderItemName = itemView.findViewById(R.id.reminderItemName);
            reminderMessage = itemView.findViewById(R.id.reminderMessage);
            reminderTime = itemView.findViewById(R.id.reminderTime);
            reminderDate = itemView.findViewById(R.id.reminderDate);
            reminderRepeatInfo = itemView.findViewById(R.id.reminderRepeatInfo);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonCancel = itemView.findViewById(R.id.buttonCancel);

            buttonEdit.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditReminder(reminders.get(position));
                }
            });

            buttonCancel.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Log.d("ReminderAdapter", "Cancel button clicked for reminder: " + reminders.get(position).getItemName());
                    listener.onCancelReminder(reminders.get(position));
                }
            });
        }

        void bind(Reminder reminder) {
            reminderItemName.setText(reminder.getItemName());
            reminderMessage.setText(reminder.getMessage());
            reminderTime.setText(timeFormat.format(reminder.getTimeInMillis()));
            reminderDate.setText(dateFormat.format(reminder.getTimeInMillis()));
            
            if (reminder.isRepeating()) {
                String repeatText = "Repeats ";
                switch (reminder.getRepeatInterval()) {
                    case 1:
                        repeatText += "daily";
                        break;
                    case 7:
                        repeatText += "weekly";
                        break;
                    default:
                        repeatText += "every " + reminder.getRepeatInterval() + " days";
                }
                reminderRepeatInfo.setText(repeatText);
                reminderRepeatInfo.setVisibility(View.VISIBLE);
            } else {
                reminderRepeatInfo.setVisibility(View.GONE);
            }
        }
    }
} 