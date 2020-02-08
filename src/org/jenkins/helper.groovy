package org.jenkins

public void deploy() {
    sh "sudo rm -rf /opt/tomcat/webapps/ROOT/*"
    sh "sudo unzip -o *.zip"
    sh "sudo find target/ -name '*.war' -exec mv {} /opt/tomcat/webapps/ROOT/ \\;"
    sh "sudo unzip -o /opt/tomcat/webapps/ROOT/*.war"
}