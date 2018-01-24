
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
  private final String E_NO_MEETING_NUMBER = "E_NO_MEETING_NUMBER";
  private final String E_INVALID_MEETING_NUMBER = "E_INVALID_MEETING_NUMBER";
  private final String E_SDK_NOT_INITIALIZED = "E_SDK_NOT_INITIALIZED";
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
		
		JoinMeetingOptions opts = new JoinMeetingOptions();
//		opts.no_driving_mode = true;
//		opts.no_invite = true;
//		opts.no_meeting_end_message = true;
//		opts.no_titlebar = true;
//		opts.no_bottom_toolbar = true;
//		opts.no_dial_in_via_phone = true;
//		opts.no_dial_out_to_phone = true;
//		opts.no_disconnect_audio = true;
//		opts.no_share = true;
//		opts.invite_options = InviteOptions.INVITE_VIA_EMAIL + InviteOptions.INVITE_VIA_SMS;
//		opts.no_audio = true;
//		opts.no_video = true;
//		opts.meeting_views_options = MeetingViewsOptions.NO_BUTTON_SHARE;
//		opts.no_meeting_error_message = true;
//		opts.participant_id = "participant id";

		int ret = meetingService.joinMeeting(this, meetingNo, userName, meetingPassword, opts);
		
		Log.i(TAG, "onJoinMeeting, ret=" + ret);
	}
	
  @ReactMethod
	public void startMeeting(ReadableMap options, Promise promise) {
		String meetingNo = options.getString("meetingNumber");
		
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
		
		StartMeetingOptions opts = new StartMeetingOptions();
//		opts.no_driving_mode = true;
//		opts.no_invite = true;
//		opts.no_meeting_end_message = true;
//		opts.no_titlebar = true;
//		opts.no_bottom_toolbar = true;
//		opts.no_dial_in_via_phone = true;
//		opts.no_dial_out_to_phone = true;
//		opts.no_disconnect_audio = true;
//		opts.no_share = true;
//		opts.invite_options = InviteOptions.INVITE_ENABLE_ALL;
//		opts.no_audio = true;
//		opts.no_video = true;
//		opts.meeting_views_options = MeetingViewsOptions.NO_BUTTON_SHARE + MeetingViewsOptions.NO_BUTTON_VIDEO;
//		opts.no_meeting_error_message = true;
		
		int ret = meetingService.startMeeting(this, USER_ID, ZOOM_TOKEN, STYPE, meetingNo, DISPLAY_NAME, opts);
	}

  // Listeners
  @Override
  public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
    if (errorCode != ZoomError.ZOOM_ERROR_SUCCESS) {
      mPromise.reject();
    } else {
      startMeetingService();
      mPromise.resolve("Success!");
    }
  }

  @Override
	public void onMeetingEvent(int meetingEvent, int errorCode, int internalErrorCode) {
		
		Log.i(TAG, "onMeetingEvent, meetingEvent=" + meetingEvent + ", errorCode=" + errorCode
				+ ", internalErrorCode=" + internalErrorCode);
		
		if(meetingEvent == MeetingEvent.MEETING_CONNECT_FAILED && errorCode == MeetingError.MEETING_ERROR_CLIENT_INCOMPATIBLE) {
		}
		
		if(mbPendingStartMeeting && meetingEvent == MeetingEvent.MEETING_DISCONNECTED) {
			mbPendingStartMeeting = false;
		}
  }
}