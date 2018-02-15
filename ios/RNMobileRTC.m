
#import "RNMobileRTC.h"

NSString * const AuthError_toString[] = {
    @"Auth Success",
    @"Key or Secret is empty",
    @"Key or Secret is wrong",
    @"Client Account does not support",
    @"Client account does not enable SDK",
    @"Auth Unknown error"
};

@implementation RNMobileRTC
{
    RCTPromiseResolveBlock _initResolver;
    RCTPromiseRejectBlock _initRejecter;
    RCTPromiseResolveBlock _meetingResolver;
    RCTPromiseRejectBlock _meetingRejecter;
    NSString *clientKey;
    NSString *clientSecret;
}



- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(initialize:(NSString *)key secret:(NSString *)secret domain:(NSString *) domain resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [[MobileRTC sharedRTC] setMobileRTCDomain:domain];
    clientKey = key;
    clientSecret = secret;
    _initResolver = resolve;
    _initRejecter = reject;
    [self sdkAuth];
}

RCT_EXPORT_METHOD(startMeeting:(NSDictionary *) options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms){
        ms.delegate = self;
        
        NSDictionary *paramDict = @{
                                    kMeetingParam_UserID:options[@"userId"],
                                    kMeetingParam_UserToken:options[@"userToken"],
                                    kMeetingParam_UserType:options[@"userType"],
                                    kMeetingParam_Username:options[@"userName"],
                                    kMeetingParam_MeetingNumber:options[@"meetNumber"],
                                    kMeetingParam_IsAppShare:options[@"appShare"],
                                    kMeetingParam_ParticipantID:options[@"participantId"],
                                    };
        
        MobileRTCMeetError ret = [ms startMeetingWithDictionary:paramDict];
        NSLog(@"onMeetNow ret:%d", ret);
        _meetingResolver = resolve;
        _meetingRejecter = reject;
    }
}

RCT_EXPORT_METHOD(joinMeeting:(NSDictionary *) options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (![options[@"meetingNumber"] length])
        return;
    
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    [[[MobileRTC sharedRTC] getMeetingSettings] setAutoConnectInternetAudio:YES];

    if (ms)
    {
        ms.delegate = self;
        //For Join a meeting with password
        NSDictionary *paramDict = @{
                                    kMeetingParam_Username: options[@"userName"],
                                    kMeetingParam_MeetingNumber: options[@"meetingNumber"],
                                    kMeetingParam_MeetingPassword: options[@"pwd"],
                                    kMeetingParam_ParticipantID: options[@"participantId"],
                                    };
        MobileRTCMeetError ret = [ms joinMeetingWithDictionary:paramDict];
        NSLog(@"onJoinaMeeting ret:%d", ret);
    }
    _meetingResolver = resolve;
    _meetingRejecter = reject;
}

#pragma mark - Module constants

