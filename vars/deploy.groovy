#!/usr/bin/groovy

import org.jenkins.*

private def helper() {
    return new helper()
}

def call() {
    helper = helper()
    helper.deploy()
}