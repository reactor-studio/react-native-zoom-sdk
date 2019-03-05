
package com.reactor;

import android.util.Log;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.MeetingOptions;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitializeListener;

import java.util.Map;

import javax.annotation.Nullable;

public class RNMobileRTCModule extends ReactContextBaseJavaModule implements MeetingServiceListener, ZoomSDKInitializeListener {

  private final ReactApplicationContext reactContext;
  private final String E_NO_MEETING_NUMBER = "E_NO_MEETING_NUMBER";
  private final String E_INVALID_MEETING_NUMBER = "E_INVALID_MEETING_NUMBER";
  private final String E_SDK_NOT_INITIALIZED = "E_SDK_NOT_INITIALIZED";
  private boolean mbPendingStartMeeting = false;
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
  public void initialize(final String sdkKey, final String sdkSecret, final String sdkDomain, final Promise promise) {
    this.getCurrentActivity().runOnUiThread(new Runnable()
    {
			public void run()
			{
				initSDK(sdkKey, sdkSecret, sdkDomain, promise);
			}
		});
  }

	private void initSDK(String sdkKey, String sdkSecret, String sdkDomain, Promise promise) {
		ZoomSDK zoomSDK = ZoomSDK.getInstance();
		mPromise = promise;

		if (!zoomSDK.isInitialized()) {
			zoomSDK.initialize(this.getCurrentActivity(), sdkKey, sdkSecret, sdkDomain, this);
		} else {
			startMeetingService();
			promise.resolve("Success!");
		}
	}

  private void startMeetingService() {
    ZoomSDK zoomSDK = ZoomSDK.getInstance();
    MeetingService meetingService = zoomSDK.getMeetingService();
    if (meetingService != null) {
      meetingService.addListener(this);
    }
  }

  @ReactMethod
  public void joinMeeting(ReadableMap options, Promise promise) {
		String meetingNo = options.getString("meetingNumber");
    String userName = options.getString("userName");
		String meetingPassword = options.getString("pwd");

		if(meetingNo.length() == 0) {
			promise.reject(E_NO_MEETING_NUMBER, "You need to enter a scheduled meeting number.");
      return;
		}

		ZoomSDK zoomSDK = ZoomSDK.getInstance();

		if(!zoomSDK.isInitialized()) {
			promise.reject(E_SDK_NOT_INITIALIZED, "ZoomSDK has not been initialized successfully");
      return;
		}

		MeetingService meetingService = zoomSDK.getMeetingService();

		mPromise = promise;

		JoinMeetingOptions opts = new JoinMeetingOptions();
		opts.no_dial_in_via_phone = true;
		opts.no_disconnect_audio = true;
		opts.no_driving_mode = true;

		int ret = meetingService.joinMeeting(this.getCurrentActivity(), meetingNo, userName, meetingPassword, opts);
	}

