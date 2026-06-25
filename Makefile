.PHONY: build test android-test lint format release clear-app-data configure-hooks copy-export

TODAY := $(shell date --iso-8601)

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

copy-export:
	adb pull /sdcard/Download/noted-backup-${TODAY}.json .

configure-hooks:
	git config core.hooksPath .githooks
