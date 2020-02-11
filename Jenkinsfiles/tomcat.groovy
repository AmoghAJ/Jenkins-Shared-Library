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
        stage('Tomcat Operation') {
            agent { label "${params.LABEL}" }
            steps {
                service name: "tomcat" ,action: "${params.ACTION}"
                script{
                    currentBuild.displayName = "#${BUILD_NUMBER}-${params.LABEL}"
                    currentBuild.description = "Action: tomcat ${params.ACTION}"
                }
            }
            post {
                always {
                    script {
                        service.buildStatus("tomcat", "${params.ACTION}")
                    }
                }
            } 
        }
    }
}