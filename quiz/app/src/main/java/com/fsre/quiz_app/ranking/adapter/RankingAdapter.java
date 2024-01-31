package com.fsre.quiz_app.ranking.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fsre.quiz_app.R;
import com.fsre.quiz_app.models.User;

import java.util.List;
public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<User> userList;
    private Context ctx;
    public ImageView pointsIcon;

    public RankingAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.ctx = parent.getContext();
        View view = LayoutInflater.from(this.ctx).inflate(R.layout.ranking_list_item, parent, false);
        pointsIcon = view.findViewById(R.id.ranking_item_points_icon);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        User model = userList.get(position);
        // Check the position to apply different styles
        if (position == 0) {
            applyFirstItemStyle(holder);
        } else if (position == 1) {
            applySecondItemStyle(holder);
        }
        else if (position == 2){
            applyThirdItemStyle(holder);
        }
        holder.position.setText(String.valueOf(position + 1));
        holder.username.setText(model.name);
        holder.points.setText(String.valueOf(model.points));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView position, username, points;

        RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            this.position = itemView.findViewById(R.id.ranking_item_position);
            this.username = itemView.findViewById(R.id.ranking_item_username);
            this.points = itemView.findViewById(R.id.ranking_item_points);
        }
    }

    private void applyFirstItemStyle(RankingViewHolder holder) {
        holder.itemView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.white));
        holder.itemView.setMinimumHeight(120);
        holder.username.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.points.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.position.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        pointsIcon.setImageResource(R.drawable.star_black);
    }

    private void applySecondItemStyle(RankingViewHolder holder) {
        holder.itemView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.purple));
        holder.username.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.points.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.position.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        pointsIcon.setImageResource(R.drawable.star_black);
    }

    private void applyThirdItemStyle(RankingViewHolder holder) {
        holder.itemView.setBackgroundColor(ContextCompat.getColor(ctx, R.color.blue));
        holder.username.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.points.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        holder.position.setTextColor(ContextCompat.getColor(ctx, R.color.black));
        pointsIcon.setImageResource(R.drawable.star_black);
    }
}
