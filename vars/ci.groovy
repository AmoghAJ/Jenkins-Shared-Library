#!/usr/bin/groovy

import org.jenkins.*

def call(StageParameters) {

    pipeline {
        agent { label 'ci' }
        options { 
            timestamps()
            buildDiscarder(logRotator(numToKeepStr: '10')) 
            }
        environment{
            APPLCICATION   = 'hello-world'
            ARTIFACT_ZIP   = misc.artifactZip(APPLCICATION)
            S3_BUCKET      = misc.artifactBucket(APPLCICATION)
            VERSION        = misc.getReleaseVersion()
        }
        stages {
            stage('Pre-Build Intialization') {
                steps {
                    appVersionChecker app_version: "${APPLCICATION}-${VERSION}"
                    script {
                        currentBuild.displayName = "#${BUILD_NUMBER}-${APPLCICATION}-${VERSION}"
                        currentBuild.description = "Artifact: ${ARTIFACT_ZIP}"
                    }
                }
            }
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
            stage('Automated Test') {
                when {
                    branch 'master'
                }
                steps {
                    release application: APPLCICATION, 
                            s3_software: "${misc.s3BucketPadding(S3_BUCKET)}${ARTIFACT_ZIP}", 
                            version: VERSION, 
                            environment: 'ci'
                }
            }
            stage('Schedule release') {
                when{
                    branch 'master'
                }
                steps{
                    script {
                        String releaseDate = misc.rmDateInput()
                        def config = StageParameters.config
                        println "Release Date: ${releaseDate}"
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