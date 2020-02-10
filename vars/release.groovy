#!/usr/bin/groovy

import org.jenkins.*

def call(Map StageParams){
    def helper = new releaseHelper()
    if ("${StageParams.environment}" == 'prod') {
        helper.releaseProd("${StageParams.application}", "${StageParams.s3_software}", "${StageParams.version}", "${StageParams.environment}", true)
    } else {
        helper.release("${StageParams.application}", "${StageParams.s3_software}", "${StageParams.version}", "${StageParams.environment}")
    }
}