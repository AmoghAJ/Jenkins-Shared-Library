#!/usr/bin/groovy

import org.jenkins.*

def call(StageParameters) {

    pipeline {
        agent { label 'ci' }
        options { 
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '5')) 
            }
        environment{
            APPLCICATION   = 'hello-world'
            ARTIFACT_ZIP   = misc.artifactZip(APPLCICATION)
            S3_BUCKET      = misc.artifactBucket(APPLCICATION)
            VERSION        = misc.getReleaseVersion()
        }
        stages {
            stage('Build') {
                steps {
                    mavenOperations action: "clean install -U"
                }
                post {
                    always {
                        junit 'target/surefire-reports/*.xml'
                    }
                    success {
                        script {
                            misc.packageArtifact(ARTIFACT_ZIP, "target/*.war")
                            awss3cp s3_object: ARTIFACT_ZIP,destination: misc.s3BucketPadding(S3_BUCKET)   
                            archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                        }
                    }
                    failure {
                        println "Slack notification: Build stage failure"
                    }
                }
            }
            stage('Integration Test') {
                when {
                    branch 'master'
                }
                steps {
                    println 'Integration testing'
                }
            }
            stage('Schedule release') {
                when{
                    branch 'master'
                }
                steps{
                    script {
                        def config = StageParameters.config
                        println "Version:" + VERSION
                        println config.deploy.qa
                        println config.deploy.test
                        println config.deploy.prod
                        // println "Type: $(config.deploy.qa).getClass()"
                    }
                }
            }
        }
    }
}