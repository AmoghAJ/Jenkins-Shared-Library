package org.jenkins

private void serviceAction(String service, String action) {
    sh "sudo service ${service} ${action}"
}

void serviceHandler(String service, String action) {
    switch(action) {
        case 'status':
            serviceAction(service, action)
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