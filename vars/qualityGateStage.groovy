def call(String serviceDir, String sonarServer) {
    stage('Quality Gate') {
        dir(serviceDir) {
            withSonarQubeEnv(sonarServer) {
                sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${JOB_NAME} \
                        -Dsonar.projectName="${JOB_NAME}" \
                        -Dsonar.projectVersion=1.0 \
                        -Dsonar.sources=src/main/java \
                        -Dsonar.tests=src/test/java \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.java.test.binaries=target/test-classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                        -Dmaven.repo.local=.m2/repository -B -q
                """
            }
            timeout(time: 5, unit: 'MINUTES') {
                def qg = waitForQualityGate()
                if (qg.status != 'OK') {
                    error "❌ Quality Gate failed: ${qg.status}"
                }
            }
            echo "✅ Quality Gate passed"
        }
    }
}
