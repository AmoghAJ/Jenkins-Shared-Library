#!/usr/bin/groovy

import org.jenkins.*

private def helper = new serviceHelper()

def call(Map StageParams){
    helper.serviceHandler("${StageParams.name}", "${StageParams.action}")
}

def status(Map StageParams){
    helper.serviceHandler("${StageParams.name}", "status")
}

def start(Map StageParams){
    helper.serviceHandler("${StageParams.name}", "start")
}

def stop(Map StageParams){
    helper.serviceHandler("${StageParams.name}", "stop")
}

def restart(Map StageParams){
    helper.serviceHandler("${StageParams.name}", "restart")
}