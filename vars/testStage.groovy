def call(String serviceDir) {
    stage('Test') {
        dir(serviceDir) {
            sh """
                mvn test -Dmaven.repo.local=.m2/repository -B -q
            """
            junit '**/target/surefire-reports/*.xml'
            jacoco execPattern: '**/target/jacoco.exec',
                   classPattern: '**/target/classes',
                   sourcePattern: '**/src/main/java'
            echo "✅ Unit tests completed"
        }
    }
}
