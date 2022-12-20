package com.gibbonstudio.myapplication.Adapters;


import static com.gibbonstudio.myapplication.ObjectVariables.NOTIFICATION_POST;
import static com.gibbonstudio.myapplication.ObjectVariables.SHOW_HIDE_APP_DRAWER;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gibbonstudio.myapplication.Models.AppItem;
import com.gibbonstudio.myapplication.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<AppItem> appList;
    private final Context mContext;
    private final boolean smallIcon;

    public AppsAdapter(Context context, List<AppItem> states, boolean smallIcon) {
        this.inflater = LayoutInflater.from(context);
        this.appList = states;
        this.mContext = context;
        this.smallIcon = smallIcon;
    }

    @NonNull
    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (smallIcon) {
            return new ViewHolder(inflater.inflate(R.layout.fast_app_item, parent, false));
        }
        return new ViewHolder(inflater.inflate(R.layout.app_item, parent, false));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull AppsAdapter.ViewHolder holder, int position) {


        FrameLayout flAppCard = holder.itemView.findViewById(R.id.flAppCard);
        RoundedImageView appIconImage = holder.itemView.findViewById(R.id.appIconImage);

        AppItem item = appList.get(position);
        if (smallIcon) {
            appIconImage.setImageDrawable(item.getIcon());
        } else {
            final TextView appTitle = holder.itemView.findViewById(R.id.appTitle);
            appIconImage.setImageDrawable(item.getIcon());
            appTitle.setText(item.getName());
            flAppCard.setOnLongClickListener(v -> {
                Intent intent = new Intent(SHOW_HIDE_APP_DRAWER);
                intent.putExtra("add_fast_app", position);
                mContext.sendBroadcast(intent);
                mContext.sendBroadcast(new Intent(SHOW_HIDE_APP_DRAWER));
                return false;
            });
        }

        flAppCard.setOnClickListener(
                view -> {
                    Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(String.valueOf(item.getPackageName()));
                    if (launchIntent != null) {
                        mContext.startActivity(launchIntent);
                        mContext.sendBroadcast(new Intent(SHOW_HIDE_APP_DRAWER));
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

}
