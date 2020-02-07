#!/usr/bin/groovy

import org.jenkins.*

private def serviceHelper() {
    return new serviceHelper()
}

def call(Map StageParams){
    helper = serviceHelper()
    helper.serviceHandler("${StageParams.name}", "${StageParams.action}")
}

// def start(Map StageParams){
//     helper = serviceHelper()
//     helper.serviceHandler("${StageParams.name}", "start")
// }

// def stop(Map StageParams){
//     helper = serviceHelper()
//     helper.serviceHandler("${StageParams.name}", "stop")
// }

// def restart(Map StageParams){
//     helper = serviceHelper()
//     helper.serviceHandler("${StageParams.name}", "restart")
// }

def buildStatus(String service, String action) {
    helper = serviceHelper()
    exitCode = helper.serviceHandler("${service}", "status", true)
    helper.setBuildStatus(action, exitCode)
}