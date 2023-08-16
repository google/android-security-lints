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

### [MASVS-STORAGE](https://mas.owasp.org/MASVS/05-MASVS-STORAGE/)
| Lint Issue ID           | Detector                                                                                                                 | Risk                                                                                                                        |
|-------------------------|--------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| `ExposedRootPath`       | [`MisconfiguredFileProviderDetector`](checks/src/main/java/com/example/lint/checks/MisconfiguredFileProviderDetector.kt) | Allowing the root directory of the device in the configuration provides arbitrary access to files and folders for attackers |
| `SensitiveExternalPath` | [`MisconfiguredFileProviderDetector`](checks/src/main/java/com/example/lint/checks/MisconfiguredFileProviderDetector.kt) | Sensitive info like PII should not be stored outside of the application container or system credential storage facilities   |

### [MASVS-CRYPTO](https://mas.owasp.org/MASVS/06-MASVS-CRYPTO/)
| Lint Issue ID               | Detector                                                                                                       | Risk                                                                                                                                       |
|-----------------------------|----------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| `VulnerableCryptoAlgorithm` | [`BadCryptographyUsageDetector`](checks/src/main/java/com/example/lint/checks/BadCryptographyUsageDetector.kt) | Using weak or broken cryptographic hash functions may allow an attacker to reasonably determine the original input                         |
| `WeakPrng`                  | [`WeakPrngDetector`](checks/src/main/java/com/example/lint/checks/WeakPrngDetector.kt)                         | Using non-cryptographically secure PRNGs in security contexts like authentication allows attackers to guess the randomly-generated numbers |

### [MASVS-NETWORK](https://mas.owasp.org/MASVS/08-MASVS-NETWORK/)
| Lint Issue ID             | Detector                                                                                                                       | Risk                                                                                                                                                                                                                     |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `DefaultCleartextTraffic` | [`MissingNetworkSecurityConfigDetector`](checks/src/main/java/com/example/lint/checks/MissingNetworkSecurityConfigDetector.kt) | On API level 27 and below, the default network security config trusts cleartext traffic and needs to be explicitly opted out by the application to only use secure connections                                           |
| `DefaultTrustedUserCerts` | [`MissingNetworkSecurityConfigDetector`](checks/src/main/java/com/example/lint/checks/MissingNetworkSecurityConfigDetector.kt) | On API level 23 and below, the default network security config trusts user-added CA certificates. In practice, it is better to limit the set of trusted CAs so only trusted CAs are used for an app's secure connections |

### [MASVS-PLATFORM](https://mas.owasp.org/MASVS/09-MASVS-PLATFORM/)
| Lint Issue ID          | Detector                                                                                   | Risk                                                                                                                                                                                                 |
|------------------------|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `TapjackingVulnerable` | [`TapjackingDetector`](checks/src/main/java/com/example/lint/checks/TapjackingDetector.kt) | Views without the `filterTouchesWhenObscured` attribute are susceptible to tapjacking attacks by other apps obscuring the UI to trick the user into performing certain actions                       |
| `StrandhoggVulnerable` | [`StrandhoggDetector`](checks/src/main/java/com/example/lint/checks/StrandhoggDetector.kt) | Android previously had a bug in task reparenting in earlier versions, allowing malicious applications to hijack legitimate user actions and trick users into providing credentials to malicious apps |

### [MASVS-CODE](https://mas.owasp.org/MASVS/10-MASVS-CODE/)
| Lint Issue ID                | Detector                                                                                                       | Risk                                                                                                                                                                                                    |
|------------------------------|----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `UnintendedExposedUrl`       | [`UnintendedExposedUrlDetector`](checks/src/main/java/com/example/lint/checks/UnintendedExposedUrlDetector.kt) | URLs that look intended for debugging and development purposes only are exposed in the application, allowing attackers to gain access to parts of the application and server that should be kept secure |
| `UnintendedPrivateIpAddress` | [`UnintendedExposedUrlDetector`](checks/src/main/java/com/example/lint/checks/UnintendedExposedUrlDetector.kt) | Private IP addresses are referenced that may have been intended only for debugging and development, and should not be exposed publicly                                                                  |

## Contact

For questions, comments or feature requests, please file an issue or start a
discussion on Github. We would love to hear from you.