  @ReactMethod
	public void startMeeting(ReadableMap options, Promise promise) {
		String meetingNo = options.getString("meetingNumber");
		String userName = options.getString("userName");
		int userType = options.getInt("userType");
		String userId = options.getString("userId");
		String userToken = options.getString("userToken");

		if(meetingNo.length() == 0) {
			promise.reject(E_NO_MEETING_NUMBER, "You need to enter a scheduled meeting number.");
      return;
		}

		ZoomSDK zoomSDK = ZoomSDK.getInstance();

		if(!zoomSDK.isInitialized()) {
			promise.reject(E_SDK_NOT_INITIALIZED, "ZoomSDK has not been initialized successfully");
      return;
		}

		final MeetingService meetingService = zoomSDK.getMeetingService();

		if(meetingService.getMeetingStatus() != MeetingStatus.MEETING_STATUS_IDLE) {
			long lMeetingNo = 0;
			try {
				lMeetingNo = Long.parseLong(meetingNo);
			} catch (NumberFormatException e) {
				promise.reject(E_NO_MEETING_NUMBER, "Invalid meeting number: " + meetingNo);
        return;
			}

			if(meetingService.getCurrentRtcMeetingNumber() == lMeetingNo) {
				meetingService.returnToMeeting(this.getCurrentActivity());
				return;
			}

			new AlertDialog.Builder(this.getCurrentActivity())
				.setMessage("Do you want to leave current meeting and start another?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mbPendingStartMeeting = true;
						meetingService.leaveCurrentMeeting(false);
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();
			return;
		}

		mPromise = promise;

		StartMeetingOptions opts = new StartMeetingOptions();
		int ret = meetingService.startMeeting(this.getCurrentActivity(), userName, userToken, userType, meetingNo, userName, opts);
	}

	@Override
  public @Nullable Map getConstants() {
		MapBuilder.Builder meetingErrorBuilder = MapBuilder.builder();

		///network issue, please check network connection
		meetingErrorBuilder.put("NETWORK_ERROR", MeetingError.MEETING_ERROR_NETWORK_ERROR);
		///mmr issue, please check mmr configuration
		meetingErrorBuilder.put("MMR_ERROR", MeetingError.MEETING_ERROR_MMR_ERROR);
		///failed to create video and audio data connection with mmr
		meetingErrorBuilder.put("SESSION_ERROR", MeetingError.MEETING_ERROR_SESSION_ERROR);
		///the meeting is over
		meetingErrorBuilder.put("MEETING_OVER", MeetingError.MEETING_ERROR_MEETING_OVER);
		///the meeting does not exist
		meetingErrorBuilder.put("MEETING_NOT_EXIST", MeetingError.MEETING_ERROR_MEETING_NOT_EXIST);
		///the meeting has reached a maximum of participants
		meetingErrorBuilder.put("USER_FULL", MeetingError.MEETING_ERROR_USER_FULL);
		///the mobilertc version is incompatible
		meetingErrorBuilder.put("CLIENT_VERSION_INCOMPATIBLE", MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE);
		///the meeting was locked by host
		meetingErrorBuilder.put("MEETING_LOCKED", MeetingError.MEETING_ERROR_LOCKED);
		//Meeting Restricted
		meetingErrorBuilder.put("MEETING_RESTRICTED", MeetingError.MEETING_ERROR_RESTRICTED);
		///there does not exist valid mmr
		meetingErrorBuilder.put("NO_MMR", MeetingError.MEETING_ERROR_NO_MMR);
		///the meeting was restricted join before host
		meetingErrorBuilder.put("RESTRICTED_JBH", MeetingError.MEETING_ERROR_RESTRICTED_JBH);
		///failed to send create meeting command to web server
		meetingErrorBuilder.put("CANNOT_EMIT_WEB_REQUEST", MeetingError.MEETING_ERROR_WEB_SERVICE_FAILED);
		///failed to start meeting with expired token
		meetingErrorBuilder.put("CANNOT_START_TOKEN_EXPIRE", MeetingError.MEETING_ERROR_WEBINAR_ENFORCE_LOGIN);
		///webinar has reached its maximum
		meetingErrorBuilder.put("WEBINAR_FULL", MeetingError.MEETING_ERROR_REGISTER_WEBINAR_FULL);
		///sign in to start the webinar
		meetingErrorBuilder.put("WEBINAR_HOST_REGISTER", MeetingError.MEETING_ERROR_DISALLOW_HOST_RESGISTER_WEBINAR);
		///join the webinar from the link
		meetingErrorBuilder.put("WEBINAR_PANELIST_REGISTER", MeetingError.MEETING_ERROR_DISALLOW_PANELIST_REGISTER_WEBINAR);
		///host has denied your webinar registration
		meetingErrorBuilder.put("WEBINAR_DENIED_EMAIL", MeetingError.MEETING_ERROR_HOST_DENY_EMAIL_REGISTER_WEBINAR);
		///sign in with the specified account to join webinar
		meetingErrorBuilder.put("WEBINAR_ENFORCE_LOGIN", MeetingError.MEETING_ERROR_WEBINAR_ENFORCE_LOGIN);
		//Invalid Arguments
		meetingErrorBuilder.put("INVALID_ARGUMENTS", MeetingError.MEETING_ERROR_INVALID_ARGUMENTS);
		//Unknown error
		meetingErrorBuilder.put("UNKNOWN", MeetingError.MEETING_ERROR_UNKNOWN);


    return MapBuilder.of(
			"MeetingError", meetingErrorBuilder.build(),
			"AuthError", MapBuilder.of(
				//Key or Secret is empty
        "KEY_OR_SECRET_EMPTY", ZoomError.ZOOM_ERROR_ILLEGAL_APP_KEY_OR_SECRET,
        //Key or Secret is wrong
        "KEY_OR_SECRET_WRONG", ZoomError.ZOOM_ERROR_ILLEGAL_APP_KEY_OR_SECRET,
        //Client Account does not support
        "ACCOUNT_NOT_SUPPORT", ZoomError.ZOOM_ERROR_DEVICE_NOT_SUPPORTED,
        //Client account does not enable SDK
        "ACCOUNT_NOT_ENABLE_SDK", ZoomError.ZOOM_ERROR_DEVICE_NOT_SUPPORTED,
        //Auth Unknown error
        "UNKNOWN", ZoomError.ZOOM_ERROR_UNKNOWN
			),
			"UserType", MapBuilder.of(
				"API_USER", MeetingService.USER_TYPE_API_USER,
				"ZOOM_USER", MeetingService.USER_TYPE_ZOOM,
				"SSO_USER", MeetingService.USER_TYPE_SSO
			)
		);
  }

  // Listeners
  @Override
  public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
    if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
      mPromise.reject("" + errorCode);
    } else {
      startMeetingService();
      mPromise.resolve("Success!");
    }
  }

  @Override
	public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
    if (mPromise == null) {
      return;
    }
    
		if (meetingStatus == MeetingStatus.MEETING_STATUS_FAILED &&
			errorCode != MeetingError.MEETING_ERROR_SUCCESS) {
			mPromise.reject("" + errorCode);
		}
		
		if (mbPendingStartMeeting && meetingStatus == MeetingStatus.MEETING_STATUS_FAILED) {
			mbPendingStartMeeting = false;
			mPromise.reject("" + errorCode);
		}
		
		if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING) {
			mPromise.resolve("Success!");
		}

    mPromise = null;

		return;
  }
}
