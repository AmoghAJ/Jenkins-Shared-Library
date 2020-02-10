@Library('jenkins-shared-lib')_

pipeline {
    agent none
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
                    buildName = "#${BUILD_NUMBER} tomcat-deploy ${params.ENVIRONMENT}"
                    buildDescription = "Version: ${params.VERSION}"
                }
            }
            post {
                failure {
                    script {
                        println "Slack faliure notification"
                    }
                }
            } 
        }
        stage('Verify') {
            steps {
                script{
                    misc.verifyHttpResp(params.APPLICATION, params.ENVIRONMENT)
                }
            }
            post {
                failure {
                    println "Slack faliure notification"
                }
            }
        }
        stage('Manual verification') {
            steps{
                script{
                    misc.msVerify()
                }
            }
        }
    }
}