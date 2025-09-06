def call(String serviceDir, String sonarServer) {
    stage('Quality Gate') {
        dir(serviceDir) {
            def sonarProjectKey = env.JOB_NAME.replaceAll(/[\/\%]/, '-') 
            def sonarProjectName = env.JOB_NAME.replaceAll(/[\/\%]/, '-')
            withSonarQubeEnv(sonarServer) {
                sh """
                    mvn sonar:sonar \
                        -Dsonar.projectKey=${sonarProjectKey} \
                        -Dsonar.projectName=${sonarProjectName} \
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
