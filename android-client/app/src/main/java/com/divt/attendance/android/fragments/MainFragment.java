package com.divt.attendance.android.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.utils.GeneralStorage;
import com.divt.attendance.android.utils.Log;

import java.text.MessageFormat;

public class MainFragment extends NavigationFragment {

  public static final String TAG = MainFragment.class.getSimpleName();

  private View mRoot = null;
  private SharedPreferences mPref;

  private static final int RC_CONSTRAINT = 100;

  public MainFragment() {
    // Required empty public constructor
  }

  public static MainFragment newInstance() {
    return new MainFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @SuppressLint("ResourceType")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d(TAG, ">>>>>>>> onCreateView:" + TAG);

    mRoot = inflater.inflate(R.layout.fragment_main, container, false);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    Toolbar toolbar = mRoot.findViewById(R.id.toolbar_main);
    activity.setSupportActionBar(toolbar);

    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(getString(R.string.app_name));
      activity.getSupportActionBar().setDisplayShowHomeEnabled(false);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View view) {
    Button btnAttendanceIn = view.findViewById(R.id.btn_attendance_in);
    btnAttendanceIn.setOnClickListener(v -> navigateTo(Page.F_ATTENDANCE_IN));

    Button btnAttendanceOut = view.findViewById(R.id.btn_attendance_out);
    btnAttendanceOut.setOnClickListener(v -> navigateTo(Page.F_ATTENDANCE_OUT));

    Button btnHistoryAttendance = view.findViewById(R.id.btn_history_attendance);
    btnHistoryAttendance.setOnClickListener(v -> navigateTo(Page.F_HISTORY_ATTENDANCE));

    // Login info
    String info = MessageFormat.format("• {0} login sebagai {1} •",
      mPref.getString("name", ""),
      mPref.getString("user_type_str", "")
    );
    TextView tvInfo = view.findViewById(R.id.tv_info);
    tvInfo.setText(info);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.clear();
    inflater.inflate(R.menu.menu_home, menu);
  }

  @SuppressLint("ApplySharedPref")
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, ">>>>>>>> onOptionsItemSelected:" + item.getItemId());

    if (!((MainActivity) getActivity()).getTopFragmentTag().equals(TAG)) {
      Log.d(TAG, ">>>>>>>> onOptionsItemSelected NOT THIS FRAGMENT");
      return super.onOptionsItemSelected(item);
    }

    if (item.getItemId() == R.id.action_logout) {
      SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
      SharedPreferences.Editor edit = pref.edit();
      edit.remove("user_id");
      edit.remove("sess");
      edit.remove("name");
      edit.remove("bio");
      edit.remove("user_type");
      edit.remove("user_type_str");
      edit.commit();

      final GeneralStorage gs = new GeneralStorage(getMainActv());
      gs.deleteAllData();

      ((MainActivity) getActivity()).popFragment();
      MainActivity.getHandler().postDelayed(() -> navigateTo(Page.F_LOGIN), 500);
      return true;
    }

    // If we got here, the user's action was not recognized.
    // Invoke the superclass to handle it.
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult(MainFragment): " + requestCode + " " + resultCode);

    super.onActivityResult(requestCode, resultCode, data);
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