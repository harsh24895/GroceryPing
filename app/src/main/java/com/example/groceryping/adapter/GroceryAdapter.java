package com.example.groceryping.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groceryping.data.GroceryItem;
import com.example.groceryping.R;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.GroceryViewHolder> {
    private List<GroceryItem> items;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GroceryItem item);
        void onDeleteClick(GroceryItem item);
        void onCheckBoxClick(GroceryItem item, boolean isChecked);
    }

    public GroceryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery, parent, false);
        return new GroceryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        GroceryItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public void setItems(List<GroceryItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    static class GroceryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewDetails;
        private final CheckBox checkBoxCompleted;
        private final ImageButton buttonDelete;

        public GroceryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewDetails = itemView.findViewById(R.id.textViewDetails);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(GroceryItem item, OnItemClickListener listener) {
            textViewName.setText(item.getName());
            String details = String.format("%s • %d x $%.2f", 
                item.getLocation(), 
                item.getQuantity(), 
                item.getPrice());
            textViewDetails.setText(details);
            checkBoxCompleted.setChecked(item.isCompleted());

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            buttonDelete.setOnClickListener(v -> listener.onDeleteClick(item));
            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> 
                listener.onCheckBoxClick(item, isChecked));
        }
    }
} 