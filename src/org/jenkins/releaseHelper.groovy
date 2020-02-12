package org.jenkins

evaluate(new File("helper.groovy"))
evaluate(new File("jobs.groovy"))

private void releaseSequence(String lb_node, String web_node, String app_node, String s3_path, String version, String env, Boolean skipHaproxy = false) {
    helper      = new helper()
    jobHelper   = new jobs()
    jobs        = jobHelper.createJob(this)
    if (!skipHaproxy) {
        jobs.haproxy(lb_node, web_node, 'disable')
    }
    jobs.nginx(web_node, 'stop')
    jobs.tomcat_deploy(s3_path, app_node, version, env)
    jobs.nginx(web_node, 'start')
    helper.checkHttpResponse(web_node)
    if (!skipHaproxy) {
        jobs.haproxy(lb_node, web_node, 'enable')
    }
}

public void release(String app, String s3_path, String version, String env) {
    switch(env) {
        case 'ci':
            releaseCI(app, s3_path, version, env)
        break
        case 'qa':
            releaseAllParallel(app, s3_path, version, env)
        break

        case 'test':
            releaseAllSequential(app, s3_path, version, env)
        break
        
        case 'prod':
            releaseFirstVerifyRestParallel(app, s3_path, version, env)
        break
    }

}

private void releaseCI(String app, String s3_path, String version, String env) {
    helper      = new helper()
    data        = helper.resources_map()
    jobHelper   = new jobs()
    jobs        = jobHelper.createJob(this)

    def webNode = data['apps'][app]['infra'][env]['web'][0]
    def appNode = data['apps'][app]['infra'][env]['app'][0]

    releaseSequence(null, webNode, appNode, s3_path, version, env, true)
}
private void releaseAllParallel(String app, String s3_path, String version, String env) {
    helper      = new helper()
    data        = helper.resources_map()
    jobHelper   = new jobs()
    jobs        = jobHelper.createJob(this)
    def builds  = [:]
    int nodes   = data['apps'][app]['infra'][env]['app'].size() - 1
    
    for(int x=0; x<=nodes; x++) {

        def lbNode  = data['apps'][app]['infra'][env]['lb'][0]
        def webNode = data['apps'][app]['infra'][env]['web'][x]
        def appNode = data['apps'][app]['infra'][env]['app'][x]

        def releaseSeq =  {[
                                jobs.haproxy(lbNode, webNode, 'disable'),
                                jobs.nginx(webNode, 'stop'),
                                jobs.tomcat_deploy(s3_path, appNode, version, env),
                                jobs.nginx(webNode, 'start'),
                                helper.checkHttpResponse(webNode),
                                jobs.haproxy(lbNode, webNode, 'enable')
                            ]}
        
        builds["Deploy ${appNode}"] = releaseSeq    
    }
    parallel(builds) 
}

private void releaseAllSequential(String app, String s3_path, String version, String env) {
    helper  = new helper()
    data    = helper.resources_map()
    int nodes = data['apps'][app]['infra'][env]['app'].size() - 1
    for(int x=0; x<=nodes; x++) {
        def lbNode  = data['apps'][app]['infra'][env]['lb'][0]
        def webNode = data['apps'][app]['infra'][env]['web'][x]
        def appNode = data['apps'][app]['infra'][env]['app'][x]

        releaseSequence(lbNode, webNode, appNode, s3_path, version, env)
    }
}

private void releaseFirstVerifyRestParallel(String app, String s3_path, String version, String env) {
    helper  = new helper()
    data    = helper.resources_map()
    
    def firstLbNode  = data['apps'][app]['infra'][env]['lb'][0]
    def firstWebNode = data['apps'][app]['infra'][env]['web'][0]
    def firstAppNode = data['apps'][app]['infra'][env]['app'][0]
    // Release on first node
    releaseSequence(firstLbNode, firstWebNode, firstAppNode, s3_path, version, env)

    // Verification from MS after first node deployment
    helper.msVerfiy()

    int nodes = data['apps'][app]['infra'][env]['app'].size() - 1
    if (nodes >= 2) {
        
        jobHelper   = new jobs()
        jobs        = jobHelper.createJob(this)
        def builds  = [:]
        
        for(int x=1; x<=nodes; x++) {

            def lbNode  = data['apps'][app]['infra'][env]['lb'][0]
            def webNode = data['apps'][app]['infra'][env]['web'][x]
            def appNode = data['apps'][app]['infra'][env]['app'][x]
            
            def releaseSeq =  {[
                                    jobs.haproxy(lbNode, webNode, 'disable'),
                                    jobs.nginx(webNode, 'stop'),
                                    jobs.tomcat_deploy(s3_path, appNode, version, env),
                                    jobs.nginx(webNode, 'start'),
                                    helper.checkHttpResponse(webNode),
                                    jobs.haproxy(lbNode, webNode, 'enable')
                                ]}
            
            builds["Deploy ${appNode}"] = releaseSeq
            
        }

        parallel(builds) 
    } else {
        for(int x=1; x<=nodes; x++) {
            
            def lbNode  = data['apps'][app]['infra'][env]['lb'][0]
            def webNode = data['apps'][app]['infra'][env]['web'][x]
            def appNode = data['apps'][app]['infra'][env]['app'][x]
            
            releaseSequence(lbNode, webNode, appNode, s3_path, version, env)
        }
    }
}