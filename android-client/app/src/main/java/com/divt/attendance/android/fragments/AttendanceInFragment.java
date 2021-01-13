package com.divt.attendance.android.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.SimpleMultiPartRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.divt.attendance.android.Const;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.model.Attendance;
import com.divt.attendance.android.model.Outlet;
import com.divt.attendance.android.model.ResponseAPI;
import com.divt.attendance.android.utils.DBParser;
import com.divt.attendance.android.utils.GeneralStorage;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */

public class AttendanceInFragment extends NavigationFragment {
  public static final String TAG = AttendanceInFragment.class.getSimpleName();

  private View mRoot = null;
  private ImageView mIvAttendance = null;
  private TextView mFileInfo = null;
  private LinearLayout mLlAttendance = null;
  private LinearLayout mLlNoAttendance = null;

  private Attendance mLastAttendance = null;
  private String mFileAttendance = null;

  private List<Outlet> mOutlets = null;
  private List<String> mOutletNames = null;
  private ArrayAdapter<String> mSpOutletAdapter = null;

  public AttendanceInFragment() {
    // Required empty public constructor
  }

  public static AttendanceInFragment newInstance() {
    return new AttendanceInFragment();
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

    mRoot = inflater.inflate(R.layout.fragment_attendance_in, container, false);

    AppCompatActivity activity = (AppCompatActivity) getActivity();
    Toolbar toolbar = mRoot.findViewById(R.id.toolbar_attendance);
    activity.setSupportActionBar(toolbar);

    if (activity.getSupportActionBar() != null) {
      activity.getSupportActionBar().setTitle(getString(R.string.in_attendance));
      activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View view) {
    mLlNoAttendance = view.findViewById(R.id.ll_no_attendance);
    mLlAttendance = view.findViewById(R.id.ll_attendance);

    mFileInfo = view.findViewById(R.id.tv_file_info);
    mIvAttendance = view.findViewById(R.id.iv_attendance);

    Spinner spShift = view.findViewById(R.id.sp_shift);
    Spinner spOutlet = view.findViewById(R.id.sp_outlet);

    // Spinner
    mOutlets = new ArrayList<>();
    mOutletNames = new ArrayList<>();
    mSpOutletAdapter = new ArrayAdapter<>(getActivity(), R.layout.item_spinner, R.id.tv_item, mOutletNames);
    spOutlet.setAdapter(mSpOutletAdapter);

    spOutlet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "outlet: " + position);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected");
      }
    });

    Button btnTakePhoto = view.findViewById(R.id.btn_take_picture);
    btnTakePhoto.setOnClickListener(v -> {
      Bundle bundle = new Bundle();
      bundle.putString(Const.PAGE, TAG);
      navigateTo(Page.F_TAKE_PHOTO, bundle);
    });

    Button btnAttendance = view.findViewById(R.id.btn_attendance);
    btnAttendance.setOnClickListener(v -> {
      btnAttendance.setEnabled(false);
      btnTakePhoto.setEnabled(false);
      spOutlet.setEnabled(false);
      spShift.setEnabled(false);

      if (TextUtils.isEmpty(mFileAttendance)) {
        Utils.toastView(getMainActv(), getString(R.string.fill_req_field));
        btnAttendance.setEnabled(true);
        btnTakePhoto.setEnabled(true);
        spOutlet.setEnabled(true);
        spShift.setEnabled(true);

        return;
      }

      int shiftPos = spShift.getSelectedItemPosition();
      int outletPos = spOutlet.getSelectedItemPosition();
      long outlet = mOutlets.get(outletPos).getIdx();
      Log.d(TAG, "pos, shift: " + shiftPos + ", outlet-[id]:" + outletPos + "-[" + outlet + "]");
      uploadInPhoto(getMainActv(), mFileAttendance, outlet, shiftPos);
    });

    MainActivity.getHandler().post(this::getOutlet);
  }

  public void processCapturedPhoto() {
    File f = new File(MainActivity.getAppCacheDir() + "/compressed.jpg");
    if (f.exists()) {
      mFileAttendance = f.getAbsolutePath();
      mFileInfo.setText(R.string.attendance_photo_ready);
      Glide.with(getMainActv())
          .load(f.getAbsolutePath())
          .apply(new RequestOptions()
              .diskCacheStrategy(DiskCacheStrategy.NONE)
              .skipMemoryCache(true)
              .dontAnimate())
          .into(mIvAttendance);
      Log.e(TAG, "File OK:" + f.getAbsolutePath());
    } else {
      Log.e(TAG, "CAPTURED PHOTO NOT EXISTS!!!!");
    }
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

                mLlAttendance.setVisibility(View.GONE);
                mLlNoAttendance.setVisibility(View.VISIBLE);
                break;
              case ENTRY_NOT_FOUND:
                mLlAttendance.setVisibility(View.VISIBLE);
                mLlNoAttendance.setVisibility(View.GONE);
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

  private void getOutlet() {
    final GeneralStorage gs = new GeneralStorage(getMainActv());
    List<Outlet> outlets = DBParser.parseOutlets(gs.getAllOutlet());
    if (outlets.size() > 0) {
      processOutlets(outlets);
      outlets.clear();
    } else {
      fetchOutlet(getMainActv());
    }
  }

  @SuppressLint("ApplySharedPref")
  private void fetchOutlet(
    @NonNull Context ctx
  ) {
    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "getOutlets";
    queue.getCache().clear();

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    StringRequest stringReq = new StringRequest(Request.Method.GET, url,
        response -> {
          Log.d(TAG, "getOutlets Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                Utils.toastView(ctx, getString(R.string.success_present));

                List<Outlet> outlets = new ArrayList<>();

                JSONArray jsonArray = respJSON.getJSONArray("data");
                int count = jsonArray.length();
                for (int i = 0; i < count; i++) {
                  JSONObject json = jsonArray.getJSONObject(i);
                  Outlet outlet = Outlet.outletFromJson(json);
                  outlets.add(outlet);
                }

                new DbSaveOutlet(AttendanceInFragment.this, outlets).execute();
                processOutlets(outlets);

                outlets.clear();
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

        return headers;
      }
    };

    queue.add(stringReq);
  }

  private void processOutlets(List<Outlet> outlets) {
    mOutletNames.clear();
    mOutlets.clear();

    mOutlets.addAll(outlets);
    for (Outlet o: outlets) {
      mOutletNames.add(MessageFormat.format("{0} - {1}", o.getName(), o.getArea()));
    }

    mSpOutletAdapter.notifyDataSetChanged();
  }

  private static class DbSaveOutlet extends AsyncTask<Void, Void, Boolean> {
    private final WeakReference<AttendanceInFragment> mRef;
    private final List<Outlet> mOutlets = new ArrayList<>();

    public DbSaveOutlet(AttendanceInFragment fragment, List<Outlet> outlets) {
      super();
      this.mRef = new WeakReference<>(fragment);
      this.mOutlets.addAll(outlets);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "-- DbSaveOutlet doInBackground");

      AttendanceInFragment ref = mRef.get();
      if (ref == null) return false;

      if (!isCancelled()) {
        final GeneralStorage gs = new GeneralStorage(ref.getActivity());
        for (Outlet o: mOutlets) {
          if (gs.storeSingleOutlet(o)) {
            Log.d(TAG, "Outlet saved");
          }
        }
        return true;
      } else {
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      super.onPostExecute(result);
      Log.d(TAG, "-- DbSaveOutlet onPostExecute:" + result);

      AttendanceInFragment ref = (AttendanceInFragment) mRef.get();
      if (ref == null) return;

      if (!isCancelled()) {
        Log.d(TAG, "DbSaveOutlet isCancelled");
      }
    }
  }

  private void uploadInPhoto(
    @NonNull Context ctx,
    @NonNull String filePath,
    long outlet,
    int shift
  ) {
    if (filePath.isEmpty()) return;
    File f = new File(filePath);
    if (!f.exists()) return;
    if (f.length() <= 0) return;
    Log.d(TAG, "uploadInPhoto size: " + f.length());

    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "uploadInPhoto";

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    SimpleMultiPartRequest smr = new SimpleMultiPartRequest(Request.Method.POST, url,
        response -> {
          Log.d(TAG, "uploadInPhoto Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                setPresent(ctx, outlet, shift);
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

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1; // Add one to month {0 - 11}
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        headers.put("type", String.valueOf(Const.ATTENDANCE_IN));
        headers.put("shift", String.valueOf(shift));
        headers.put("outlet", String.valueOf(outlet));
        headers.put("year", String.valueOf(year));
        headers.put("month", String.valueOf(month));
        headers.put("day", String.valueOf(day));
        headers.put("compress", String.valueOf(1)); // "1" COMPRESS, "0" NO

        return headers;
      }
    };
    smr.addFile("attdc_in", filePath);

    queue.add(smr);
  }

  @SuppressLint("ApplySharedPref")
  private void setPresent(
    @NonNull Context ctx,
    long outlet,
    int shift
  ) {
    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "setAttendanceIn";

    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);

    JSONObject presentJson = new JSONObject();
    try {
      presentJson.put("type", Const.ATTENDANCE_IN);
      presentJson.put("shift", shift);
      presentJson.put("outlet", outlet);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    StringRequest stringReq = new StringRequest(Request.Method.POST, url,
        response -> {
          Log.d(TAG, "setAttendanceIn Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                Utils.toastView(ctx, getString(R.string.success));
                finishPresent();
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
        return presentJson.toString().getBytes();
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

  private void finishPresent() {
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
