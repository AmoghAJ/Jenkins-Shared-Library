package org.jenkins

public void mvnOperation(String arguments) {
    sh "mvn ${arguments}"
}