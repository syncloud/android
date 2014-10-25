[![Build Status](https://travis-ci.org/syncloud/android.svg?branch=master)](https://travis-ci.org/syncloud/android)

### Publish to Google Play

1. Have keystore.properties pointing at the key
2. gradle clean assemble (or Gradle tab -> All tasks -> :syncloud -> assemble inside ~~Visual~~ Android Studio)
2. Apk: syncloud/build/outputs/apk/
3. Use syncloud-[beta|prod]-release.apk
4. Go to: [Google Play Console: Syncloud](https://play.google.com/apps/publish/?dev_acc=00379821603627617580#AppListPlace)
5. Select flavor and upload new apk using their UI

#### Google Play key

Was generated using the following and is being kept in secret place! :)
````
keytool -genkey -v -keystore syncloud.keystore -alias syncloud\
-keyalg RSA -keysize 2048 -validity 10000
````
keystore.properties:
````
storePassword=
keyPassword=
keyAlias=syncloud
storeFile=/path/to/syncloud.keystore
````
