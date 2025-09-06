def call(String serviceDir, String imageName, String imageTag) {
    stage('Docker Build') {
        dir(serviceDir) {
            sh """
                mvn compile jib:dockerBuild \
                    -Dimage=${imageName}:${imageTag} \
                    -Dmaven.repo.local=.m2/repository -B -q
            """
            if (env.BRANCH_NAME == 'main') {
                sh "docker tag ${imageName}:${imageTag} ${imageName}:latest"
            }
            env.DOCKER_IMAGE = "${imageName}:${imageTag}"
            echo "✅ Docker image built: ${env.DOCKER_IMAGE}"
        }
    }
}
