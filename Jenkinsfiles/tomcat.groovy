@Library('jenkins-shared-lib')_

pipeline {
    agent none
    parameters {
        string(name: 'LABEL', defaultValue: 'qa&&web&&helloworld', description: 'Node label')
        choice(name: 'ACTION', choices: ['start', 'stop', 'restart'], description: 'Action')
    }
    stages {
        stage('Tomcat Operation') {
            agent { label "${params.LABEL}" }
            steps {
                service name: "tomcat" ,action: "${params.ACTION}"

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