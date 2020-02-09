#!/usr/bin/groovy

import org.jenkins.*

def call(Map StageParams){
    new releaseHelper().release("${StageParams.application}", "${StageParams.s3_software}", "${StageParams.version}", "${StageParams.environment}")
}