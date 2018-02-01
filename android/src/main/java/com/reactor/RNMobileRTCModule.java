
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
import com.facebook.react.common.MapBuilder;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.StartMeetingOptions;
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
  public void initialize(String sdkKey, String sdkSecret, String sdkDomain, Promise promise) {
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
    return MapBuilder.of(
			"MeetingError", MapBuilder.of(
				///network issue, please check network connection
				"NETWORK_ERROR": MeetingError.MEETING_ERROR_NETWORK_ERROR,
				///mmr issue, please check mmr configuration
				"MMR_ERROR": MeetingError.MEETING_ERROR_MMR_ERROR,
				///failed to create video and audio data connection with mmr
				"SESSION_ERROR": MeetingError.MEETING_ERROR_SESSION_ERROR,
				///the meeting is over
				"MEETING_OVER": MeetingError.MEETING_ERROR_MEETING_OVER,
				///the meeting does not exist
				"MEETING_NOT_EXIST": MeetingError.MEETING_ERROR_MEETING_NOT_EXIST,
				///the meeting has reached a maximum of participants
				"USER_FULL": MeetingError.MEETING_ERROR_USER_FULL,
				///the mobilertc version is incompatible
				"CLIENT_VERSION_INCOMPATIBLE": MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE,
				///the meeting was locked by host
				"MEETING_LOCKED": MeetingError.MEETING_ERROR_LOCKED,
				//Meeting Restricted
				"MEETING_RESTRICTED": MeetingError.MEETING_ERROR_RESTRICTED,
				///there does not exist valid mmr
				"NO_MMR": MeetingError.MEETING_ERROR_NO_MMR,
				///the meeting was restricted join before host
				"RESTRICTED_JBH": MeetingError.MEETING_ERROR_RESTRICTED_JBH,
				///failed to send create meeting command to web server
				"CANNOT_EMIT_WEB_REQUEST": MeetingError.MEETING_ERROR_WEB_SERVICE_FAILED,
				///failed to start meeting with expired token
				"CANNOT_START_TOKEN_EXPIRE": MeetingError.MEETING_ERROR_WEBINAR_ENFORCE_LOGIN,
				///webinar has reached its maximum
				"WEBINAR_FULL": MeetingError.MEETING_ERROR_REGISTER_WEBINAR_FULL,
				///sign in to start the webinar
				"WEBINAR_HOST_REGISTER": MeetingError.MEETING_ERROR_DISALLOW_HOST_RESGISTER_WEBINAR,
				///join the webinar from the link
				"WEBINAR_PANELIST_REGISTER": MeetingError.MEETING_ERROR_DISALLOW_PANELIST_REGISTER_WEBINAR,
				///host has denied your webinar registration
				"WEBINAR_DENIED_EMAIL": MeetingError.MEETING_ERROR_HOST_DENY_EMAIL_REGISTER_WEBINAR,
				///sign in with the specified account to join webinar
				"WEBINAR_ENFORCE_LOGIN": MeetingError.MEETING_ERROR_WEBINAR_ENFORCE_LOGIN,
				//Invalid Arguments
				"INVALID_ARGUMENTS": MeetingError.MEETING_ERROR_INVALID_ARGUMENTS,
				//Unknown error
				"UNKNOWN": MeetingError.MEETING_ERROR_UNKNOWN
    	),
			"AuthError", MapBuilder.of(
				//Key or Secret is empty
        "KEY_OR_SECRET_EMPTY": ZoomError.ZOOM_ERROR_ILLEGAL_APP_KEY_OR_SECRET,
        //Key or Secret is wrong
        "KEY_OR_SECRET_WRONG": ZoomError.ZOOM_ERROR_ILLEGAL_APP_KEY_OR_SECRET,
        //Client Account does not support
        "ACCOUNT_NOT_SUPPORT": ZoomError.ZOOM_ERROR_DEVICE_NOT_SUPPORTED,
        //Client account does not enable SDK
        "ACCOUNT_NOT_ENABLE_SDK": ZoomError.ZOOM_ERROR_DEVICE_NOT_SUPPORTED,
        //Auth Unknown error
        "UNKNOWN": ZoomError.ZOOM_ERROR_UNKNOWN
			),
			"UserType", MapBuilder.of(
				"API_USER": MeetingService.USER_TYPE_API_USER,
				"ZOOM_USER": MeetingService.USER_TYPE_ZOOM_USER,
				"SSO_USER": MeetingService.USER_TYPE_SSO_USER
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
	public void onMeetingEvent(int meetingEvent, int errorCode, int internalErrorCode) {		
		if(meetingEvent == MeetingEvent.MEETING_CONNECT_FAILED && errorCode !== MeetingError.MEETING_ERROR_SUCCESS) {
			mPromise.reject("" + errorCode);
		}
		
		if(mbPendingStartMeeting && meetingEvent == MeetingEvent.MEETING_DISCONNECTED) {
			mbPendingStartMeeting = false;
			mPromise.reject("" + errorCode);
		}

		mPromise.resolve("Success!");

		return;
  }
}