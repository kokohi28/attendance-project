package com.divt.attendance.android.fragments;

import android.app.Fragment;
import android.os.Bundle;

import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.ifaces.INavigation;

public class NavigationFragment extends Fragment implements INavigation {

  public Fragment getActiveFragmentByTag(String tag) {
    if (getActivity() instanceof MainActivity) {
      return ((MainActivity) getActivity()).getFragmentByTag(tag);
    }
    return null;
  }

  public String getTopFragmentTag() {
    if (getActivity() instanceof MainActivity) {
      return ((MainActivity) getActivity()).getTopFragmentTag();
    }
    return null;
  }

  public MainActivity getMainActv() {
    return (MainActivity) getActivity();
  }

  @Override
  public boolean backButtonEvent() {
    return false;
  }

  @Override
  public void navigateTo(INavigation.Page page) {
    if (getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).navigateTo(page);
    }
  }

  @Override
  public void navigateTo(INavigation.Page page, Bundle bundle) {
    if (getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).navigateTo(page, bundle, null, false);
    }
  }

  @Override
  public void navigateTo(INavigation.Page page, Object object, boolean marker) {
    if (getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).navigateTo(page, null, object, marker);
    }
  }

  @Override
  public void navigateTo(Page page, Bundle bundle, Object object, boolean marker) {
    if (getActivity() instanceof MainActivity) {
      ((MainActivity) getActivity()).navigateTo(page, bundle, object, marker);
    }
  }
}
