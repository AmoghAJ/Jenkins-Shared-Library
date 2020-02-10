package org.jenkins

public void haproxy(String lb_node, String web_node, String action) {
    build job: 'haproxy_operations', 
          parameters: [string(name: 'HAPROXY_NODE', value: lb_node),
                       string(name: 'APP_NODE', value: web_node),
                       string(name: 'ACTION', value: action)],
          wait: true,
          propagate: true
}

public void nginx(String web_node, String action) {
    build job: 'nginx_operation', 
          parameters: [string(name: 'LABEL', value: web_node),
                       string(name: 'ACTION', value: action)],
          wait: true,
          propagate: true
}

public void tomcat_deploy(String s3_path, String app_node, String version, String env) {
    build job: 'tomcat_deploy', 
          parameters: [string(name: 'SOFTWARE_S3_PATH', value: s3_path),
                       string(name: 'APP_NODE', value: app_node),
                       string(name: 'VERSION', value: version),
                       string(name: 'ENVIRONMENT', value: env)],
          wait: true,
          propagate: true
}