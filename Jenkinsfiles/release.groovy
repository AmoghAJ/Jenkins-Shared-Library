@Library('jenkins-shared-lib')_

pipeline {
    agent none
    option {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        parallelsAlwaysFailFast()
    }
    parameters {
        string(name: 'SOFTWARE_S3_PATH', defaultValue: null, description: 'Software zip')
        string(name: 'VERSION', defaultValue: null, description: 'software version')
        choice(name: 'ENVIRONMENT', choices: ['qa', 'test', 'prod'], description: 'Environment')
        string(name: 'APPLICATION', defaultValue: null, description: 'Application name')

    }
    stages {
        stage('Release') {
            steps {
                release application: "${params.APPLICATION}",
                        s3_software: "${params.SOFTWARE_S3_PATH}",
                        version: "${params.VERSION}",
                        environment: "${params.ENVIRONMENT}"
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.ENVIRONMENT}"
                    currentBuild.description = "Application:${params.APPLICATION}\nVersion: ${params.VERSION}"
                }
            }
            post {
                failure {
                    script {
                        println "Slack faliure notification: Failure in release stage"
                    }
                }
            } 
        }
        stage('Auto Verification') {
            steps {
                script{
                    misc.verifyHttpResp(params.APPLICATION, params.ENVIRONMENT)
                }
            }
            post {
                failure {
                    println "Slack faliure notification: Auto Verification failed"
                }
            }
        }
        stage('Manual verification') {
            when {
                expression { 
                    params.ENVIRONMENT == 'test' ||
                    params.ENVIRONMENT == 'prod'
                }
            }
            steps{
                script{
                    misc.msVerify()
                }
            }
            post {
                failure {
                    println "Slack faliure notification: Manual Verification failed"
                }
                success {
                    script {
                        if(params.ENVIRONMENT == 'prod') {
                            println "Slack notification: Release deployed to production."
                        }
                    }
                }
            }
        }
    }
}