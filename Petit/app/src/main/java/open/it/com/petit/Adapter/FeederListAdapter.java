package open.it.com.petit.Adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import open.it.com.petit.Activity.PetitFeedHistory;
import open.it.com.petit.Activity.PetitMediaActivity;
import open.it.com.petit.Activity.PetitReserveActivity;
import open.it.com.petit.Model.Feeder;
import open.it.com.petit.Popup.PetitFeederSettingPopup;
import open.it.com.petit.R;

/**
 * Created by user on 2017-05-23.
 */

public class FeederListAdapter extends RecyclerView.Adapter<FeederListAdapter.ViewHolder> {
    public static final String TAG = "FeederListAdapter";
    private Context context;
    private ArrayList<Feeder> petList;
    private GradientDrawable drawable;
    private Typeface face;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView petNameTV;
        public TextView petNameTmpTV;
        public FrameLayout mediaBtn;
        public LinearLayout reserveBtn;
        public LinearLayout historyBtn;
        public ImageView feederBtn;
        public ImageView settingBtn;

        public ViewHolder(View v) {
            super(v);
            petNameTV = (TextView) v.findViewById(R.id.tv_pfm_list_view_petname);
            petNameTmpTV = (TextView) v.findViewById(R.id.tv_feeder_tmp);
            mediaBtn = (FrameLayout) v.findViewById(R.id.btn_goto_petit_media);
            reserveBtn = (LinearLayout) v.findViewById(R.id.btn_pfm_reserve);
            historyBtn = (LinearLayout) v.findViewById(R.id.btn_pfm_feed_history);
            feederBtn = (ImageView) v.findViewById(R.id.img_feeder_list_media);
            settingBtn = (ImageView) v.findViewById(R.id.btn_feeder_setting);
        }
    }

    public FeederListAdapter(ArrayList petList, Context mContext) {
        this.petList = petList;
        this.context = mContext;
        this.face = Typeface.createFromAsset(mContext.getAssets(), "NanumBarunGothic.ttf");
        //this.drawable = (GradientDrawable)context.getDrawable(R.drawable.feeder_list_mediabtn);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_feeder_list_view,parent,false);

        FeederListAdapter.ViewHolder vh=new FeederListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Glide.with(context).load(petList.get(position).getF_IMG()).into(holder.feederBtn);
        holder.petNameTV.setText(petList.get(position).getP_NAME());

        Log.d(TAG, "ms :: " + petList.get(position).getMS());
        //Slave Phone
        if (petList.get(position).getMS() == 0)
            holder.reserveBtn.setEnabled(false);
        else
            holder.reserveBtn.setEnabled(true);

        holder.mediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PetitMediaActivity.class);
                intent.putExtra("GUID", petList.get(position).getGUID());
                context.startActivity(intent);
            }
        });

        holder.reserveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PetitReserveActivity.class);
                intent.putExtra("petname", petList.get(position).getP_NAME());
                intent.putExtra("GUID", petList.get(position).getGUID());
                context.startActivity(intent);
            }
        });

        holder.historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PetitFeedHistory.class);
                intent.putExtra("petname", petList.get(position).getP_NAME());
                intent.putExtra("GUID", petList.get(position).getGUID());
                context.startActivity(intent);
            }
        });

        holder.settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PetitFeederSettingPopup.class);
                intent.putExtra("GUID", petList.get(position).getGUID());
                intent.putExtra("PW", petList.get(position).getPW());
                intent.putExtra("MS", petList.get(position).getMS());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }
}