- (NSDictionary *)constantsToExport
{
    return @{
             @"UserTypes": @{
                     @"API_USER": @(MobileRTCUserType_APIUser),
                     @"ZOOM_USER": @(MobileRTCUserType_ZoomUser),
                     @"SSO_USER": @(MobileRTCUserType_SSOUser),
                     },
             @"MeetingError": @{
                     ///network issue, please check network connection
                     @"NETWORK_ERROR": @(MobileRTCMeetError_NetworkError),
                     ///failed to reconnect meeting
                     @"RECONNECT_ERROR": @(MobileRTCMeetError_ReconnectError),
                     ///mmr issue, please check mmr configuration
                     @"MMR_ERROR": @(MobileRTCMeetError_MMRError),
                     ///meeting password incorrect
                     @"PASSWORD_ERROR": @(MobileRTCMeetError_PasswordError),
                     ///failed to create video and audio data connection with mmr
                     @"SESSION_ERROR": @(MobileRTCMeetError_SessionError),
                     ///the meeting is over
                     @"MEETING_OVER": @(MobileRTCMeetError_MeetingOver),
                     ///the meeting does not start
                     @"MEETING_NOT_START": @(MobileRTCMeetError_MeetingNotStart),
                     ///the meeting does not exist
                     @"MEETING_NOT_EXIST": @(MobileRTCMeetError_MeetingNotExist),
                     ///the meeting has reached a maximum of participants
                     @"USER_FULL": @(MobileRTCMeetError_MeetingUserFull),
                     ///the mobilertc version is incompatible
                     @"CLIENT_VERSION_INCOMPATIBLE": @(MobileRTCMeetError_MeetingClientIncompatible),
                     ///the meeting was locked by host
                     @"MEETING_LOCKED": @(MobileRTCMeetError_MeetingLocked),
                     //Meeting Restricted
                     @"MEETING_RESTRICTED": @(MobileRTCMeetError_MeetingRestricted),
                     ///there does not exist valid mmr
                     @"NO_MMR": @(MobileRTCMeetError_NoMMR),
                     ///the meeting was restricted join before host
                     @"RESTRICTED_JBH": @(MobileRTCMeetError_MeetingRestrictedJBH),
                     ///failed to send creat meeting command to web server
                     @"CANNOT_EMIT_WEB_REQUEST": @(MobileRTCMeetError_CannotEmitWebRequest),
                     ///failed to start meeting with expired token
                     @"CANNOT_START_TOKEN_EXPIRE": @(MobileRTCMeetError_CannotStartTokenExpire),
                     ///the user video cannot work
                     @"VIDEO_ERROR": @(MobileRTCMeetError_VideoError),
                     ///the user audio cannot auto start
                     @"AUDIO_AUTO_START_ERROR": @(MobileRTCMeetError_AudioAutoStartError),
                     ///webinar has reached its maximum
                     @"WEBINAR_FULL": @(MobileRTCMeetError_RegisterWebinarFull),
                     ///sign in to start the webinar
                     @"WEBINAR_HOST_REGISTER": @(MobileRTCMeetError_RegisterWebinarHostRegister),
                     ///join the webinar from the link
                     @"WEBINAR_PANELIST_REGISTER": @(MobileRTCMeetError_RegisterWebinarPanelistRegister),
                     ///host has denied your webinar registration
                     @"WEBINAR_DENIED_EMAIL": @(MobileRTCMeetError_RegisterWebinarDeniedEmail),
                     ///sign in with the specified account to join webinar
                     @"WEBINAR_ENFORCE_LOGIN": @(MobileRTCMeetError_RegisterWebinarEnforceLogin),
                     ///the certificate of ZC has been changed
                     @"ZC_CERTIFICATE_CHANGED": @(MobileRTCMeetError_ZCCertificateChanged),
                     ///the vanity url does not exist
                     @"VANITY_NOT_EXIST": @(MobileRTCMeetError_VanityNotExist),
                     ///the email address has already been register in this webinar
                     @"JOIN_WEBINAR_WITH_SAME_EMAIL": @(MobileRTCMeetError_JoinWebinarWithSameEmail),
                     ///failed to write config file
                     @"WRITE_CONFIG_FILE": @(MobileRTCMeetError_WriteConfigFile),
                     //Invalid Arguments
                     @"INVALID_ARGUMENTS": @(MobileRTCMeetError_InvalidArguments),
                     //Invalid user type
                     @"INVALID_USER_TYPE": @(MobileRTCMeetError_InvalidUserType),
                     //Already In another ongoing meeting
                     @"IN_ANOTHER_MEETING": @(MobileRTCMeetError_InAnotherMeeting),
                     //Unknown error
                     @"UNKNOWN": @(MobileRTCMeetError_Unknown),
                     },
             @"AuthError": @{
                     //Key or Secret is empty
                     @"KEY_OR_SECRET_EMPTY": @(MobileRTCAuthError_KeyOrSecretEmpty),
                     //Key or Secret is wrong
                     @"KEY_OR_SECRET_WRONG": @(MobileRTCAuthError_KeyOrSecretWrong),
                     //Client Account does not support
                     @"ACCOUNT_NOT_SUPPORT": @(MobileRTCAuthError_AccountNotSupport),
                     //Client account does not enable SDK
                     @"ACCOUNT_NOT_ENABLE_SDK": @(MobileRTCAuthError_AccountNotEnableSDK),
                     //Auth Unknown error
                     @"UNKNOWN": @(MobileRTCAuthError_Unknown),
                     }
             };
}

