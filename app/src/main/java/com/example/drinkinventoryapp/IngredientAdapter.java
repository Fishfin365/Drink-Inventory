package com.example.drinkinventoryapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredientList;
    private List<Ingredient> ingredientListFull;
    private boolean isEditMode = false;
    private boolean isDeleteMode = false;
    private OnIngredientActionListener actionListener;

    public interface OnIngredientActionListener {
        void onDelete(Ingredient item);
    }

    public IngredientAdapter(List<Ingredient> ingredientList, OnIngredientActionListener listener) {
        this.ingredientList = new ArrayList<>(ingredientList);
        this.ingredientListFull = new ArrayList<>(ingredientList);
        this.actionListener = listener;
    }

    public List<Ingredient> getAllItems() {
        return ingredientListFull;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
        if (editMode) isDeleteMode = false;
        notifyDataSetChanged();
    }

    public void setDeleteMode(boolean deleteMode) {
        this.isDeleteMode = deleteMode;
        if (deleteMode) isEditMode = false;
        notifyDataSetChanged();
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    public void filter(String query) {
        ingredientList.clear();
        if (query.isEmpty()) {
            ingredientList.addAll(ingredientListFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Ingredient item : ingredientListFull) {
                if (item.getName() != null && item.getName().toLowerCase().contains(lowerCaseQuery)) {
                    ingredientList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void addItem(Ingredient item) {
        ingredientListFull.add(item);
        ingredientList.add(item);
        notifyItemInserted(ingredientList.size() - 1);
    }

    public void setList(List<Ingredient> newList) {
        this.ingredientList = new ArrayList<>(newList);
        this.ingredientListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.nameText.setText(ingredient.getName());

        if (holder.textWatcher != null) {
            holder.volumeEdit.removeTextChangedListener(holder.textWatcher);
        }

        if (isEditMode) {
            holder.volumeText.setVisibility(View.GONE);
            holder.volumeEdit.setVisibility(View.VISIBLE);
            holder.btnRemove.setVisibility(View.GONE);
            
            holder.volumeEdit.setText(ingredient.getVolume());
            holder.textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ingredient.setVolume(s.toString());
                }
                @Override
                public void afterTextChanged(Editable s) {}
            };
            holder.volumeEdit.addTextChangedListener(holder.textWatcher);
        } else if (isDeleteMode) {
            holder.volumeEdit.setVisibility(View.GONE);
            holder.volumeText.setVisibility(View.VISIBLE);
            holder.volumeText.setText("Volume: " + ingredient.getVolume());
            holder.btnRemove.setVisibility(View.VISIBLE);
            
            holder.btnRemove.setOnClickListener(v -> {
                int adapterPos = holder.getAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    Ingredient removedItem = ingredientList.get(adapterPos);
                    ingredientList.remove(adapterPos);
                    ingredientListFull.remove(removedItem);
                    notifyItemRemoved(adapterPos);
                    if (actionListener != null) {
                        actionListener.onDelete(removedItem);
                    }
                }
            });
        } else {
            holder.volumeEdit.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
            holder.volumeText.setVisibility(View.VISIBLE);
            holder.volumeText.setText("Volume: " + ingredient.getVolume());
        }
    }

    @Override
    public int getItemCount() {
        return ingredientList != null ? ingredientList.size() : 0;
    }

    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, volumeText;
        EditText volumeEdit;
        ImageButton btnRemove;
        TextWatcher textWatcher;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.ingredient_name);
            volumeText = itemView.findViewById(R.id.ingredient_volume);
            volumeEdit = itemView.findViewById(R.id.ingredient_volume_edit);
            btnRemove = itemView.findViewById(R.id.btn_remove_item);
        }
    }
}
