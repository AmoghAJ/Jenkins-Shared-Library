@Library('jenkins-shared-lib')_

pipeline {
    agent none
    parameters {
        string(name: 'HAPROXY_NODE', defaultValue: null, description: 'Node label')
        string(name: 'APP_NODE', defaultValue: null, description: 'Node label')
        choice(name: 'ACTION', choices: ['enable', 'disable'], description: 'Action')
    }
    stages {
        stage('HaProxy Operation') {
            agent { label "${params.HAPROXY_NODE}" }
            steps {
                haproxy app_node: "${params.APP_NODE}" ,action: "${params.ACTION}"
            } 
        }
    }
}