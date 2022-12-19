package com.gibbonstudio.myapplication.Adapters;


import static com.gibbonstudio.myapplication.ObjectVariables.SHOW_HIDE_APP_DRAWER;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gibbonstudio.myapplication.Models.AppItem;
import com.gibbonstudio.myapplication.R;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {

    private final LayoutInflater inflater;
    private final List<AppItem> states;
    private final Context mContext;

    public AppsAdapter(Context context, List<AppItem> states) {
        this.inflater = LayoutInflater.from(context);
        this.states = states;
        this.mContext = context;
    }

    @NonNull
    @Override
    public AppsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.app_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppsAdapter.ViewHolder holder, int position) {

        AppItem item = states.get(position);

        holder.appIconImage.setImageDrawable(item.getIcon());
        holder.appTitle.setText(item.getName());
        holder.appCard.setOnClickListener(
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
        return states.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final RoundedImageView appIconImage;
        final CardView appCard;
        final TextView appTitle;

        ViewHolder(View view) {
            super(view);
            appIconImage = view.findViewById(R.id.appIconImage);
            appCard = view.findViewById(R.id.appCard);
            appTitle = view.findViewById(R.id.appTitle);
        }
    }

}
