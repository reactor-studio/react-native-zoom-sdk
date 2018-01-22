
package com.reactor;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingEvent;
import us.zoom.sdk.MeetingOptions;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class RNMobileRTCModule extends ReactContextBaseJavaModule implements MeetingServiceListener, ZoomSDKInitializeListener {

  private final ReactApplicationContext reactContext;
  private Promise mPromise;

  public RNMobileRTCModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNMobileRTC";
  }

  @ReactMethod
  public void initialize(String sdkKey, String sdkSecret, String sdkDomain, Promise promise) {
    ZoomSDK zoomSDK = ZoomSDK.getInstance();
    mPromise = promise;

    if (!zoomSDK.isInitialized()) {
      zoomSDK.initialize(initialize(this.getCurrentActivity(), sdkKey, sdkSecret, sdkDomain, this);
    } else {
      startMeetingService();
      promise.resolve("Success!");
    }
  }

  private void startMeetingService() {
    ZoomSDK zoomSDK = ZoomSDK.getInstance();
    meetingService = zoomSDK.getMeetingService();
    if (meetingService !=null) {
      meetingService.addListener(this);
    }
  }

  @Override
  public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
    if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
      mPromise.reject();
    } else {
      startMeetingService();
      mPromise.resolve("Success!");
    }
  }
}