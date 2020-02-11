@Library('jenkins-shared-lib')_

pipeline {
    agent none
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }
    parameters {
        string(name: 'LABEL', defaultValue: null, description: 'Node label')
        choice(name: 'ACTION', choices: ['start', 'stop', 'restart'], description: 'Action')
    }
    stages {
        stage('Nginx Operation') {
            agent { label "${params.LABEL}" }
            steps {
                service name: "nginx" ,action: "${params.ACTION}"
                script{
                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.LABEL}"
                    currentBuild.description = "Action: nginx ${params.ACTION}"
                }
            }
            post {
                always {
                    script {
                        service.buildStatus("nginx", "${params.ACTION}")
                    }
                }
            } 
        }
    }
}