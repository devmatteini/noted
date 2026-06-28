SHELL := bash
.SHELLFLAGS := -euo pipefail -c

TODAY := $(shell date --iso-8601)

build:
	./gradlew :app:assembleDebug

check:
	./gradlew ktlintCheck test :app:assembleDebug

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

generate-fixture:
	./gradlew :app:generateFixture

upload-fixture:
	adb push noted-fixture-${TODAY}.json /sdcard/Download/

configure-hooks:
	git config core.hooksPath .githooks

.PHONY: build test android-test lint format release clear-app-data configure-hooks copy-export generate-fixture upload-fixture check
