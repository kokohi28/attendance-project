package com.divt.attendance.android.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.model.ResponseAPI;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 */

public class LoginFragment extends NavigationFragment {
  public static final String TAG = LoginFragment.class.getSimpleName();

  private View mRoot = null;

  private EditText mEtEmail = null;
  private EditText mEtPassword = null;
  private ImageView mIvEye = null;
  private boolean mPasswordShow = false;

  private static final int MIN_PASSWORD_LENGTH = 3;

  public LoginFragment() {
    // Required empty public constructor
  }

  public static LoginFragment newInstance() {
    return new LoginFragment();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mRoot = inflater.inflate(R.layout.fragment_login, container, false);
    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View v) {
    mEtEmail = (EditText) v.findViewById(R.id.et_email);
    mEtPassword = (EditText) v.findViewById(R.id.et_password);

    mIvEye = (ImageView) v.findViewById(R.id.iv_eye);
    mIvEye.setOnClickListener(view -> {
      if (mPasswordShow) {
        mIvEye.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_psw_show));
        mEtPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
      } else {
        mIvEye.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_psw_hide));
        mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
      }
      mEtPassword.setSelection(mEtPassword.getText().length());
      mPasswordShow = !mPasswordShow;
    });

    // ***.....Action Listener to disable button save.....*** //
    mEtPassword.addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mIvEye.getVisibility() != View.VISIBLE) mIvEye.setVisibility(View.VISIBLE);
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void afterTextChanged(Editable s) {
      }
    });

    LinearLayout llLogin = (LinearLayout) mRoot.findViewById(R.id.ll_ab_login);
    llLogin.setOnClickListener(v1 -> tryLogin());
  }

  private void tryLogin() {
    // Reset errors.
    mEtEmail.setError(null);
    mEtPassword.setError(null);

    String email = mEtEmail.getText().toString();
    String pass = mEtPassword.getText().toString();

    if (!email.isEmpty()) {
      email = email.trim();
    }

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (TextUtils.isEmpty(pass)) {
      mIvEye.setVisibility(View.GONE);
      mEtPassword.setError(getString(R.string.error_invalid_password));
      focusView = mEtPassword;
      cancel = true;
    } else if (!isPasswordValid(pass)) {
      mEtPassword.setError(getString(R.string.error_invalid_password));
      focusView = mEtPassword;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      mEtEmail.setError(getString(R.string.error_field_required));
      focusView = mEtEmail;
      cancel = true;
    } else if (!isEmailValid(email)) {
      mEtEmail.setError(getString(R.string.error_invalid_email));
      focusView = mEtEmail;
      cancel = true;
    }

    if (cancel) {
      focusView.requestFocus();
    } else {
      // showProgress(true);
      ((MainActivity) getActivity()).dismissKeyboard(mEtEmail);
      doLogin(getActivity(), email, pass);
    }
  }

  @SuppressLint("ApplySharedPref")
  private void doLogin(
    @NonNull Context ctx,
    String mail,
    String pwd
  ) {
    RequestQueue queue = Volley.newRequestQueue(ctx);
    String url = MainActivity.getEndPoint(ctx) + "login";

    try {
      pwd = Utils.SHA1(pwd);
    } catch (NoSuchAlgorithmException e1) {
      e1.printStackTrace();
    }

    String finalPwd = pwd;
    StringRequest stringReq = new StringRequest(Request.Method.POST, url,
        response -> {
          Log.d(TAG, "login Response: " + response);
          try {
            JSONObject respJSON = new JSONObject(response);
            int code = respJSON.getInt("code");
            ResponseAPI respCode = ResponseAPI.values()[code];
            switch (respCode) {
              case SUCCESS:
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("user_id", respJSON.getString("uid"));
                edit.putString("sess", respJSON.getString("token"));

                edit.putString("name", respJSON.getString("name"));
                edit.putString("bio", respJSON.getString("bio"));
                edit.putInt("user_type", respJSON.getInt("user_type"));
                edit.putString("user_type_str", respJSON.getString("user_type_str"));

                edit.commit();

                ((MainActivity) getActivity()).popFragment();
                MainActivity.getHandler().postDelayed(() -> navigateTo(Page.F_HOME), 500);
                Utils.toastView(ctx, R.string.success_login);
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
        headers.put("mail", mail);
        headers.put("pwd", finalPwd);

        return headers;
      }
    };

    queue.add(stringReq);
  }

  private static boolean isPasswordValid(String password) {
    return password.length() > MIN_PASSWORD_LENGTH;
  }

  private static boolean isEmailValid(String email) {
    String regExp = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";

    Pattern pattern = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(email);

    return matcher.matches();
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
