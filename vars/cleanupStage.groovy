def call() {
    stage('Cleanup') {
        cleanWs()
        sh 'docker image prune -f || true'
        echo "🧹 Workspace cleaned"
    }
}
