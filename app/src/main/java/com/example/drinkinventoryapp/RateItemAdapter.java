package com.example.drinkinventoryapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.drinkinventoryapp.RateItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class RateItemAdapter extends RecyclerView.Adapter<RateItemAdapter.RatingViewHolder> {

    public interface OnItemActionListener {
        void onEdit(RateItem item, int position);
        void onDelete(RateItem item, int position);
        void onRatingChanged(RateItem item, int newRating, int position);

    }

    private final Context context;
    private List<RateItem> items;
    private OnItemActionListener listener;

    public RateItemAdapter(Context context, List<RateItem> items) {
        this.context = context;
        this.items = items != null ? items : new ArrayList<>();

    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    /** Replace the full dataset and refresh */
    public void setItems(List<com.example.drinkinventoryapp.RateItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Update a single item in place */
    public void updateItem(int position, RateItem updated) {
        if (position >= 0 && position < items.size()) {
            items.set(position, updated);
            notifyItemChanged(position);
        }
    }

    /** Remove an item */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.addrating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        RateItem item = items.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // -------------------------------------------------------------------------
    class RatingViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvBadge;
        private final TextView tvRatingNum;
        private final TextView tvNotes;
        private final ChipGroup chipGroup;
        private final View btnEdit;
        private final View btnDelete;

        RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName      = itemView.findViewById(R.id.tvItemName);
            tvBadge     = itemView.findViewById(R.id.tvTypeBadge);
            tvRatingNum = itemView.findViewById(R.id.tvRatingNum);
            tvNotes     = itemView.findViewById(R.id.tvNotes);
            chipGroup   = itemView.findViewById(R.id.chipGroup);
            btnEdit     = itemView.findViewById(R.id.btnEdit);
            btnDelete   = itemView.findViewById(R.id.btnDelete);
        }

        void bind(RateItem item, int position) {
            // Name
            tvName.setText(item.getName());

            // Type badge
            if (item.getType() == RateItem.Type.RECIPE) {
                tvBadge.setText("- RECIPE");
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.blue_light));
            } else {
                tvBadge.setText("- INGREDIENT");
                tvBadge.setTextColor(ContextCompat.getColor(context, R.color.amber));
            }

            // Rating label
            tvRatingNum.setText("⭐ " + item.getRating() + " / 5");

            // Buttons
            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(item, getAdapterPosition());
            });
            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(item, getAdapterPosition());
            });
        }
    }
}