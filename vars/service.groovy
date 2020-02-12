#!/usr/bin/groovy

import org.jenkins.*

private def serviceHelper() {
    return new serviceHelper()
}

def call(Map StageParams){
    helper = serviceHelper()
    helper.serviceHandler("${StageParams.name}", "${StageParams.action}")
}

def buildStatus(String service, String action) {
    helper = serviceHelper()
    exitCode = helper.serviceHandler("${service}", "status", true)
    helper.setBuildStatus(action, exitCode)
}