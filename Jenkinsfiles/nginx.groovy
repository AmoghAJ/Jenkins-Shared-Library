@Library('jenkins-shared-lib')_

pipeline {
    agent none
    parameters {
        string(name: 'LABEL', defaultValue: 'qa&&web&&helloworld', description: 'Node label')
        choice(name: 'ACTION', choices: ['start', 'stop', 'restart'], description: 'Action')
    }
    stages {
        stage('Nginx Operation') {
            agent { label "${params.LABEL}" }
            steps {
                service name: "nginx" ,action: "${params.ACTION}"

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