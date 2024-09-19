# RawNFC

Our new and improved Android application for interacting with NFC tags at a low level.

## Development

```
./gradlew installDebug && adb shell am start -n com.dangerousthings.nfc.raw/.MainActivity
```

## Release

```
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=$KEYFILE \
  -Pandroid.injected.signing.store.password=$STORE_PASSWORD \
  -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
  -Pandroid.injected.signing.key.password=$KEY_PASSWORD
```

