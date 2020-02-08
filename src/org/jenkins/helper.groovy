package org.jenkins

public void deploy() {
    sh "rm -rf /opt/tomcat/webapps/ROOT/*"
    sh "unzip -o *.zip"
    sh "find target/ -name '*.war' -exec mv {} /opt/tomcat/webapps/ROOT/ \\;"
    sh "unzip -o /opt/tomcat/webapps/ROOT/*.war -d /opt/tomcat/webapps/ROOT/"
}