package com.example.finalproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    private final LayoutInflater inflater;

    public RestaurantAdapter(@NonNull Context context, @NonNull List<Restaurant> restaurants) {
        super(context, 0, restaurants);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.row_restaurant, parent, false);
            holder = new ViewHolder();
            holder.textName = convertView.findViewById(R.id.textName);
            holder.textTags = convertView.findViewById(R.id.textTags);
            holder.ratingBar = convertView.findViewById(R.id.ratingBarSmall);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Restaurant restaurant = getItem(position);
        if (restaurant != null) {
            holder.textName.setText(restaurant.getName());
            String tags = restaurant.getTags();
            if (tags == null || tags.trim().isEmpty()) {
                holder.textTags.setText("No tags");
            } else {
                holder.textTags.setText(tags);
            }
            holder.ratingBar.setRating(restaurant.getRating());
        }

        return convertView;
    }

    static class ViewHolder {
        TextView textName;
        TextView textTags;
        RatingBar ratingBar;
    }
}