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
        IMAGE_TAG        = "${BUILD_NUMBER}"
        SONARQUBE_SERVER = 'SonarQube'
    }

    stages {
        stage('Checkout')      { steps { checkoutStage() } }
        stage('Build')         { steps { buildStage(env.SERVICE_DIR) } }
        stage('Test')          { steps { testStage(env.SERVICE_DIR) } }
        stage('Quality Gate')  { steps { qualityGateStage(env.SERVICE_DIR, env.SONARQUBE_SERVER) } }
        stage('Package')       { steps { packageStage(env.SERVICE_DIR) } }
        stage('Docker Build')  { steps { dockerBuildStage(env.SERVICE_DIR, env.IMAGE_NAME, env.IMAGE_TAG) } }
        stage('Security Scan') { steps { securityScanStage(env.SERVICE_DIR, env.IMAGE_NAME, env.IMAGE_TAG) } }
    }

    post {
        always   { cleanupStage() }
        success  { notifyStage('success') }
        failure  { notifyStage('failure') }
        unstable { notifyStage('unstable') }
    }
}
