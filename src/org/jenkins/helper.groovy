package org.jenkins

// import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic
import java.util.regex.*

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

public void msVerfiy() {
    try {
        input(message: 'Sign off from MS post sanity check?', ok: "Yes", submitter: "ms")
    } catch(err) {
        error('"Looks like some issues are found during the monitoring by MS, triggering messge to SDPSP for rollback"')
    }
}

private String getDateInputForRelease() {
    try {
        def inputDate = input(id: 'inputDate', message: 'Please specify date for the release', ok: "Submit",
                      parameters: [string(defaultValue	: null,
                                          description	: 'Date formate: YYYYMMDD',
                                          name			: 'date')])
        return inputDate
    } catch(err) {
        error("This release would not be auto scheduled for deployment.")
    }
}

public String getGitCommitHash() {
    return sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
}

public String getZipFilename(String application, String gitCommit) {
    data = resources_map()
    String zipFile = data['apps'][application]['zip']
    return zipFile.replaceAll('#commit-hash#', "${gitCommit}")
}

public String getArtifactBucket(String application) {
    data = resources_map()
    return data['apps'][application]['artifacts-bucket']
}

private String padS3bucketName(String bucketName) {
    return "s3://${bucketName}"
}

private void zipArchive(String zipName, String zipContent) {
    sh "zip ${zipName} ${zipContent}"
}

private String extractVersionNumber() {
    String commitMsg = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim().toLowerCase()
    String version = ''
    try {
        extractVersion = { it.split("version:")[1] }
        String versionWithBrackets = commitMsg.split("[\\[\\]]")[1]
        version = extractVersion(versionWithBrackets).trim()
    } catch(ArrayIndexOutOfBoundsException err) {
        error("Version not specified please specify version number in commit messege in format [Version:1.0]")
    }
    return version
}

private void checkOut(String repo) {
    git(
       url: repo,
       branch: "master"
    )
}

private void checkOutReleaseHelper() {
    checkOut('https://github.com/AmoghAJ/release-management-helper.git')
}

private void versionChecker(String appVersion) {
    checkOutReleaseHelper()
    dir('release-management-helper') {
        result = sh(returnStdout: true, script: "invoke application-version-exist ${appVersion}").trim()
        if (result == 'false'){
            error('Application version number: ${appVersion} already exist in the release database.')
        }
    }
}