package com.divt.attendance.android;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.divt.attendance.android.fragments.AttendanceInFragment;
import com.divt.attendance.android.fragments.AttendanceOutFragment;
import com.divt.attendance.android.fragments.AttendancePhotoFragment;
import com.divt.attendance.android.fragments.HistoryAttendanceFragment;
import com.divt.attendance.android.fragments.LoginFragment;
import com.divt.attendance.android.fragments.MainFragment;
import com.divt.attendance.android.fragments.TakePhotoFragment;
import com.divt.attendance.android.ifaces.INavigation;
import com.divt.attendance.android.utils.Log;

public class NavigationActivity extends AppCompatActivity implements INavigation {
  private final static String TAG = NavigationActivity.class.getSimpleName();

  private MainFragment mMainFragment = null;

  public NavigationActivity() {
  }

  public MainFragment getMainFragment() {
    return mMainFragment;
  }

  // ***.....Function to hide keyboard.....*** //
  public void dismissKeyboard(View view) {
    if (view == null) view = getCurrentFocus();
    if (view == null) view = new View(this);

    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
  }

  // ***.....Function to show keyboard.....*** //
  public void showKeyboard(View view) {
    if (view == null) return;

    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
  }

  public Fragment getFragmentByTag(String tag) {
    return getFragmentManager().findFragmentByTag(tag);
  }

  public String getTopFragmentTag() {
    int stack = getFragmentManager().getBackStackEntryCount();
    if (stack >= 1) {
      return getFragmentManager().getBackStackEntryAt(stack - 1).getName();
    }

    return "";
  }

  public void popFragment() {
    popFragment(false);
  }

  public void popFragment(boolean immediate) {
    int stack = getFragmentManager().getBackStackEntryCount();
    if (stack > 1) {
      if (immediate) {
        getFragmentManager().popBackStackImmediate();
      } else {
        getFragmentManager().popBackStack();
      }
    }
  }

  public void popAllFragment() {
    popAllFragment(false);
  }

  public void popAllFragment(boolean untilMainActivity) {
    int stack = getFragmentManager().getBackStackEntryCount();
    for (int i = 0; i < stack; i++) {
      int last_idx = getFragmentManager().getBackStackEntryCount() - 1;
      String top_tag = getFragmentManager().getBackStackEntryAt(last_idx).getName();
      Log.d(TAG, ">>>>>>>> pop IMMEDIATE:" + top_tag);

      if (untilMainActivity) {
        if (!top_tag.equals(MainFragment.TAG)) getFragmentManager().popBackStackImmediate();
      } else {
        getFragmentManager().popBackStackImmediate();
      }
    }
  }

  public void popFragments(int count) {
    for (int i = 0; i < count; i++) {
      int stack = getFragmentManager().getBackStackEntryCount();
      Log.d(TAG, ">>>>>>>> popFragments size:" + stack);

      if (stack > 1) {
        getFragmentManager().popBackStackImmediate();
      }
    }
  }

  @Override
  public void navigateTo(Page page) {
    navigateTo(page, null, null, false);
  }

  @Override
  public void navigateTo(Page page, Bundle bundle) {
    navigateTo(page, bundle, null, false);
  }

  @Override
  public void navigateTo(Page page, Object object, boolean marker) {
    navigateTo(page, null, object, marker);
  }

