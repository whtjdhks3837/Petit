package open.it.com.petit.Adapter;


import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import open.it.com.petit.Handler.ReserveDBHandler;
import open.it.com.petit.Model.Time;
import open.it.com.petit.R;

/**
 * Created by user on 2017-05-25.
 */

public class FeederReserveTimeAdapter extends RecyclerView.Adapter<FeederReserveTimeAdapter.ViewHolder> {
    public static final String TAG = "FeederReserveTimeAdapter";

    private ReserveDBHandler openHelper;
    private ArrayList<Time> timeDatas;
    private Handler handler;
    private Context context;

    private static final String dbFileName = "petit_reserve.db";
    private static final String timeTableName = "TB_MACHINE_TIME";
    private static final String weekTableName = "TB_MACHINE_WEEK";

    public FeederReserveTimeAdapter(ArrayList timeDatas, Context context, Handler handler) {
        this.timeDatas = timeDatas;
        this.context = context;
        this.handler = handler;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_pfm_reserve_amORpm;
        private TextView tv_pfm_reserve_time;
        private TextView tv_pfm_reserve_provisions;
        private ImageView btn_pfm_reserve_time_del;

        public ViewHolder(View v) {
            super(v);
            tv_pfm_reserve_amORpm = (TextView) v.findViewById(R.id.tv_pfm_reserve_amORpm);
            tv_pfm_reserve_time = (TextView) v.findViewById(R.id.tv_pfm_reserve_time);
            tv_pfm_reserve_provisions = (TextView)  v.findViewById(R.id.tv_pfm_reserve_provisions);
            btn_pfm_reserve_time_del = (ImageView) v.findViewById(R.id.btn_pfm_reserve_time_del);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_reserve_timedata,parent,false);

        FeederReserveTimeAdapter.ViewHolder vh=new FeederReserveTimeAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int amORpm = Integer.valueOf(timeDatas.get(position).getMT_TIME());
        holder.tv_pfm_reserve_provisions.setText(String.valueOf(timeDatas.get(position).getMT_AMOUNT()));

        if (amORpm <= 12) {
            holder.tv_pfm_reserve_time.setText(String.valueOf(amORpm));
            holder.tv_pfm_reserve_amORpm.setText("오전 ");
        } else if (amORpm > 12) {
            amORpm -= 12;
            holder.tv_pfm_reserve_time.setText(String.valueOf(amORpm));
            holder.tv_pfm_reserve_amORpm.setText("오후 ");
        }

        holder.btn_pfm_reserve_time_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHelper = new ReserveDBHandler(context, dbFileName, null, 1, handler);
                openHelper.deleteTimeData(timeDatas.get(position).getMT_ID(), true);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (timeDatas != null) {
            return timeDatas.size();
        } else {
            return 0;
        }
    }

}
