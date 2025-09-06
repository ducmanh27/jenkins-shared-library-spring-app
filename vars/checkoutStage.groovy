def call() {
    stage('Checkout') {
        checkout scm
        script {
            env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            env.GIT_COMMIT_MSG   = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
            env.BRANCH_NAME      = sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
            echo "📋 Commit: ${env.GIT_COMMIT_SHORT}"
            echo "💬 Message: ${env.GIT_COMMIT_MSG}"
            echo "🌿 Branch: ${env.BRANCH_NAME}"
        }
    }
}
