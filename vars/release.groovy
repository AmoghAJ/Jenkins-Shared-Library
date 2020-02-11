#!/usr/bin/groovy

import org.jenkins.*

def call(Map StageParams){
    def helper = new releaseHelper()
    helper.release("${StageParams.application}", "${StageParams.s3_software}", "${StageParams.version}", "${StageParams.environment}")
}