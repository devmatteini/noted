.PHONY: build test android-test release

build:
	./gradlew :app:assembleDebug

test:
	./gradlew test

android-test:
	./gradlew :app:connectedDebugAndroidTest

release:
	./gradlew :app:assembleRelease
