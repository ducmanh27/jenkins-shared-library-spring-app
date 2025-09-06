package com.jenkins.helpers

class GitHelper implements Serializable {
    def script

    GitHelper(script) { this.script = script }

    String getShortCommit() {
        return script.sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    }

    String getCommitMessage() {
        return script.sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
    }

    String getGitTag() {
        return script.sh(script: "git describe --tags --exact-match || echo ''", returnStdout: true).trim()
    }

    String getBranchName() {
        return script.env.BRANCH_NAME ?: ''
    }

    String getImageTag() {
        def tag = getGitTag()
        return tag ?: "${script.env.BUILD_NUMBER}"
    }
}