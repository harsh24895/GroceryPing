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
import java.util.ArrayList;
import java.util.List;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryAdapter.ViewHolder> {
    private List<GroceryItem> items;
    private List<GroceryItem> filteredItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(GroceryItem item);
        void onDeleteClick(GroceryItem item);
        void onCheckBoxClick(GroceryItem item, boolean isChecked);
    }

    public GroceryAdapter(OnItemClickListener listener) {
        this.listener = listener;
        this.items = new ArrayList<>();
        this.filteredItems = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_grocery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroceryItem item = filteredItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public void setItems(List<GroceryItem> newItems) {
        this.items = new ArrayList<>(newItems);
        this.filteredItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        filteredItems.clear();
        if (query.isEmpty()) {
            filteredItems.addAll(items);
        } else {
            for (GroceryItem item : items) {
                if (item.getName().toLowerCase().contains(query) ||
                    item.getCategory().toLowerCase().contains(query) ||
                    item.getLocation().toLowerCase().contains(query)) {
                    filteredItems.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView categoryText;
        private final TextView locationText;
        private final TextView priceText;
        private final CheckBox checkBox;
        private final ImageButton deleteButton;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.textName);
            categoryText = view.findViewById(R.id.textCategory);
            locationText = view.findViewById(R.id.textLocation);
            priceText = view.findViewById(R.id.textPrice);
            checkBox = view.findViewById(R.id.checkBox);
            deleteButton = view.findViewById(R.id.buttonDelete);
        }

        void bind(GroceryItem item, OnItemClickListener listener) {
            nameText.setText(item.getName());
            categoryText.setText(item.getCategory());
            locationText.setText(item.getLocation());
            priceText.setText(String.format("$%.2f", item.getPrice()));
            checkBox.setChecked(item.isCompleted());

            itemView.setOnClickListener(v -> listener.onItemClick(item));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> 
                listener.onCheckBoxClick(item, isChecked));
        }
    }
} 