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
        buildRunner.runBuild('haproxy_operations', [
            'SOFTWARE_S3_PATH'  :   s3_path,
            'APP_NODE'          :   app_node,
            'VERSION'           :   version,
            'ENVIRONMENT'       :   env
        ])
    }

}

// public void haproxy(String lb_node, String web_node, String action) {
//     build job: 'haproxy_operations', 
//           parameters: [string(name: 'HAPROXY_NODE', value: lb_node),
//                        string(name: 'APP_NODE', value: web_node),
//                        string(name: 'ACTION', value: action)],
//           wait: true,
//           propagate: true
// }

// public void nginx(String web_node, String action) {
//     build job: 'nginx_operation', 
//           parameters: [string(name: 'LABEL', value: web_node),
//                        string(name: 'ACTION', value: action)],
//           wait: true,
//           propagate: true
// }

// public void tomcat_deploy(String s3_path, String app_node, String version, String env) {
//     build job: 'tomcat_deploy', 
//           parameters: [string(name: 'SOFTWARE_S3_PATH', value: s3_path),
//                        string(name: 'APP_NODE', value: app_node),
//                        string(name: 'VERSION', value: version),
//                        string(name: 'ENVIRONMENT', value: env)],
//           wait: true,
//           propagate: true
// }