#!/usr/bin/groovy

import org.jenkins.*

def call(Map StageParams){
    sh "sudo haproxyctl "${StageParams.app_node} all ${StageParams.action}""
}