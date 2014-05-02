kosmische
=========

A cosmic virtual analog synthesizer and sequencer for Android.

## Build Instructions
**NOTE: this was tested on OS X. It should work similarly on Linux with some minor changes (ant installation, etc.)**

Install the Android SDK. See https://developer.android.com/sdk/installing/index.html.

Install Apache ant. On OS X, if homebrew is installed:

```bash
brew install ant
```
This project uses the Crystax NDK rather than the standard Android NDK. Download it from https://www.crystax.net/android/ndk.php. Extract and follow the installation instructions in docs/INSTALL.html.

For convenience, add the location of the NDK to your PATH (optional).

From the root SuperCollider-Android project directory:

```bash
ndk-build
# or /path/to/ndk-build if this isn't on your PATH
```
This will build the native modules.

From the same directory:

```bash
android update project -p . --target [target_number]
# where target_number is a one of your installed android targets
# these can be viewed with:
android list
```
This will generate an ant build.xml in the root directory.

Build the project itself:

```bash
ant debug
```
Install on a connected device or emulator:

```bash
adb install -r bin/SuperColliderActivity-debug.apk 
# -r here forces a reintsall if the apk already exists on the target
```