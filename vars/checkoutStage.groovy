def call() {
    stage('Checkout') {
        checkout scm
        script {
            env.GIT_COMMIT_SHORT = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            env.GIT_COMMIT_MSG   = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
            
            // Lấy branch từ Jenkins nếu có, fallback rỗng
            env.BRANCH_NAME = env.BRANCH_NAME ?: ''

            // Lấy tag nếu commit có tag
            env.GIT_TAG = sh(script: "git describe --tags --exact-match || echo ''", returnStdout: true).trim()

            echo "📋 Commit: ${env.GIT_COMMIT_SHORT}"
            echo "💬 Message: ${env.GIT_COMMIT_MSG}"
            echo "🌿 Branch: ${env.BRANCH_NAME}"
            echo "🏷️ Tag: ${env.GIT_TAG}"
        }
    }
}
