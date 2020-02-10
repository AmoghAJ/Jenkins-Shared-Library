package org.jenkins

evaluate(new File("helper.groovy"))
evaluate(new File("jobs.groovy"))

public def helper  = new helper()

private void releaseSequence(String lb_node, String web_node, String app_node, String s3_path, String version, String env) {
    jobs    = new jobs()
    jobs.haproxy(lb_node, web_node, 'disable')
    jobs.nginx(web_node, 'stop')
    jobs.tomcat_deploy(s3_path, app_node, version, env)
    jobs.nginx(web_node, 'start')
    helper.checkHttpResponse(web_node)
    jobs.haproxy(lb_node, web_node, 'enable')
}

private def releaseSequenceParallel(String lb_node, String web_node, String app_node, String s3_path, String version, String env) {
    jobHelper   = new jobs()
    jobs        = jobHelper.createJob(this)
    def executionSeq = [
        jobs.haproxy(lb_node, web_node, 'disable'),
        jobs.nginx(web_node, 'stop'),
        jobs.tomcat_deploy(s3_path, app_node, version, env),
        jobs.nginx(web_node, 'start'),
        helper.checkHttpResponse(web_node),
        jobs.haproxy(lb_node, web_node, 'enable')
    ]
    return executionSeq
}

def release(String app, String s3_path, String version, String env) {
    data = helper.resources_map()
    int nodes = data['apps'][app]['infra'][env]['app'].size() - 1
    for(int x=0; x<=nodes; x++) {
        releaseSequence(data['apps'][app]['infra'][env]['lb'][0], 
                        data['apps'][app]['infra'][env]['web'][x], 
                        data['apps'][app]['infra'][env]['app'][x],
                        s3_path,
                        version,
                        env)
    }
}

def releaseProd(String app, String s3_path, String version, String env, Boolean parallelDeployment = false) {
    helper  = new helper()
    data = helper.resources_map()
    // Release on first node
    // releaseSequence(data['apps'][app]['infra'][env]['lb'][0], 
    //                     data['apps'][app]['infra'][env]['web'][0], 
    //                     data['apps'][app]['infra'][env]['app'][0],
    //                     s3_path,
    //                     version,
    //                     env)
    // Verification from MS after first node deployment
    // helper.msVerfiy()
    int nodes = data['apps'][app]['infra'][env]['app'].size() - 1
    if (parallelDeployment && nodes >= 2) {
        // def builds = [:]
        def buildJobs = []
        for(int x=0; x<=nodes; x++) {
            // builds.put("Deployment on ${data['apps'][app]['infra'][env]['app'][x]}", releaseSequence(data['apps'][app]['infra'][env]['lb'][0], 
                                                                                //  data['apps'][app]['infra'][env]['web'][x], 
                                                                                //  data['apps'][app]['infra'][env]['app'][x],
                                                                                //  s3_path,
                                                                                //  version,
            
                                                                                //  env))
            def jobs = [releaseSequenceParallel(data['apps'][app]['infra'][env]['lb'][0], 
                                          data['apps'][app]['infra'][env]['web'][x], 
                                          data['apps'][app]['infra'][env]['app'][x],
                                          s3_path,
                                          version,
                                          env)]
            buildJobs.add(jobs)
        }
        println(buildJobs)
        parallel buildJobs
    } else {
        for(int x=1; x<=nodes; x++) {
            releaseSequence(data['apps'][app]['infra'][env]['lb'][0], 
                            data['apps'][app]['infra'][env]['web'][x], 
                            data['apps'][app]['infra'][env]['app'][x],
                            s3_path,
                            version,
                            env)
        }
    }
}