@Library('jenkins-shared-lib')_

pipeline {
    agent { label 'master'}
    options { 
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10')) 
    }
    triggers {
        cron ('H 10,15 * * 1-5')
    }
    environment{
        releaseMap = ''
        RUN_CD = false
    }
    stages {
        stage('Check Available releases') {
            steps {
                script {
                    releaseMap = misc.getReleaseForDeploy()
                    if(releaseMap == '') {
                        println "There are no releases for now."
                    } else {
                        RUN_CD = true
                    }
                }
            }
        }
        stage('Trigger CD') {
            when {
                expression {
                    RUN_CD == true
                }
            }
            steps {
                script {
                    misc.triggerCD(releaseMap)
                }
            }
        }
    }
}