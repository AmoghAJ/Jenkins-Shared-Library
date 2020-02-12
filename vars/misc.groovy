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

def msVerify(){
    helper = helperObj()
    helper.msVerfiy()
}

def shortGitCommitHash() {
    helper = helperObj()
    return helper.getGitCommitHash()
}

def artifactZip(String application) {
    helper = helperObj()
    return helper.getZipFilename(application, helper.getGitCommitHash())
}

def artifactBucket(String application) {
    helper = helperObj()
    return helper.getArtifactBucket(application)
}

String s3BucketPadding(String bucketName, Boolean TrailingBackSlash = true) {
    helper = helperObj()
    String backSlash = TrailingBackSlash ? '/' : ''
    return helper.padS3bucketName(bucketName + backSlash)
}

void packageArtifact(String zipName, String zipContent) {
    helper = helperObj()
    helper.zipArchive(zipName, zipContent)
}