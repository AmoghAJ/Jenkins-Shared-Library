#!/usr/bin/groovy

import org.jenkins.*

private def awsHelper() {
    return new awsHelper()
}

def call(Map StageParams) {
    awsHelper = awsHelper()
    awsHelper.s3copy(StageParams.s3_object, StageParams.destination)
}