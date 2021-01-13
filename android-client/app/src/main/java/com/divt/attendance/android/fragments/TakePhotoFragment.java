package com.divt.attendance.android.fragments;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.divt.attendance.android.Const;
import com.divt.attendance.android.MainActivity;
import com.divt.attendance.android.R;
import com.divt.attendance.android.support.AspectRatioDialog;
import com.divt.attendance.android.support.cameraview.AspectRatio;
import com.divt.attendance.android.support.cameraview.CameraView;
import com.divt.attendance.android.utils.Log;
import com.divt.attendance.android.utils.MPermsManager;
import com.divt.attendance.android.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A simple {@link Fragment} subclass.
 */

public class TakePhotoFragment extends NavigationFragment implements AspectRatioDialog.RatioDialogListener, View.OnClickListener {
  public static final String TAG = TakePhotoFragment.class.getSimpleName();

  private View mRoot = null;
  private Toolbar mToolbar;
  private ImageButton mIvFlash;
  private ImageButton mIvSwitchCamera;

  private static final String RATIO_CHOOSE__DIALOG = "ratio_choose_dialog";

  private static final int[] FLASH_OPTIONS = {
      CameraView.FLASH_AUTO,
      CameraView.FLASH_OFF,
      CameraView.FLASH_ON,
  };

  private static final int[] FLASH_ICONS = {
      R.drawable.ic_flash_auto,
      R.drawable.ic_flash_off,
      R.drawable.ic_flash_on,
  };

  private static final int[] FLASH_TITLES = {
      R.string.flash_auto,
      R.string.flash_off,
      R.string.flash_on,
  };

  private int mCurrentFlash;
  private CameraView mCameraView;
  private String mFromPage;

  final String[] CAMERA_PERMISSION = new String[]{
      MPermsManager.P_CAMERA
  };

  public TakePhotoFragment() {
    // Required empty public constructor
  }

  public static TakePhotoFragment newInstance() {
    TakePhotoFragment fragment = new TakePhotoFragment();
    return fragment;
  }

  public static TakePhotoFragment newInstance(Bundle data) {
    TakePhotoFragment fragment = new TakePhotoFragment();
    fragment.mFromPage = data.getString(Const.PAGE);
    return fragment;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    Log.d(TAG, ">>>>>>>> onCreateView:" + TAG);

    mRoot = inflater.inflate(R.layout.fragment_take_photo, container, false);
    AppCompatActivity activity = (AppCompatActivity) getActivity();


    ((MainActivity) getActivity()).dismissKeyboard(container);


    //For translucent status bar
    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

    // Toolbar
    mToolbar = mRoot.findViewById(R.id.toolbar_take_photo);
    activity.setSupportActionBar(mToolbar);
    activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

    configViews(mRoot);

    return mRoot;
  }

  private void configViews(View v) {
    MainActivity actv = (MainActivity) getActivity();
//    actv.setFragmentEvent(com.halo.support.TakePhotoFragment.TAG, new UICallback.ActivityToFragmentCallback() {
//      @Override
//      public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        Log.d(TAG, "-- onRequestPermissionsResult CONTACT() : " + requestCode + " " + permissions.length + " " + grantResults.length);
//
//        if (requestCode == MPermsManager.REQUEST_WITH_SPECIFIC_PURPOSE) {
//          if (MPermsManager.verifyWithSpecificPermissions(grantResults, permissions, CAMERA_PERMISSION)) {
//            Log.d(TAG, "... Perms : CONTACT GRANTED");
//
//            Log.d(TAG, "Start Camera");
//            // Started on Resume
//            // if (mCameraView != null) mCameraView.start();
//          } else {
//            Log.d(TAG, "... Perms : DENY");
//            ((MainActivity) getActivity()).popFragment();
//          }
//          return true;
//        }
//
//        return false;
//      }
//
//      @Override
//      public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
//        return false;
//      }
//    });

    mCameraView = v.findViewById(R.id.camera);

    mIvFlash = v.findViewById(R.id.iv_flash);
    mIvSwitchCamera = v.findViewById(R.id.iv_switch_camera);

    mIvFlash.setOnClickListener(this);
    mIvSwitchCamera.setOnClickListener(this);

    mCameraView.addCallback(mCallback);

    ImageButton fab = v.findViewById(R.id.take_picture);
    fab.setOnClickListener((v1) -> {
      if (mCameraView != null) mCameraView.takePicture();
    });
  }

