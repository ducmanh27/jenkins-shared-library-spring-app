import com.jenkins.helpers.DockerHelper
import com.jenkins.helpers.GitHelper

def call(String serviceDir, String imageName) {
    script {
        def gitHelper = new GitHelper(this)
        def dockerHelper = new DockerHelper(this)

        // IMAGE_TAG đã được inject ở checkout stage
        def imageTag = env.IMAGE_TAG ?: gitHelper.getImageTag()
        def branchName = gitHelper.getBranchName()

        // Luôn build image với version tag
        dockerHelper.buildImage(serviceDir, imageName, imageTag)

        // Nếu là main, đánh thêm tag latest trùng với version mới build
        if (branchName == 'main') {
            dockerHelper.tagImage(imageName, imageTag, 'latest')
        }

        env.DOCKER_IMAGE = "${imageName}:${imageTag}"
        echo "✅ Docker images built for ${branchName}: ${imageName}:${imageTag}${branchName == 'main' ? ' + latest' : ''}"
    }
}