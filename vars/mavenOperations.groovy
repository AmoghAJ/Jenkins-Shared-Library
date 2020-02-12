#!/usr/bin/groovy

import org.jenkins.*

private def mavenObj() {
    return new maven()
}

def call(Map StageParams) {
    mvnObj    = mavenObj()
    arguments = StageParams.action
    mvnOperation(arguments)
}