package com.jenkins.helpers

class NotifyHelper implements Serializable {
    def script

    NotifyHelper(script) { this.script = script }

    void notify(String status) {
        def duration = script.currentBuild.durationString.replace(' and counting', '')
        def message = """
            📋 Job: ${script.env.JOB_NAME}
            🔢 Build: #${script.env.BUILD_NUMBER}
            🌿 Branch: ${script.env.BRANCH_NAME}
            ⏰ Duration: ${duration}
            📝 Commit: ${script.env.GIT_COMMIT_SHORT}
            💬 Message: ${script.env.GIT_COMMIT_MSG}
            🔗 URL: ${script.env.BUILD_URL}
        """.stripIndent()

        switch(status) {
            case 'success':
                script.echo "🎉 Build SUCCESS"
                // script.slackSend(channel: '#ci-cd', color: 'good', message: message)
                break
            case 'failure':
                script.echo "🚨 Build FAILED"
                // script.slackSend(channel: '#ci-cd', color: 'danger', message: message)
                // script.emailext(subject: "❌ Build Failed: ${script.env.JOB_NAME} #${script.env.BUILD_NUMBER}",
                //                body: message, to: 'team@example.com')
                break
            case 'unstable':
                script.echo "⚠️ Build UNSTABLE"
                // script.slackSend(channel: '#ci-cd', color: 'warning', message: message)
                break
            default:
                script.echo "ℹ️ Build status: ${status}"
        }
    }
}