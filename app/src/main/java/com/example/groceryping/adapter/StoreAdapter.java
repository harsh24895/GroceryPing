package com.example.groceryping.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groceryping.R;
import com.example.groceryping.data.StoreLocation;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    private List<StoreLocation> stores = new ArrayList<>();
    private OnStoreClickListener listener;

    public interface OnStoreClickListener {
        void onStoreClick(StoreLocation store);
        void onStoreToggle(StoreLocation store, boolean isActive);
        void onStoreDelete(StoreLocation store);
    }

    public StoreAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        StoreLocation store = stores.get(position);
        holder.bind(store);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    public void setStores(List<StoreLocation> stores) {
        this.stores = stores;
        notifyDataSetChanged();
    }

    class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStoreName;
        private final TextView tvStoreAddress;
        private final SwitchMaterial switchActive;
        private final MaterialButton buttonDelete;

        StoreViewHolder(View itemView) {
            super(itemView);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreAddress = itemView.findViewById(R.id.tvStoreAddress);
            switchActive = itemView.findViewById(R.id.switchActive);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStoreClick(stores.get(position));
                }
            });

            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStoreToggle(stores.get(position), isChecked);
                }
            });

            buttonDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onStoreDelete(stores.get(position));
                }
            });
        }

        void bind(StoreLocation store) {
            tvStoreName.setText(store.getName());
            tvStoreAddress.setText(store.getAddress());
            switchActive.setChecked(store.isActive());
        }
    }
} 