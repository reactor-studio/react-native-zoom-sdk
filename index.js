
import { NativeModules } from 'react-native';

const { RNMobileRTC } = NativeModules;
const { UserTypes } = RNMobileRTC;
const DEFAULT_MEETING_OPTIONS = {
  meetingNumber: '',
  pwd: '',
  participantId: '',
  userName: '',
};

export default {
  ...RNMobileRTC,

  /**
   * Initializes the Zoom SDK by signing it with SDK Auth.
   * Call this when you app is mounting.
   * @param {String} sdkKey SDK key created on https://developer.zoom.us/me/#sdk
   * @param {String} sdkSecret SDK secret associated with SDK key
   * @param {String} [sdkDomain='zoom.us'] SDK domain, defaults to zoom.us
   * @returns {Promise} A promise represents a result of SDK Authentication.
   * Catch and handle error types from AuthError object
   */
  initialize(sdkKey, sdkSecret, sdkDomain = 'zoom.us') {
    return RNMobileRTC.initialize(sdkKey, sdkSecret, sdkDomain);
  },

  /**
   * Starts the new meeting
   * @param {Object} options Meeting options
   * @param {String} options.meetingNumber Meeting number
   * @param {String} options.userName Users' name to be displayed at a meeting
   * @param {String} [options.pwd] Meeting password
   * @param {String} [options.participantId] Participant ID
   * @param {String} [optons.userType=UserTypes.ZOOM_USER] API_USER, ZOOM_USER or SSO_USER user type
   * from UserTypes object
   * @param {String} [options.userId] User ID from Zoom REST API, send it only
   * when using API_USER UserType
   * @param {String} [options.userToken] User Token from Zoom REST API, send it only
   * when using API_USER UserType
   * @returns {Promise} A promise that represents a result of starting the meeting
   * if resulted with error. Catch and handle error types from MeetingError object
   */
  startMeeting(options) {
    const meetingOptions = {
      ...DEFAULT_MEETING_OPTIONS,
      userType: UserTypes.ZOOM_USER,
      userId: '',
      userToken: '',
      ...options,
    };

    return RNMobileRTC.startMeeting(meetingOptions);
  },

  /**
   * Join existing meeting with meeting options
   * @param {Object} options Meeting options
   * @param {String} options.meetingNumber Meeting number
   * @param {String} options.userName Users' name to be displayed at a meeting
   * @param {String} [options.pwd] Meeting password
   * @param {String} [options.participantId] Participant ID
   * @returns {Promise} A promise that represents a result of joining the meeting
   * if resulted with error. Catch and handle error types from MeetingError object
   */
  joinMeeting(options) {
    const meetingOptions = {
      ...DEFAULT_MEETING_OPTIONS,
      ...options,
    };

    return RNMobileRTC.joinMeeting(meetingOptions);
  },
};
