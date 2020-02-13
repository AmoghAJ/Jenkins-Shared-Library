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

def rmDateInput() {
    helper = helperObj()
    return helper.getDateInputForRelease()
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

String getReleaseVersion() {
    helper = helperObj()
    return helper.extractVersionNumber()
}

void registerRelease(String appVersion, String artifacts, String relDate, String relToQA ,String relToTest, String relToProd) {
    helper = helperObj()
    helper.registerReleasetoDyno(appVersion, artifacts, relDate, relToQA, relToTest, relToProd)
}

void appVersionChecker(String appVersion) {
    helper = helperObj()
    helper.versionChecker(appVersion)
}

void markAsReleased(String appVersion) {
    helper = helperObj()
    helper.markAsReleased(appVersion)
}