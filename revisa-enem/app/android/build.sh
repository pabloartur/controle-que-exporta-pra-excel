#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
BT=../sdk/android-14
JAR=../sdk/android-34/android.jar
rm -rf build && mkdir -p build/gen build/classes build/dex assets/fonts
python3 make_icon.py res
python3 make_assets.py ../revisa-enem.html assets
$BT/aapt2 compile --dir res -o build/res.zip
$BT/aapt2 link -o build/base.apk -I $JAR --manifest AndroidManifest.xml -A assets \
  --min-sdk-version 21 --target-sdk-version 34 --version-code 4 --version-name 1.3 \
  --java build/gen build/res.zip
javac -nowarn -Xlint:-options --release 8 -classpath $JAR -d build/classes \
  src/br/revisaenem/app/MainActivity.java build/gen/br/revisaenem/app/R.java
java -cp ../sdk/r8.jar com.android.tools.r8.D8 --release --lib $JAR --min-api 21 --output build/dex $(find build/classes -name '*.class')
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
$BT/zipalign -p -f 4 build/unsigned.apk build/aligned.apk
[ -f revisa.jks ] || keytool -genkeypair -keystore revisa.jks -alias revisa -keyalg RSA -keysize 2048 \
  -validity 10000 -storepass revisaenem -keypass revisaenem -dname "CN=Revisa ENEM" >/dev/null 2>&1
$BT/apksigner sign --ks revisa.jks --ks-pass pass:revisaenem --key-pass pass:revisaenem --out RevisaENEM.apk build/aligned.apk
$BT/apksigner verify --verbose RevisaENEM.apk | head -5
$BT/aapt2 dump badging RevisaENEM.apk | head -6
ls -la RevisaENEM.apk
