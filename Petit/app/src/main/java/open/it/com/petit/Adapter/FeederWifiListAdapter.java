package open.it.com.petit.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import open.it.com.petit.Popup.PetitFeederAddWifiPwPopup;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-13.
 */

public class FeederWifiListAdapter extends RecyclerView.Adapter<FeederWifiListAdapter.ViewHolder> {
    private ArrayList<ScanResult> list;
    private Context context;
    private ScanResult ap;

    public FeederWifiListAdapter(Context context, ArrayList list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_wifi;

        public ViewHolder(View v) {
            super(v);
            tv_wifi = (TextView) v.findViewById(R.id.tv_wifi);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_feeder_wifi_list,parent,false);

        FeederWifiListAdapter.ViewHolder vh = new FeederWifiListAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.tv_wifi.setText(list.get(position).SSID);
        holder.tv_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ap = list.get(position);
                Intent intent = new Intent(context, PetitFeederAddWifiPwPopup.class);
                intent.putExtra("ap", ap);
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });
    }

    @Override
    public int getItemCount() {
       if (list != null) {
           return list.size();
       } else {
           return 0;
       }
    }


}
