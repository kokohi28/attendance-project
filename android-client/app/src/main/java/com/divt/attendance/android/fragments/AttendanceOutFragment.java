package com.divt.attendance.android.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divt.attendance.android.Const;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.model.Outlet;
import com.divt.attendance.android.model.ResponseAPI;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */

public class AttendanceOutFragment extends NavigationFragment {
  public static final String TAG = AttendanceOutFragment.class.getSimpleName();

  private View mRoot = null;
  private LinearLayout mLlAttendance = null;
  private LinearLayout mLlNoAttendance = null;

  private Attendance mLastAttendance = null;

  public AttendanceOutFragment() {
    // Required empty public constructor
  }

  public static AttendanceOutFragment newInstance() {
    return new AttendanceOutFragment();
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

    mRoot = inflater.inflate(R.layout.fragment_attendance_out, container, false);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    Toolbar toolbar = mRoot.findViewById(R.id.toolbar_attendance);
    activity.setSupportActionBar(toolbar);

    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(getString(R.string.out_attendance));
      activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View view) {
    mLlNoAttendance = view.findViewById(R.id.ll_no_attendance);
    mLlAttendance = view.findViewById(R.id.ll_attendance);

    EditText etIncome = view.findViewById(R.id.et_income);
    Button btnLeave = view.findViewById(R.id.btn_leave);
    btnLeave.setOnClickListener(v -> {
      long income = Long.parseLong(etIncome.getText().toString());
      Log.d(TAG, "income:" + income + ", ref:" + mLastAttendance.getId() + ", shift:" + mLastAttendance.getShift());
      setLeave(getMainActv(), income, mLastAttendance.getOutletIdx(), mLastAttendance.getShift());
    });
  }

  private void getData() {
    fetchLastAttendance(getMainActv());
  }

  @SuppressLint("ApplySharedPref")
  private void fetchLastAttendance(
    @NonNull Context ctx
  ) {
    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "getLatestAttendanceIn";
    queue.getCache().clear();

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    StringRequest stringReq = new StringRequest(Request.Method.GET, url,
        response -> {
          Log.d(TAG, "getLatestAttendanceIn Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                mLastAttendance = Attendance.attendanceFromJson(respJSON.getJSONObject("data"));

                mLlAttendance.setVisibility(View.VISIBLE);
                mLlNoAttendance.setVisibility(View.GONE);
                break;
              case ENTRY_NOT_FOUND:
                mLlAttendance.setVisibility(View.GONE);
                mLlNoAttendance.setVisibility(View.VISIBLE);
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

        return headers;
      }
    };

    queue.add(stringReq);
  }

  @SuppressLint("ApplySharedPref")
  private void setLeave(
    @NonNull Context ctx,
    long income,
    long outlet,
    int shift
  ) {
    if (mLastAttendance == null) return;

    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "setAttendanceOut";

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    JSONObject leaveJson = new JSONObject();
    try {
      leaveJson.put("type", Const.ATTENDANCE_OUT);
      leaveJson.put("ref", mLastAttendance.getId());
      leaveJson.put("shift", shift);
      leaveJson.put("outlet", outlet);
      leaveJson.put("income", income);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    StringRequest stringReq = new StringRequest(Request.Method.POST, url,
        response -> {
          Log.d(TAG, "setAttendanceOut Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                Utils.toastView(ctx, getString(R.string.success));
                finishLeave();
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
      public byte[] getBody() {
        return leaveJson.toString().getBytes();
      }

      @Override
      public String getBodyContentType() {
        return "application/json";
      }

      @Override
      public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        headers.put("uid", Objects.requireNonNull(pref.getString("user_id", "")));
        headers.put("token", Objects.requireNonNull(pref.getString("sess", "")));

        return headers;
      }
    };

    queue.add(stringReq);
  }

  private void finishLeave() {
    ((MainActivity) getActivity()).popFragment();
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
