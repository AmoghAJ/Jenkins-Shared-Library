package org.jenkins

// import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic

public void deploy() {
    sh "rm -rf /opt/tomcat/webapps/ROOT/*"
    sh "unzip -o *.zip"
    sh "find target/ -name '*.war' -exec mv {} /opt/tomcat/webapps/ROOT/ \\;"
    sh "unzip -o /opt/tomcat/webapps/ROOT/*.war -d /opt/tomcat/webapps/ROOT/"
}

private String resources() {
    def json_obj = libraryResource resource: 'app-setup.json'
    return json_obj
}

public def resources_map() {
    def JsonSlurperClassic = new groovy.json.JsonSlurperClassic()
    String resource_string_out = resources()
    return new HashMap<> (JsonSlurperClassic.parseText(resource_string_out))    
}

public void checkHttpResponse(String web_node) {
    node(web_node) {
        httpResponse = sh returnStdout: true, script: "curl -o /dev/null -s -w '%{http_code}' http://localhost"
        println "Server:${web_node}, Http response: ${httpResponse}"
        buildStatus = (httpResponse == '200' || httpResponse == '302') ? 'SUCCESS' : 'FAILURE'
        currentBuild.result = buildStatus
    }
}

public void verifyHttp(String app, String env) {
    data = resources_map()
    loadBalancers = data['apps'][app]['infra'][env]['lb']
    loadBalancers.each { lb_node ->
        checkHttpResponse(lb_node)
    }
}

public def msVerfiy() {
    try {
        userInput = input(id: 'userInput', message: 'Is everything looks fine after release?', ok: "Yes",
                    parameters: [booleanParam(description: 'Please check this if everything is good', name: 'Sanity Check', defaultValue: false)
    } catch(err) {
        def user = err.getCauses()[0].getUser()
        userInput = false
        currentBuild.result = 'Unstable'
        echo "Looks like some issues are found during the monitoring by MS, triggering messge to SDPSP for rollback"
    }
}