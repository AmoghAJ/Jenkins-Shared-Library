package org.jenkins

evaluate(new File("helper.groovy"))
evaluate(new File("jobs.groovy"))

def resourceData() {
    helper = new helper()
    return helper.resources_map()
}

private void releaseSequence(String lb_node, String web_node, String app_node, String s3_path, String version, String env) {
    jobs = new jobs()
    jobs.haproxy(lb_node, web_node, 'disable')
    jobs.nginx(web_node, 'stop')
    jobs.tomcat_deploy(s3_path, app_node, version, env)
    jobs.nginx(web_node, 'start')
    jobs.haproxy(lb_node, web_node, 'enable')
}

def release(String app, String s3_path, String version, String env) {
    data = resourceData()
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