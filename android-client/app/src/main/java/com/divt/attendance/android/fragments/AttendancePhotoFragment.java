package com.divt.attendance.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.divt.attendance.android.Const;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.utils.Log;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * A simple {@link Fragment} subclass.
 */

public class AttendancePhotoFragment extends NavigationFragment {
  public static final String TAG = AttendancePhotoFragment.class.getSimpleName();

  private View mRoot = null;

  private Attendance mAttendance = null;

  public AttendancePhotoFragment() {
    // Required empty public constructor
  }

  public static AttendancePhotoFragment newInstance() {
    return new AttendancePhotoFragment();
  }

  public static AttendancePhotoFragment newInstance(Object data) {
    AttendancePhotoFragment fragment = new AttendancePhotoFragment();
    fragment.mAttendance = (Attendance) data;
    return fragment;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mRoot = inflater.inflate(R.layout.fragment_attendance_photo, container, false);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    Toolbar toolbar = mRoot.findViewById(R.id.toolbar_attendance);
    activity.setSupportActionBar(toolbar);

    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(R.string.attendance_photo);
      activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View view) {
    TextView tvIncome = view.findViewById(R.id.tv_income_val);
    tvIncome.setText(MessageFormat.format("Rp. {0}", mAttendance.getIncome()));

    ImageView ivAttendance = view.findViewById(R.id.iv_attendance);
    Date date = new Date(mAttendance.getCreatedAt());
    Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH) + 1; // Add one to month {0 - 11}
    int day = calendar.get(Calendar.DAY_OF_MONTH);

    String url = MainActivity.getEndPoint(getMainActv()) +
        MessageFormat.format("getInPhoto?d={0}-{1}-{2}&o={3}&t={4}&s={5}",
            String.valueOf(year),
            String.valueOf(month),
            String.valueOf(day),
            mAttendance.getOutletIdx(),
            String.valueOf(Const.ATTENDANCE_IN),
            mAttendance.getShift());

    Glide.with(getActivity())
        .load(url)
        .apply(new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .dontAnimate())
        .into(ivAttendance);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.clear();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, ">>>>>>>> onOptionsItemSelected:" + item.getItemId());

    if (!((MainActivity) getActivity()).getTopFragmentTag().equals(TAG)) {
      Log.d(TAG, ">>>>>>>> onOptionsItemSelected NOT THIS FRAGMENT");
      return super.onOptionsItemSelected(item);
    }

    if (item.getItemId() == android.R.id.home) {
      ((MainActivity) getActivity()).popFragment();
      return true;
    }

    // If we got here, the user's action was not recognized.
    // Invoke the superclass to handle it.
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onStart() {
    super.onStart();
    Log.d(TAG, ">>>>>>>> onStart:" + TAG);
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, ">>>>>>>> onPause:" + TAG);
  }

  @Override
  public void onStop() {
    super.onStop();

    Log.d(TAG, ">>>>>>>> onStop:" + TAG);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, ">>>>>>>> onDestroy:" + TAG);

    mRoot = null;
  }
}
