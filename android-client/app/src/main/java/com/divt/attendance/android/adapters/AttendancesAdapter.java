package com.divt.attendance.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.divt.attendance.android.Const;
import com.divt.attendance.android.R;
import com.divt.attendance.android.ifaces.IUIAdapterEvent;
import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.utils.Utils;

import java.text.MessageFormat;
import java.util.List;

public class AttendancesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private static final String TAG = AttendancesAdapter.class.getSimpleName();

  private Context mContext;
  private final List<Attendance> mAttendances;
  private final IUIAdapterEvent mBridge;

  public AttendancesAdapter(Context context, List<Attendance> attendances, IUIAdapterEvent event) {
    mContext = context;
    mAttendances = attendances;
    mBridge = event;
  }

  @Override
  public int getItemCount() {
    return mAttendances.size();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();

    final View viewItem = LayoutInflater.from(context).inflate(R.layout.item_attendance, parent, false);
    ItemAttendance item = new ItemAttendance(viewItem);
    item.rlItem.setOnClickListener(v -> mBridge.onClick((int) v.getTag()));

    return item;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    final Attendance attendance = mAttendances.get(position);

    ItemAttendance vh = (ItemAttendance) holder;

    if (attendance.getType() == Const.ATTENDANCE_IN) {
      vh.tvRef.setText(MessageFormat.format("#{0}", attendance.getId()));
      vh.ivType.setImageResource(R.drawable.ic_baseline_login_24);
    } else {
      vh.tvRef.setText(MessageFormat.format("#{0}", attendance.getRef()));
      vh.ivType.setImageResource(R.drawable.ic_baseline_outbond_24);
    }

    String outlet = attendance.getOutletName() + " • " + attendance.getOutletArea();
    vh.tvOutlet.setText(outlet);

    String info = attendance.getUserName() + " [" + Utils.getTimeAndDate(attendance.getCreatedAt()) + "]";
    vh.tvInfo.setText(info);

    vh.rlItem.setTag(position);
  }

  public void refresh() {
    notifyDataSetChanged();
  }

  private class ItemAttendance extends RecyclerView.ViewHolder {
    private RelativeLayout rlItem;
    private TextView tvRef;
    private ImageView ivType;
    private TextView tvOutlet;
    private TextView tvInfo;

    public ItemAttendance(View view) {
      super(view);

      rlItem = view.findViewById(R.id.v_touch);
      ivType = view.findViewById(R.id.iv_type_attendance);
      tvRef = view.findViewById(R.id.tv_ref);
      tvOutlet = view.findViewById(R.id.tv_outlet);
      tvInfo = view.findViewById(R.id.tv_info);
    }
  }
}
