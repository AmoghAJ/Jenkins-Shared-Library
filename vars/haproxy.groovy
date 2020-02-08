#!/usr/bin/groovy

import org.jenkins.*

def call(Map StageParams){
    new serviceHelper().haproxyHandler("${StageParams.app_node}", "${StageParams.action}")
}