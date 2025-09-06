def call(String serviceDir) {
    stage('Build') {
        dir(serviceDir) {
            sh """
                mvn clean compile -DskipTests \
                    -Dmaven.repo.local=.m2/repository -B -q
            """
            echo "✅ Build completed successfully"
        }
    }
}
