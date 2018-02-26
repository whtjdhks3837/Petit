package open.it.com.petit.Holder;

import android.util.Log;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import open.it.com.petit.R;

import static android.view.animation.Animation.ABSOLUTE;
import static android.view.animation.Animation.RELATIVE_TO_PARENT;
import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by user on 2017-07-21.
 */

public class FeederHistoryParentHolder extends GroupViewHolder {
    private TextView history;
    private ImageView arrow;

    public FeederHistoryParentHolder(View itemView) {
        super(itemView);
        history = (TextView) itemView.findViewById(R.id.tv_pfm_feed_history);
        arrow = (ImageView) itemView.findViewById(R.id.btn_pfm_feed_history);
    }

    public void setHistoryTitle(String historyText) {
        history.setText(historyText);
    }

    @Override
    public void expand() {
        Log.d("tag", "expand");
        //animateExpand();
        arrow.setImageResource(R.drawable.open_list);
    }

    @Override
    public void collapse() {
        Log.d("tag", "collapse");
        //animateCollapse();
        arrow.setImageResource(R.drawable.close_list02);
    }

    private void animateExpand() {
        RotateAnimation rotate =
                new RotateAnimation(180, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate =
                new RotateAnimation(180, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        arrow.setAnimation(rotate);
    }
}
