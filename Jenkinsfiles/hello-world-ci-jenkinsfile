pipeline {
    options { 
        buildDiscarder(logRotator(numToKeepStr: '5')) 
        }
    agent {
        node {
            label 'ci'
        }
    }    
    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/AmoghAJ/hello-world-servlet.git'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean install -U'
            }
            post {
                success {
                    junit 'target/surefire-reports/*.xml'
                }
                always {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
                failure {
                    sh 'echo "Trigger Slack Notification"'
                }
            }
        }  
    }
    post {
        success {
            script{
                git_hash = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                zip_filename = "hello-world-${git_hash}.zip"
                sh """
                    zip ${zip_filename} target/*.war
                    aws s3 cp ${zip_filename} s3://hello-world-ci-artifacts/
                    """
            }
        }
    }
}