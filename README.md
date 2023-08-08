# Android Security Lints

This repository contains custom lint checks for Android development. These lint
checks are by nature more security-focused and experimental than the built-in
lint checks within Android Studio, and are intended for more security-conscious
developers.

These lint checks are based on guidance from the Android Application Security
Knowledge Base that the Android Vulnerability Research team has developed, and
common recurring vulnerabilities that the team spots in the wild.

Visit the official
[Android Lint Github Repo](https://github.com/googlesamples/android-custom-lint-rules)
for guidance on writing your own custom lint checks.

This library uses the Apache license, as is Google's default.

## How to use this library

1.  Clone it from GitHub.
1.  Add the `checks` module to your app's `build.gradle` file:

```shell
dependencies {
  lintChecks project(':checks')
}
```

## Lint checks included in this library

*   `MissingNetworkSecurityConfigDetector` - detects when unsafe default network 
    connection settings are applied to an application through older SDK versions.
    *   Lint ID `DefaultCleartextTraffic`: Suggests adding a network security config
        with `cleartextTrafficPermitted="false"`.
        *   **Risk:** On API level 27 and below, the default network security
            config trusts cleartext traffic and needs to be explicitly opted out
            by the application to only use secure connections.
    *   Lint ID `DefaultTrustedUserCerts`: Prohibits any user certificates being trusted
        in the app's network security config.
        *   **Risk:** On API level 23 and below, the default network security
            config trusts user-added CA certificates. In practice, it is better
            to limit the set of trusted CAs so only trusted CAs are used for an
            app's secure connections.
*   `TapjackingDetector` - Suggests adding the
    `filterTouchesWhenObscured` attribute to buttons and switches with
    `"enable"` in the view name.
    *   Lint ID: `TapjackingVulnerable`
    *   **Risk:** views without this attribute can be susceptible to tapjacking
        attacks by other apps obscuring the UI to trick the user into performing
        certain actions.
*   `StrandhoggDetector` - Suggests updating the target SDK version to
    28 or above.
    *   Lint ID: `StrandhoggVulnerable`
    *   **Risk:** Android previously had a design bug in task reparenting in
        earlier versions, which allowed malicious applications to hijack
        legitimate user actions and trick users into providing credentials to
        malicious apps.
*   `UnintendedExposedUrlDetector` - Prohibits an application from having unintentional
    URLs exposed within XML files.
    *   Lint ID: `UnintendedExposedUrl`
        *   **Risk:** URLs that look intended for debugging and development 
        purposes only are exposed in the application, allowing attackers to
        gain access to parts of the application and server that should be kept secure.
    *   Lint ID: `UnintendedPrivateIpAddress`
        *   **Risk:** Private IP addresses are referenced that may have been 
        intended only for debugging and development, and should not be exposed
        publicly.
*   `TelephonySecretCodeDetector` - Prohibits the use of
    `android.provider.Telephony.SECRET_CODE` within an intent filter / exported
    component.
    *   **Risk:** This is often used to enable debug functionalities in
        production builds, but the secret code can often be retrieved easily and
        abused by attackers, giving them access to developer options of an app.

## Contact

For questions, comments or feature requests, please contact the
[Android Vulnerability Research](mailto:hackdroidz@google.com) team or start a
discussion on Github. We would love to hear from you.
