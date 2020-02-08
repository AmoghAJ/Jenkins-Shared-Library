@Library('jenkins-shared-lib')_

pipeline {
    agent none
    parameters {
        string(name: 'SOFTWARE_S3_PATH', defaultValue: null, description: 'Software zip')
        string(name: 'APP_NODE', defaultValue: null, description: 'Node label')
        string(name: 'VERSION', defaultValue: null, description: 'Action')
        choice(name: 'ENVIRONMENT', choices: ['qa', 'test', 'prod'], description: 'Environment')
    }
    stages {
        stage('Download Software') {
            agent { label "${params.APP_NODE}" }
            steps {
                awss3cp s3_object           :   "${params.APP_NODE}" ,
                        destination         :   "."
                script {
                    buildName = "#${BUILD_NUMBER} tomcat-deploy ${params.ENVIRONMENT}"
                    buildDescription = "Version: ${params.VERSION}"
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
                deploy
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