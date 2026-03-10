source "/home/rafilaos/.sdkman/bin/sdkman-init.sh"
sdk use java 22.1.0.1.r17-gln
sdk use maven 3.8.8
export GRAALVM_HOME=~/.sdkman/candidates/java/22.1.0.1.r17-gln
export JAVA_TOOL_OPTIONS="-XX:-UseContainerSupport"
export ANDROID_HOME=~/Android/Sdk
cd /home/rafilaos/Documents/GitHub/miracloud/frontend

mvn clean gluonfx:build gluonfx:package

find target/gluonfx/aarch64-android/gvm/android_project -name "AndroidManifest.xml" -exec python3 -c "
import sys
with open(sys.argv[1], 'r') as f:
    content = f.read()
content = content.replace(\"android:label='frontend'\", \"android:label='MiraCloud'\")
content = content.replace('android:label=\"frontend\"', 'android:label=\"MiraCloud\"')
with open(sys.argv[1], 'w') as f:
    f.write(content)
" {} \;

cd target/gluonfx/aarch64-android/gvm/android_project
./gradlew assembleDebug
cd /home/rafilaos/Documents/GitHub/miracloud/frontend
adb uninstall org.miracloud.frontend
adb install target/gluonfx/aarch64-android/gvm/android_project/app/build/outputs/apk/debug/app-debug.apk