  @Override
  public void navigateTo(Page page, Bundle bundle, Object object, boolean marker /* DIFFERENTIATE PARAMS COUNT */) {
    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
    Log.d(TAG, "----------------------- navigateTo --:" + page);

    switch (page) {
      case F_HOME: {
        mMainFragment = (MainFragment) getFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (mMainFragment == null) {
          mMainFragment = MainFragment.newInstance();

          fragmentTransaction.addToBackStack(MainFragment.TAG);
          fragmentTransaction.add(R.id.fl_view, mMainFragment, MainFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_ATTENDANCE_IN: {
        AttendanceInFragment inFragment = getAttendanceInFragment();
        if (inFragment == null) {
          inFragment = AttendanceInFragment.newInstance();

          fragmentTransaction.addToBackStack(AttendanceInFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein,0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, inFragment, AttendanceInFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_ATTENDANCE_OUT: {
        AttendanceOutFragment outFragment = getAttendanceOutFragment();
        if (outFragment == null) {
          outFragment = AttendanceOutFragment.newInstance();

          fragmentTransaction.addToBackStack(AttendanceOutFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein,0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, outFragment, AttendanceOutFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_HISTORY_ATTENDANCE: {
        HistoryAttendanceFragment historyFragment = getHistoryAttendanceFragment();
        if (historyFragment == null) {
          historyFragment = HistoryAttendanceFragment.newInstance();

          fragmentTransaction.addToBackStack(HistoryAttendanceFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein,0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, historyFragment, HistoryAttendanceFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_LOGIN: {
        LoginFragment loginFragment = getLoginFragment();
        if (loginFragment == null) {
          loginFragment = LoginFragment.newInstance();

          fragmentTransaction.addToBackStack(LoginFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein,0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, loginFragment, LoginFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_TAKE_PHOTO: {
        TakePhotoFragment photoFragment = getTakePhotoFragment();
        if (photoFragment == null) {
          if (bundle != null) {
            photoFragment = TakePhotoFragment.newInstance(bundle);
          } else {
            photoFragment = TakePhotoFragment.newInstance();
          }

          fragmentTransaction.addToBackStack(TakePhotoFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein, 0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, photoFragment, TakePhotoFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      case F_VIEW_PHOTO: {
        AttendancePhotoFragment photoFragment = getAttendancePhotoFragment();
        if (photoFragment == null) {
          if (object != null) {
            photoFragment = AttendancePhotoFragment.newInstance(object);
          } else {
            photoFragment = AttendancePhotoFragment.newInstance();
          }

          fragmentTransaction.addToBackStack(AttendancePhotoFragment.TAG);
          fragmentTransaction.setCustomAnimations(R.animator.enter_fadein, 0, 0, R.animator.exit_fadeout);
          fragmentTransaction.add(R.id.fl_view, photoFragment, AttendancePhotoFragment.TAG);
          fragmentTransaction.commit();
        }
      }
      break;

      default:
        break;
    }
  }

  @Override
  public boolean backButtonEvent() {
    return false;
  }

  @Override
  public void onBackPressed() {
    int stack = getFragmentManager().getBackStackEntryCount();
    if (stack >= 1) {
      String top_tag = getTopFragmentTag();
      Log.d(TAG, ">>>>>>>> onBackPressed NORMAL:" + TAG + ":" + top_tag + " " + stack);

      if (stack == 1 || top_tag.equals(MainFragment.TAG) || top_tag.equals(LoginFragment.TAG)) {
        this.finish();
      } else {
        popFragment();
      }
    } else {
      Log.d(TAG, ">>>>>>>> onBackPressed ZERO STACK");
      super.onBackPressed();
    }
  }

  public AttendanceInFragment getAttendanceInFragment() {
    return (AttendanceInFragment) getFragmentByTag(AttendanceInFragment.TAG);
  }

  public AttendanceOutFragment getAttendanceOutFragment() {
    return (AttendanceOutFragment) getFragmentByTag(AttendanceOutFragment.TAG);
  }

  public HistoryAttendanceFragment getHistoryAttendanceFragment() {
    return (HistoryAttendanceFragment) getFragmentByTag(HistoryAttendanceFragment.TAG);
  }

  public LoginFragment getLoginFragment() {
    return (LoginFragment) getFragmentByTag(LoginFragment.TAG);
  }

  public TakePhotoFragment getTakePhotoFragment() {
    return (TakePhotoFragment) getFragmentByTag(TakePhotoFragment.TAG);
  }

  public AttendancePhotoFragment getAttendancePhotoFragment() {
    return (AttendancePhotoFragment) getFragmentByTag(AttendancePhotoFragment.TAG);
  }
}
