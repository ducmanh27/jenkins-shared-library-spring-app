import com.jenkins.helpers.GitHelper

def call() {
    stage('Checkout') {
        checkout scm
        script {
            def gitHelper = new GitHelper(this)

            env.GIT_COMMIT_SHORT = gitHelper.getShortCommit()
            env.GIT_COMMIT_MSG   = gitHelper.getCommitMessage()
            env.BRANCH_NAME      = gitHelper.getBranchName()
            env.GIT_TAG          = gitHelper.getGitTag()
            env.IMAGE_TAG        = gitHelper.getImageTag()  // Inject cho các stage khác

            echo "📋 Commit: ${env.GIT_COMMIT_SHORT}"
            echo "💬 Message: ${env.GIT_COMMIT_MSG}"
            echo "🌿 Branch: ${env.BRANCH_NAME}"
            echo "🏷️ Tag: ${env.GIT_TAG}"
            echo "🖋 IMAGE_TAG: ${env.IMAGE_TAG}"
        }
    }
}