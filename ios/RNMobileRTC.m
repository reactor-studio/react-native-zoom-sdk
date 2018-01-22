
#import "RNMobileRTC.h"
#import <MobileRTC/MobileRTC.h>

// TODO (Ivan): export user type consants
// TODO (Ivan): export error codes

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
                                    kMeetingParam_UserID:options["userId"],
                                    kMeetingParam_UserToken:options["userToken"],
                                    kMeetingParam_UserType:options["userType"],
                                    kMeetingParam_Username:options["userName"],
                                    kMeetingParam_MeetingNumber:options["meetNumber"],
                                    kMeetingParam_IsAppShare:@(appShare),
                                    kMeetingParam_ParticipantID:options["participantId"],
                                    };

        MobileRTCMeetError ret = [ms startMeetingWithDictionary:paramDict];
        NSLog(@"onMeetNow ret:%d", ret);
        _meetingResolver = resolve;
        _meetingRejecter = reject;
    }
}

RCT_EXPORT_METHOD(joinMeeting:(NSDictionary *) options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    if (![meetingNo length])
        return;
    
    MobileRTCMeetingService *ms = [[MobileRTC sharedRTC] getMeetingService];
    if (ms)
    {
        ms.delegate = self;
        
        //For Join a meeting with password
        NSDictionary *paramDict = @{
                                    kMeetingParam_Username:options["userName"],
                                    kMeetingParam_MeetingNumber:options["meetingNumber"],
                                    kMeetingParam_MeetingPassword:options["pwd"],
                                    kMeetingParam_ParticipantID:options["participantId"],
                                    };
        MobileRTCMeetError ret = [ms joinMeetingWithDictionary:paramDict];
        NSLog(@"onJoinaMeeting ret:%d", ret);
    }
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
        _initRejecter(errorMsg, errorMsg, nil);
    }
    else
    {
        _initResolver(@"Success!");
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
        _meetingRejecter(@"error", @"error", nil);
    }
    else
    {
        _meetingResolver(@"Meeting!");
    }
    
    _meetingResolver = nil;
    _meetingRejecter = nil;
    return;
}

- (void)onMeetingError:(NSInteger)error message:(NSString*)message
{
    NSLog(@"onMeetingError:%zd, message:%@", error, message);
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

@end

