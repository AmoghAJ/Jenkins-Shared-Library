@Library('jenkins-shared-lib')_

pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }
    parameters {
        string(name: 'SOFTWARE_S3_PATH', defaultValue: null, description: 'Software zip')
        string(name: 'APP_NODE', defaultValue: null, description: 'Node label')
        string(name: 'VERSION', defaultValue: null, description: 'Software version')
        choice(name: 'ENVIRONMENT', choices: ['ci', 'qa', 'test', 'prod'], description: 'Environment')
    }
    stages {
        stage('Download Software') {
            agent { label "${params.APP_NODE}" }
            steps {
                awss3cp s3_object           :   "${params.SOFTWARE_S3_PATH}" ,
                        destination         :   "."
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER} tomcat-deploy ${params.ENVIRONMENT}"
                    currentBuild.description = "Version: ${params.VERSION}"
                } 
            } 
        }
        stage('Stop Tomcat') {
            steps {
                build job: 'tomcat_operations', 
                parameters: [string(name: 'LABEL', value: "${params.APP_NODE}"),
                             string(name: 'ACTION', value: 'stop')]
            }
        }
        stage('Deploy') {
            agent { label "${params.APP_NODE}" }
            steps {
                deploy()
            }
        }
        stage('Start Tomcat') {
            steps {
                build job: 'tomcat_operations', 
                parameters: [string(name: 'LABEL', value: "${params.APP_NODE}"),
                             string(name: 'ACTION', value: 'start')]
            }
        }
    }
}