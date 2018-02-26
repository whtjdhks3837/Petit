package open.it.com.petit.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

import open.it.com.petit.Holder.FeederHistoryChildHolder;
import open.it.com.petit.Holder.FeederHistoryParentHolder;
import open.it.com.petit.Model.RHChild;
import open.it.com.petit.Model.RHParent;
import open.it.com.petit.R;

/**
 * Created by user on 2017-07-21.
 */

public class FeederReserveHistoryAdapter
        extends ExpandableRecyclerViewAdapter<FeederHistoryParentHolder, FeederHistoryChildHolder> {


    public FeederReserveHistoryAdapter(List<? extends ExpandableGroup> groups) {
        super(groups);
    }

    @Override
    public FeederHistoryParentHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_reserve_history_parent_data, parent, false);
        return new FeederHistoryParentHolder(view);
    }

    @Override
    public FeederHistoryChildHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.petit_reserve_history_child_data, parent, false);
        return new FeederHistoryChildHolder(view);
    }

    @Override
    public void onBindChildViewHolder(FeederHistoryChildHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final RHChild child = ((RHParent) group).getItems().get(childIndex);
        Log.d("tag", child.getContent());
        holder.setHistoryChild(child.getContent());
    }

    @Override
    public void onBindGroupViewHolder(FeederHistoryParentHolder holder, int flatPosition, ExpandableGroup group) {
        Log.d("tag", group.getTitle());
        holder.setHistoryTitle(group.getTitle());
    }
}
