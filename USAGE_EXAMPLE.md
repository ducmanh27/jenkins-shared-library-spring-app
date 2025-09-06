# Usage Examples

This document provides comprehensive examples of how to use the Jenkins Shared Library in different scenarios.

## 📚 Table of Contents

- [Basic Pipeline Examples](#basic-pipeline-examples)
- [Advanced Pipeline Examples](#advanced-pipeline-examples)
- [Multi-Service Projects](#multi-service-projects)
- [Custom Configuration Examples](#custom-configuration-examples)
- [Integration Examples](#integration-examples)
- [Best Practices](#best-practices)

## 🚀 Basic Pipeline Examples

### Simple Java Maven Project

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.11'
        jdk 'OpenJDK-21'
    }
    
    environment {
        SERVICE_DIR      = '.'
        IMAGE_NAME       = 'myapp/backend'
        SONARQUBE_SERVER = 'SonarQube'
    }
    
    stages {
        stage('Checkout')      { steps { checkoutStage() } }
        stage('Build')         { steps { buildStage(env.SERVICE_DIR) } }
        stage('Test')          { steps { testStage(env.SERVICE_DIR) } }
        stage('Quality Gate')  { steps { qualityGateStage(env.SERVICE_DIR, env.SONARQUBE_SERVER) } }
    }
    
    post {
        always   { cleanupStage() }
        success  { notifyStage('success') }
        failure  { notifyStage('failure') }
    }
}
```

### Spring Boot Microservice

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.11'
        jdk 'OpenJDK-21'
    }
    
    environment {
        SERVICE_DIR      = 'user-service'
        IMAGE_NAME       = 'elearning/user-service'
        SONARQUBE_SERVER = 'SonarQube'
        SPRING_PROFILES_ACTIVE = 'test'
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
            steps { dockerBuildStage(env.SERVICE_DIR, env.IMAGE_NAME) }
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
```

## 🏗️ Advanced Pipeline Examples

### Full Production Pipeline with Deployment

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.11'
        jdk 'OpenJDK-