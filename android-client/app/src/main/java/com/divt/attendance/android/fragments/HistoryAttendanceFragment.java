package com.divt.attendance.android.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.adapters.AttendancesAdapter;
import com.divt.attendance.android.ifaces.IUIAdapterEvent;
import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.model.Outlet;
import com.divt.attendance.android.model.ResponseAPI;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */

public class HistoryAttendanceFragment extends NavigationFragment {
  public static final String TAG = HistoryAttendanceFragment.class.getSimpleName();

  private View mRoot = null;

  private RecyclerView mRecyclerView;
  private AttendancesAdapter mAdapter = null;
  private List<Attendance> mAttendances;

  public HistoryAttendanceFragment() {
    // Required empty public constructor
  }

  public static HistoryAttendanceFragment newInstance() {
    return new HistoryAttendanceFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);

    MainActivity.getHandler().post(this::getData);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mRoot = inflater.inflate(R.layout.fragment_history_attendance, container, false);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    Toolbar toolbar = mRoot.findViewById(R.id.toolbar_attendance);
    activity.setSupportActionBar(toolbar);

    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(getString(R.string.history_attendance));
      activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View view) {
    mAttendances = new ArrayList<>();

    IUIAdapterEvent event = (pos) -> {
      Log.d(TAG, "click: " + mAttendances.get(pos).getId());
      Attendance attendance = mAttendances.get(pos);

      navigateTo(Page.F_VIEW_PHOTO, attendance, true);
    };

    mRecyclerView = view.findViewById(R.id.list_attendances);

    // width recyclerView tidak berubah-ubah
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setRecycledViewPool(new RecyclerView.RecycledViewPool());
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

    mAdapter = new AttendancesAdapter(getActivity(), mAttendances, event);
    mRecyclerView.setAdapter(mAdapter);
  }

  private void getData() {
    fetchAttendances(getMainActv());
  }

  @SuppressLint("ApplySharedPref")
  private void fetchAttendances(
    @NonNull Context ctx
  ) {
    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "getAttendanceAll";
    queue.getCache().clear();

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    StringRequest stringReq = new StringRequest(Request.Method.GET, url,
        response -> {
          Log.d(TAG, "getAttendanceAll Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                List<Attendance> attendances = new ArrayList<>();

                JSONArray jsonArray = respJSON.getJSONArray("data");
                int count = jsonArray.length();
                for (int i = 0; i < count; i++) {
                  JSONObject json = jsonArray.getJSONObject(i);
                  Attendance attendance = Attendance.attendanceFromJson(json);
                  attendances.add(attendance);
                }

//                new AttendanceInFragment.DbSaveOutlet(AttendanceInFragment.this, outlets).execute();
                processAttendances(attendances);

                attendances.clear();
                break;
              case ENTRY_NOT_FOUND:
                break;
              case USER_NOT_FOUND:
                Utils.toastView(ctx, R.string.error_user_not_found);
                break;
              case WRONG_PASSWORD:
                Utils.toastView(ctx, R.string.error_incorrect_password);
                break;
              default:
                Utils.toastView(ctx, R.string.error_generic);
                break;
            }
          } catch (JSONException e) {
            e.printStackTrace();
          }
        },
        error -> Log.d(TAG, "Volley Error : " + error.getMessage())
    ) {
      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        headers.put("uid", Objects.requireNonNull(pref.getString("user_id", "")));
        headers.put("token", Objects.requireNonNull(pref.getString("sess", "")));

        headers.put("user", Objects.requireNonNull(pref.getString("user_id", "")));
        headers.put("first", String.valueOf(1));

        return headers;
      }
    };

    queue.add(stringReq);
  }

  private void processAttendances(List<Attendance> attendances) {
    mAttendances.clear();
    mAttendances.addAll(attendances);
    mAdapter.refresh();
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
