def call(String serviceDir) {
    stage('Package') {
        dir(serviceDir) {
            // Xác định version từ Git tag hoặc fallback
            def version = env.GIT_TAG ? env.GIT_TAG.replaceFirst(/^v/, '') : "${env.BUILD_NUMBER}-SNAPSHOT"
            echo "📦 Packaging version: ${version}"

            // Set version Maven trước khi package
            sh """
                mvn versions:set -DnewVersion=${version} -B -q
                mvn package -DskipTests \
                    -Dmaven.repo.local=.m2/repository -B -q
            """

            // Archive artifact
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            echo "✅ Package created successfully: target/*.jar"
        }
    }
}