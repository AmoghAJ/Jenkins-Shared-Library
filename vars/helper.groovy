import org.jenkins.*

private def helperObj() {
    return new helper()
}

def verifyHttpResp(String app, String env) {
    helper = helperObj()
    helper.verifyHttp(app, env)
}

def verifyHttpResp(String web_node) {
    helper = helperObj()
    helper.checkHttpResponse(web_node)
}

