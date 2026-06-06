.PHONY: build test android-test release clear-app-data

build:
	./gradlew :app:assembleDebug

test:
	./gradlew test

android-test:
	./gradlew :app:connectedDebugAndroidTest

release:
	./gradlew :app:assembleRelease

clear-app-data:
	adb shell pm clear com.cosimomatteini.noted
