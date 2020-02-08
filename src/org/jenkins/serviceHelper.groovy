package org.jenkins

private int serviceActionReturnExitCode(String service, String action) {
    exitCode = sh returnStatus: true, script: "sudo service ${service} ${action}"
    return exitCode
}

private void serviceAction(String service, String action) {
    sh "sudo service ${service} ${action}"
}

void setBuildStatus(String action, int exitCode) {
    if ("${action}" == 'start' || "${action}" == 'restart') {
        if (exitCode != 0) {
            currentBuild.result = 'FAILURE'
        }
    } else if ("${action}" == 'stop') {
        if (exitCode != 0) {
            println ("Script exit code: ${exitCode}")
            currentBuild.result = 'SUCCESS'
        }
    }
}

def serviceHandler(String service, String action, Boolean returnExitCode = false) {
    switch(action) {
        case 'status':
            if(returnExitCode) {
                return serviceActionReturnExitCode(service, action)
            } else {
                serviceAction(service, action)
            }
        break

        case 'start':
            serviceAction(service, action)
        break
        
        case 'stop':
            serviceAction(service, action)
        break

        case 'restart':
            serviceAction(service, action)
        break
    }
}

def haproxyHandler(String appNode, String action) {
    sh "sudo haproxyctl '${action} all ${appNode}'"
}