@Library('jenkins-shared-lib')_

node {
    buildName = "#${BUILD_NUMBER} haproxy"
    buildDescription = "App node: ${params.APP_NODE}\nAction: ${params.ACTION}"
}

pipeline {
    agent none
    parameters {
        string(name: 'HAPROXY_NODE', defaultValue: 'qa&&web&&helloworld', description: 'Node label')
        string(name: 'APP_NODE', defaultValue: 'qa&&web&&helloworld', description: 'Node label')
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