def call(String status) {
    def duration = currentBuild.durationString.replace(' and counting', '')
    def message = """
        📋 Job: ${env.JOB_NAME}
        🔢 Build: #${env.BUILD_NUMBER}
        🌿 Branch: ${env.BRANCH_NAME}
        ⏰ Duration: ${duration}
        📝 Commit: ${env.GIT_COMMIT_SHORT}
        💬 Message: ${env.GIT_COMMIT_MSG}
        🔗 URL: ${env.BUILD_URL}
    """.stripIndent()

    if (status == 'success') {
        echo "🎉 Build SUCCESS"
        // slackSend(channel: '#ci-cd', color: 'good', message: message)
    } else if (status == 'failure') {
        echo "🚨 Build FAILED"
        // slackSend(channel: '#ci-cd', color: 'danger', message: message)
        // emailext(subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
        //          body: message, to: 'team@example.com')
    } else if (status == 'unstable') {
        echo "⚠️ Build UNSTABLE"
        // slackSend(channel: '#ci-cd', color: 'warning', message: message)
    }
}
