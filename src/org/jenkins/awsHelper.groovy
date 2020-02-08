package org.jenkins

public void s3copy(String s3ObjectAbsPath, String destination) {
    sh "aws s3 cp ${s3ObjectAbsPath} ${destination}"
}