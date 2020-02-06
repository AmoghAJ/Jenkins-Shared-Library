private void serviceCheck() {
    RETURN_STATUS   = sh(returnStatus: true, script: 'sudo service tomcat status')
    if ("${params.ACTION}" == 'start' || "${params.ACTION}" == 'restart') {
        if (RETURN_STATUS != 0) {
            currentBuild.result = 'FAILURE'
        }
    } else if ("${params.ACTION}" == 'stop') {
        if (RETURN_STATUS != 0) {
            println ("Script exit code: ${RETURN_STATUS}")
            currentBuild.result = 'SUCCESS'
        }
    }
}

pipeline {
    agent none
    parameters {
        string(name: 'LABEL', defaultValue: 'qa&&web&&helloworld', description: 'Node label')
        choice(name: 'ACTION', choices: ['', 'start', 'stop', 'restart'], description: 'Action')
    }
    stages {
        stage('Tomcat Operation') {
            agent { label "${params.LABEL}" }
            steps {
                sh "sudo service tomcat ${params.ACTION}"    
            }
            post {
                success {
                    script {
                        serviceCheck()
                    }
                }
            } 
        }
    }
}