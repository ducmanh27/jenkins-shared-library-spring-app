def call(String serviceDir, String imageName, String imageTag) {
    stage('Security Scan') {
        dir(serviceDir) {
            sh """
                mvn org.owasp:dependency-check-maven:check \
                    -Dmaven.repo.local=.m2/repository -B -q
            """
            // Nếu cài Trivy:
            // sh "trivy image ${imageName}:${imageTag} || true"
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'target',
                reportFiles: 'dependency-check-report.html',
                reportName: 'OWASP Dependency Check Report'
            ])
            echo "✅ Security scan completed"
        }
    }
}
