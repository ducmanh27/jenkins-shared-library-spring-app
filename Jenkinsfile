@Library('jenkins-shared-library') _

pipeline {
    agent any

    tools {
        maven 'Maven-3.9.11'
        jdk 'OpenJDK-21'
    }

    environment {
        SERVICE_DIR      = 'service/courses'
        IMAGE_NAME       = 'elearning/courses'
        SONARQUBE_SERVER = 'SonarQube'
    }

    stages {
        stage('Checkout')      { steps { checkoutStage() } }
        stage('Build')         { steps { buildStage(env.SERVICE_DIR) } }
        stage('Test')          { steps { testStage(env.SERVICE_DIR) } }
        stage('Quality Gate')  { steps { qualityGateStage(env.SERVICE_DIR, env.SONARQUBE_SERVER) } }

        stage('Package') {
            when { expression { isReleaseOrTag() } }
            steps { packageStage(env.SERVICE_DIR) }
        }

        stage('Docker Build') {
            when { expression { isReleaseOrTag() } }
            steps {
                script {
                    def gitHelper = new com.jenkins.helpers.GitHelper(this)
                    def dockerHelper = new com.jenkins.helpers.DockerHelper(this)

                    def imageTag = env.IMAGE_TAG ?: gitHelper.getImageTag()
                    def branchName = gitHelper.getBranchName()

                    // Luôn build image với version tag
                    dockerHelper.buildImage(env.SERVICE_DIR, env.IMAGE_NAME, imageTag)

                    // Nếu là main, tag thêm latest
                    if (branchName == 'main') {
                        dockerHelper.tagImage(env.IMAGE_NAME, imageTag, 'latest')
                    }

                    env.DOCKER_IMAGE = "${env.IMAGE_NAME}:${imageTag}"
                    echo "✅ Docker images built for ${branchName}: ${env.DOCKER_IMAGE}${branchName == 'main' ? ' + latest' : ''}"
                }
            }
        }

        stage('Security Scan') {
            when { expression { isReleaseOrTag() } }
            steps { securityScanStage(env.SERVICE_DIR, env.IMAGE_NAME, env.IMAGE_TAG) } 
        }
    }

    post {
        always   { cleanupStage() }
        success  { notifyStage('success') }
        failure  { notifyStage('failure') }
        unstable { notifyStage('unstable') }
    }
}
