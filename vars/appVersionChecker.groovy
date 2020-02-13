#!/usr/bin/groovy

import org.jenkins.*

private def helper() {
    return new helper()
}

def call(StageParameters) {
    helper = helper()
    helper.versionChecker(StageParameters.app_version)
}