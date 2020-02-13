@Library('jenkins-shared-lib')_

pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }
    parameters {
        string(name: 'SOFTWARE_S3_PATH', defaultValue: null, description: 'Software zip')
        string(name: 'VERSION', defaultValue: null, description: 'software version')
        string(name: 'APPLICATION', defaultValue: null, description: 'Application name')
        booleanParam(name: 'RELEASE_ON_QA', defaultValue: true, description: 'Uncheck if does not want deployment on QA')
        booleanParam(name: 'RELEASE_ON_TEST', defaultValue: true, description: 'Uncheck if does not want deployment on TEST')
        booleanParam(name: 'RELEASE_ON_PROD', defaultValue: true, description: 'Uncheck if does not want deployment on PROD')
    }
    stages {
        stage('Deploy to QA') {
            when {
                expression { 
                    params.RELEASE_ON_QA == true
                }
            }
            steps {
                build job: 'release', 
                parameters: [string(name: 'SOFTWARE_S3_PATH', value: "${params.SOFTWARE_S3_PATH}"),
                             string(name: 'VERSION', value: "${params.VERSION}"),
                             string(name: 'ENVIRONMENT', value: "qa"),
                             string(name: 'APPLICATION', value: "${params.APPLICATION}")]
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}"
                    currentBuild.description = "Application:${params.APPLICATION}\nVersion: ${params.VERSION}\nDeployment:\nQA:${params.RELEASE_ON_QA}\nTest:${params.RELEASE_ON_TEST}\nProd:${params.RELEASE_ON_PROD}"
                }
            }
            post {
                failure {
                    println "Slack faliure notification: QA deployment failed"
                }
            } 
        }
        stage('Deploy to Test') {
            when {
                expression { 
                    params.RELEASE_ON_TEST == true
                }
            }
            steps {
                build job: 'release', 
                parameters: [string(name: 'SOFTWARE_S3_PATH', value: "${params.SOFTWARE_S3_PATH}"),
                             string(name: 'VERSION', value: "${params.VERSION}"),
                             string(name: 'ENVIRONMENT', value: "test"),
                             string(name: 'APPLICATION', value: "${params.APPLICATION}")]
            }
            post {
                failure {
                    println "Slack faliure notification: TEST deployment failed"
                }
            }
        }
        stage('Deploy to Prod') {
            when {
                expression { 
                    params.RELEASE_ON_PROD == true
                }
            }
            steps{
                build job: 'release', 
                parameters: [string(name: 'SOFTWARE_S3_PATH', value: "${params.SOFTWARE_S3_PATH}"),
                             string(name: 'VERSION', value: "${params.VERSION}"),
                             string(name: 'ENVIRONMENT', value: "prod"),
                             string(name: 'APPLICATION', value: "${params.APPLICATION}")]
            }
            post {
                failure {
                    println "Slack faliure notification: TEST deployment failed"
                }
            }
        }
        stage('Marking released in Realease Mangement') {
            agent { label "master" }
            steps {
                script {
                    misc.markAsReleased("${params.APPLICATION}-${params.VERSION}")
                }
            }
        }
    } 
}