#pragma mark - Auth Delegate

- (void)sdkAuth
{
    
    MobileRTCAuthService *authService = [[MobileRTC sharedRTC] getAuthService];
    if (authService)
    {
        NSLog(@"jesam");
        authService.delegate = self;
        
        [authService logoutRTC];
        
        authService.clientKey = clientKey;
        authService.clientSecret = clientSecret;
        [authService sdkAuth];
    }
}


- (void)onMobileRTCAuthReturn:(MobileRTCAuthError)returnValue
{
    NSLog(@"onMobileRTCAuthReturn");
    if (returnValue != MobileRTCAuthError_Success)
    {
        NSString *errorMsg = AuthError_toString[returnValue];
        _initRejecter([@(returnValue) stringValue], errorMsg, nil);
    }
    else
    {
        _initResolver(nil);
    }
    
    _initResolver = nil;
    _initRejecter = nil;
    return;
}

- (void)onMobileRTCLoginReturn:(NSInteger)returnValue
{
    NSLog(@"onMobileRTCLoginReturn");
}

- (void)onMobileRTCLogoutReturn:(NSInteger)returnValue
{
    NSLog(@"onMobileRTCLogoutReturn");
}

#pragma mark - Meeting Service Delegate

- (void)onMeetingReturn:(MobileRTCMeetError)error internalError:(NSInteger)internalError
{
    NSLog(@"onMeetingReturn:%d, internalError:%zd", error, internalError);
    if (error != MobileRTCMeetError_Success)
    {
        _meetingRejecter([@(error) stringValue], @"error", nil);
    }
    else
    {
        _meetingResolver(nil);
    }
    
    _meetingResolver = nil;
    _meetingRejecter = nil;
    return;
}

- (void)onMeetingError:(NSInteger)error message:(NSString*)message
{
    NSLog(@"onMeetingError:%zd, message:%@", error, message);
    _meetingRejecter([@(error) stringValue], message, nil);
    _meetingRejecter = nil;
    _meetingResolver = nil;
}

- (void)onMeetingStateChange:(MobileRTCMeetingState)state
{
    NSLog(@"onMeetingStateChange:%d", state);
    
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    BOOL inAppShare = [ms isDirectAppShareMeeting] && (state == MobileRTCMeetingState_InMeeting);
    
    if (state == MobileRTCMeetingState_Idle)
    {
        // TODO:
    }
    
    if (state != MobileRTCMeetingState_InMeeting)
    {
        // TODO:
    }
}

- (void)onMeetingReady
{
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if ([ms isDirectAppShareMeeting])
    {
        if ([ms isStartingShare] || [ms isViewingShare])
        {
            NSLog(@"There exist an ongoing share");
            [ms showMobileRTCMeeting:nil];
            return;
        }
        
        BOOL ret = [ms startAppShare];
        NSLog(@"Start App Share... ret:%zd", ret);
    }
}

- (void)onAppShareSplash
{
    
}

- (void)onClickedShareButton
{
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms)
    {
        if ([ms isStartingShare] || [ms isViewingShare])
        {
            NSLog(@"There exist an ongoing share");
            return;
        }
        
        [ms hideMobileRTCMeeting:^(void){
            [ms startAppShare];
        }];
    }
}

- (void)onOngoingShareStopped
{
    NSLog(@"There does not exist ongoing share");
}

- (void)onJBHWaitingWithCmd:(JBHCmd)cmd
{
    
}

- (void)onClickedInviteButton:(UIViewController*)parentVC
{
    
}

- (void)onClickedDialOut:(UIViewController*)parentVC isCallMe:(BOOL)me
{
    NSLog(@"Dial out result");
}

- (void)onDialOutStatusChanged:(DialOutStatus)status
{
    NSLog(@"onDialOutStatusChanged: %zd", status);
}


@end


