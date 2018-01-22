
# react-native-mobile-rtc

## Getting started

`$ npm install react-native-mobile-rtc --save`

### Mostly automatic installation

`$ react-native link react-native-mobile-rtc`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-mobile-rtc` and add `RNMobileRtc.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMobileRtc.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactor.RNMobileRtcPackage;` to the imports at the top of the file
  - Add `new RNMobileRtcPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-mobile-rtc'
  	project(':react-native-mobile-rtc').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-mobile-rtc/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-mobile-rtc')
  	```


## Usage
```javascript
import RNMobileRTC from 'react-native-mobile-rtc';

// TODO: What to do with the module?
RNMobileRtc.init(zoomSDKKey, zoomSDKSecret, zoomDomain);
```
  