  private CameraView.Callback mCallback = new CameraView.Callback() {
    @Override
    public void onCameraOpened(CameraView cameraView) {
      Log.d(TAG, "onCameraOpened");
    }

    @Override
    public void onCameraClosed(CameraView cameraView) {
      Log.d(TAG, "onCameraClosed");
    }

    @Override
    public void onPictureTaken(CameraView cameraView, final byte[] data) {
      Log.d(TAG, "onPictureTaken " + data.length);

      MainActivity.getHandler().post(() -> {
        File file = null;
        if (mFromPage.equals(AttendanceInFragment.TAG)) {
          file = new File(MainActivity.getAppCacheDir(), Const.CAPTURED_IN);
        }

        if (file == null) {
          throw new IllegalArgumentException("PAGE FROM NOT DEFINED");
        }

        OutputStream os = null;
        try {
          os = new FileOutputStream(file);
          os.write(data);
          os.close();
        } catch (IOException e) {
          Log.e(TAG, "Cannot write to " + file, e);
        } finally {
          Utils.closeSilently(os);
        }

        Utils.closeSilently(os);
        String fName = Utils.makeCopyPhotoCompressed(getMainActv(), file.getAbsolutePath(), MainActivity.getAppCacheDir());
        Log.d(TAG, "Compress to: " + fName);

        ((MainActivity) getActivity()).popFragment();

        MainActivity.getHandler().post(() -> {
          if (mFromPage.equals(AttendanceInFragment.TAG)) {
            AttendanceInFragment fragment = (AttendanceInFragment) getActiveFragmentByTag(AttendanceInFragment.TAG);
            if (fragment != null) fragment.processCapturedPhoto();
          }
        });
      });
    }
  };

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, ">>>>>>>> onResume:" + TAG);

    if (MPermsManager.isAllPermsGranted(getActivity(), CAMERA_PERMISSION)) {
      if (mCameraView != null) {
        mCameraView.start();
//        MainActivity.getHandler().post(() -> {
//          if (mFromPage.equals(ProfileFragment.TAG) || mFromPage.equals(SetProfileFragment.TAG)) {
//            Log.d(TAG, "set Front Facing");
//            mCameraView.setFacing(CameraView.FACING_FRONT);
//          } else if (mFromPage.equals(PrivateChatFragment.TAG)) {
//            Log.d(TAG, "set Back Camera");
//            mCameraView.setFacing(CameraView.FACING_BACK);
//          }
//        });
      }
    } else {
      MPermsManager.requestPerms(getActivity(), CAMERA_PERMISSION, MPermsManager.REQUEST_WITH_SPECIFIC_PURPOSE);
    }
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

    if (mCameraView != null) mCameraView.stop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, ">>>>>>>> onDestroy:" + TAG);

    mRoot = null;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    menu.clear();
//    inflater.inflate(R.menu.menu_camera, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

      case android.R.id.home:
        getFragmentManager().popBackStack();
        return true;

      default:
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onAspectRatioSelected(@NonNull AspectRatio ratio) {
    if (mCameraView != null) mCameraView.setAspectRatio(ratio);
  }

  @Override
  public void onClick(View v) {

    switch (v.getId()) {

      case R.id.iv_flash: {
        if (mCameraView != null) {
          mCurrentFlash = (mCurrentFlash + 1) % FLASH_OPTIONS.length;
          mIvFlash.setImageResource(FLASH_ICONS[mCurrentFlash]);
          mCameraView.setFlash(FLASH_OPTIONS[mCurrentFlash]);
        }
      }
      break;

      case R.id.iv_switch_camera: {
        if (mCameraView != null) {
          int facing = mCameraView.getFacing();
          mCameraView.setFacing(facing == CameraView.FACING_FRONT ?
                  CameraView.FACING_BACK : CameraView.FACING_FRONT);
        }
      }
      break;

    }
  }
}
