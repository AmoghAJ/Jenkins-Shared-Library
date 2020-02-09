package org.jenkins

import groovy.json.JsonSlurper

public void deploy() {
    sh "rm -rf /opt/tomcat/webapps/ROOT/*"
    sh "unzip -o *.zip"
    sh "find target/ -name '*.war' -exec mv {} /opt/tomcat/webapps/ROOT/ \\;"
    sh "unzip -o /opt/tomcat/webapps/ROOT/*.war -d /opt/tomcat/webapps/ROOT/"
}

private String resources() {
    def jsonSlurper = new JsonSlurper()
    def json_obj = libraryResource resource: 'app-setup.json'
    return json_obj
}

public def resources_map() {
    String resource_string_out = resources()   
    def out = jsonSlurper.parseText(json_obj)
    return out
}