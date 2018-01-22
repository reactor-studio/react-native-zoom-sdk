
#import "RNMobileRTC.h"
#import <MobileRTC/MobileRTC.h>

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

@end

