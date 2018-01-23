
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import <MobileRTC/MobileRTCAuthService.h>
#import <MobileRTC/MobileRTC.h>

@interface RNMobileRTC : NSObject <RCTBridgeModule, MobileRTCAuthDelegate, MobileRTCMeetingServiceDelegate>

@end
