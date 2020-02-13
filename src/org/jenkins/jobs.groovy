package org.jenkins

def createJob(jenkins) {
    new Job(jenkins)
}

class BuildRunner {

    final def steps;

    public BuildRunner(steps) {
        this.steps = steps
    }

    def runBuild(name, parameters, propagate = true, boolean wait = true) {
        steps.build(
                job: name,
                parameters: toJobParameters(parameters),
                propagate: propagate,
                wait: wait
        )
    }

    private def toJobParameters(parametersMap) {
        parametersMap.collect({ it ->
            toStep(it)
        })
    }

    private def toStep(mapEntry) {

        def value = mapEntry.value
        if (value instanceof Boolean) {
            steps.booleanParam(name: mapEntry.key, value: value)
        } else {
            steps.string(name: mapEntry.key, value: value.toString())
        }
    }
}

class Job {
    private def jenkins
    private def buildRunner

    Job(jenkins) {
        this.jenkins = jenkins
        this.buildRunner = new BuildRunner(jenkins)
    }

    def haproxy(String lb_node, String web_node, String action) {
        buildRunner.runBuild('haproxy_operations', [
            'HAPROXY_NODE'  :   lb_node,
            'APP_NODE'      :   web_node,
            'ACTION'        :   action
        ])
    }

    def nginx(String web_node, String action) {
        buildRunner.runBuild('nginx_operation', [
            'LABEL'         :   web_node,
            'ACTION'        :   action
        ])
    }
    
    def tomcat_deploy(String s3_path, String app_node, String version, String env) {
        buildRunner.runBuild('tomcat_deploy', [
            'SOFTWARE_S3_PATH'  :   s3_path,
            'APP_NODE'          :   app_node,
            'VERSION'           :   version,
            'ENVIRONMENT'       :   env
        ])
    }

    def cd(String s3_path, String app_node, String version, Boolean rel_to_qa, Boolean rel_to_test, Boolean rel_to_prod) {
        buildRunner.runBuild('CD', [
            'SOFTWARE_S3_PATH'      :   s3_path,
            'APPLICATION'           :   app_node,
            'VERSION'               :   version,
            'RELEASE_ON_QA'         :   rel_to_qa,
            'RELEASE_ON_TEST'       :   rel_to_test,
            'RELEASE_ON_PROD'       :   rel_to_prod
        ])
    }
}