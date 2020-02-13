@Library('jenkins-shared-lib')_

pipeline {
    agent none
    options { 
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10')) 
    }
    triggers {
        cron {'H 10,15 * * 1-5'}
    }
    environment{
        RUN_CD = false
    }
    stages {
        stage('Check Available releases') {
            steps {
                script {
                    def releaseMap = misc.getReleaseForDeploy()
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