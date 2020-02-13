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
        }
        stages {
            stage('Pre-Build Intialization') {
                steps {
                    script {
                        if(GIT_BRANCH == 'master')  {
                            env.VERSION        = misc.getReleaseVersion()
                            misc.appVersionChecker("${APPLCICATION}-${VERSION}")
                        } else {
                            env.VERSION = GIT_BRANCH
                        }
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
                        misc.registerRelease("${APPLCICATION}-${VERSION}", 
                                             "${misc.s3BucketPadding(S3_BUCKET)}${ARTIFACT_ZIP}",
                                             "${releaseDate}",
                                             config.deploy.qa.toString(),
                                             config.deploy.test.toString(),
                                             config.deploy.prod.toString())
                    }
                }
            }
        }
    }
}