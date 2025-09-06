def call(String serviceDir) {
    stage('Package') {
        dir(serviceDir) {
            sh """
                mvn package -DskipTests \
                    -Dmaven.repo.local=.m2/repository -B -q
            """
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            echo "✅ Package created successfully"
        }
    }
}
