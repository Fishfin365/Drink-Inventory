
package com.example.drinkinventoryapp;
import java.util.ArrayList;
import java.util.List;

    public class RateItem {

        public enum Type {
            INGREDIENT, RECIPE
        }

        private long id;
        private String name;
        private Type type;
        private int rating;          // 1–5
        private String notes;
        private List<String> tags;
        private long createdAt;      // epoch ms
        private long updatedAt;

        public RateItem() {
            this.tags = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = System.currentTimeMillis();
        }

        public RateItem(String name, Type type, int rating, String notes, List<String> tags) {
            this.name = name;
            this.type = type;
            this.rating = rating;
            this.notes = notes;
            this.tags = tags != null ? tags : new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = System.currentTimeMillis();
        }

        // --- Getters & Setters ---

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Type getType() { return type; }
        public void setType(Type type) { this.type = type; }

        public int getRating() { return rating; }
        public void setRating(int rating) {
            if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating must be 1–5");
            this.rating = rating;
        }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags != null ? tags : new ArrayList<>(); }

        public long getCreatedAt() { return createdAt; }
        public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

        public long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

        /** Convenience: comma-joined tag string for the edit dialog */
        public String getTagsAsString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tags.size(); i++) {
                sb.append(tags.get(i));
                if (i < tags.size() - 1) sb.append(", ");
            }
            return sb.toString();
        }

        /** Parse a comma-separated string back into a tag list */
        public static List<String> parseTags(String raw) {
            List<String> result = new ArrayList<>();
            if (raw == null || raw.trim().isEmpty()) return result;
            for (String t : raw.split(",")) {
                String trimmed = t.trim();
                if (!trimmed.isEmpty()) result.add(trimmed);
            }
            return result;
        }
    }
