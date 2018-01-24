
# react-native-mobile-rtc

## Getting started

Before you start, you should add Zoom SDK by downloading iOS and Android SDK to your project
place **iOS SDK** in `ios/lib/` and **Android SDK** in `android` directory of your project.

After that install the package with:

`$ npm install react-native-mobile-rtc --save`

### Mostly automatic installation

Run the `link` command. It will execute other SDK config to your app.

`$ react-native link react-native-mobile-rtc`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-mobile-rtc` and add `RNMobileRTC.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNMobileRTC.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactor.RNMobileRTCPackage;` to the imports at the top of the file
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

## Methods

`initialize` Initializes the SDK with SDK authorization. Returns a promise as a result.
Catch and handle errors with codes from `RNMobileRTC.AuthError` object.

Signature:
```javascript
RNMobileRTC.initialize(sdkKey, sdkSecret, 'zoom.us')
```

`startMeeting` Starts the new meeting. Returns a promise that represents a result of starting
the meeting. Catch and handle error types from `RNMobileRTC.MeetingError` object

Signature:
```javascript
RNMobileRTC.startMeeting({
  meetingNumber: '0123456789',
  userName: 'Nana',
  userType: UserType.ZOOM_USER,
})
```

`joinMeeting` Joins the user to the meeting. Returns a promise that represents a result of joining
to the meeting. Catch and handle error types from `RNMobileRTC.MeetingError` object

Signature:
```javascript
RNMobileRTC.joinMeeting({ meetingNumber: '0123456789', userName: 'Nana', })
```

## Constants

### User types

```javascript
  const { UserType } = RNMobileRTC;
  UserType.API_USER // API user type
  UserType.ZOOM_USER // Work email user type
  UserType.SSO_USER // Single-sign-on user type
```

### Auth error

```javascript
```

## Usage
```javascript
import RNMobileRTC from 'react-native-mobile-rtc';

export default class App extends Component<{}> {
  constructor() {
    super();
    ...
    RNMobileRTC.initialize(key, secret, domain)
      .then((result) => alert(result))
      .catch((error) => { throw new Error(error.message) });
  }

  handleJoinTheMeeting() {
    // Join the meeting
    RNMobileRTC.joinMeeting({
      meetingNumber: '0123456789',
      userName: 'Nana',
      // pwd: 'my password',
      // participantId: '111',
    }).then((result) => alert(result))
      .catch((error) => {
        if (error.code === RNMobileRTC.MeetingError.MEETING_NOT_EXIST) {
          console.error('Meeting does not exist!');
        }
      });
  }

  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity onPress={this.handleJoinTheMeeting}>
          <Text style={styles.welcome}>
            Join the meeting!
        	</Text>
        </TouchableOpacity>
      </View>
    );
  }
}	
```
  