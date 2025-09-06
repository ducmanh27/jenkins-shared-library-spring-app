package com.jenkins.helpers

class DockerHelper implements Serializable {
    def script

    DockerHelper(script) { this.script = script }

    void buildImage(String serviceDir, String imageName, String imageTag, String branchName) {
        script.dir(serviceDir) {
            script.sh """
                mvn compile jib:dockerBuild \
                    -Dimage=${imageName}:${imageTag} \
                    -Dmaven.repo.local=.m2/repository -B -q
            """
            if (branchName == 'main') {
                script.sh "docker tag ${imageName}:${imageTag} ${imageName}:latest"
            }
            script.env.DOCKER_IMAGE = "${imageName}:${imageTag}"
            script.echo "✅ Docker image built: ${script.env.DOCKER_IMAGE}"
        }
    }

    void tagImage(String imageName, String fromTag, String toTag) {
        script.sh "docker tag ${imageName}:${fromTag} ${imageName}:${toTag}"
    }
}
