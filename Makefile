.PHONY: build test android-test lint format release clear-app-data configure-hooks

build:
	./gradlew :app:assembleDebug

test:
	./gradlew test

android-test:
	./gradlew :app:connectedDebugAndroidTest

lint:
	./gradlew ktlintCheck

format:
	./gradlew ktlintFormat

release:
	./gradlew :app:assembleRelease

clear-app-data:
	adb shell pm clear com.cosimomatteini.noted

configure-hooks:
	git config core.hooksPath .githooks
