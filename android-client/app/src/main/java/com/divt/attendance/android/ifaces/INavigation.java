package com.divt.attendance.android.ifaces;

import android.os.Bundle;

public interface INavigation {
  enum Page {
    F_UNKNOWN, //

    F_HOME, //

    F_LOGIN, //

    F_ATTENDANCE_IN, //
    F_ATTENDANCE_OUT, //
    F_HISTORY_ATTENDANCE, //

    F_TAKE_PHOTO,
    F_VIEW_PHOTO
  }

  void navigateTo(Page page);

  void navigateTo(Page page, Bundle bundle);

  void navigateTo(Page page, Object object, boolean marker);

  void navigateTo(Page page, Bundle bundle, Object object, boolean marker);

  boolean backButtonEvent();
}