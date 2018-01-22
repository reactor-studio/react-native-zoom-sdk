
#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif
#import <MobileRTC/MobileRTCAuthService.h>

@interface RNMobileRTC : NSObject <RCTBridgeModule, MobileRTCAuthDelegate>

@end
  
