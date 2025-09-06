# Jenkins Shared Library for DevOps Pipeline

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Jenkins](https://img.shields.io/badge/Jenkins-2.0+-blue.svg)](https://jenkins.io/)
[![Docker](https://img.shields.io/badge/Docker-24.0+-blue.svg)](https://docker.com/)

A comprehensive Jenkins Shared Library that provides reusable pipeline stages for Java/Maven projects with integrated DevOps toolchain including SonarQube, Docker, security scanning, and notifications.

## 🚀 Features
![CompleteCICDProject!](https://lucid.app/publicSegments/view/0c183bd6-73f4-4547-93e1-5246db5e863c/image.png) 
- **Complete CI/CD Pipeline**: From checkout to deployment with quality gates
- **Docker Integration**: Docker-in-Docker support for containerized builds
- **Code Quality**: SonarQube integration with quality gates
- **Security Scanning**: OWASP dependency check and container security
- **Artifact Management**: Nexus repository integration
- **Smart Versioning**: Git tag-based semantic versioning
- **Branch Strategy**: Release branch and tag-based deployment
- **Notifications**: Configurable build notifications
- **Infrastructure as Code**: Complete Docker Compose setup

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Infrastructure Setup](#infrastructure-setup)
- [Pipeline Usage](#pipeline-usage)
- [Available Stages](#available-stages)
- [Helper Classes](#helper-classes)
- [Configuration](#configuration)
- [Branch Strategy](#branch-strategy)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🚀 Quick Start

### 1. Setup Infrastructure

```bash
# Clone the repository
git clone <your-repo-url>
cd jenkins-shared-library

# Start the infrastructure
docker-compose up -d

# Wait for services to be healthy
docker-compose ps
```

### 2. Configure Jenkins

1. Access Jenkins at `http://localhost:8080`
2. Complete the initial setup wizard
3. Install required plugins:
   - Pipeline
   - Docker Pipeline
   - SonarQube Scanner
   - JaCoCo
   - OWASP Dependency-Check

### 3. Setup Shared Library

1. Go to Jenkins → Manage Jenkins → Configure System
2. Under "Global Pipeline Libraries", add:
   - **Name**: `jenkins-shared-library`
   - **Default Version**: `main`
   - **Repository URL**: `<your-shared-library-repo>`
   - **Credentials**: Select appropriate Git credentials

### 4. Create Your First Pipeline

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9.11'
        jdk 'OpenJDK-21'
    }
    
    environment {
        SERVICE_DIR      = 'your-service-directory'
        IMAGE_NAME       = 'your-app/service-name'
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

## 🏗️ Infrastructure Setup

The infrastructure includes the following services:

| Service | Port | Description |
|---------|------|-------------|
| Jenkins | 8080 | CI/CD Server |
| SonarQube | 9000 | Code Quality Analysis |
| Nexus | 8081, 8082 | Artifact Repository |
| Portainer | 9443 | Docker Management |
| Redis | 6379 | Caching (optional) |
| PostgreSQL | 5432 | SonarQube Database |

### Environment Variables

Create a `.env` file in your project root:

```env
# Database passwords
SONAR_DB_PASSWORD=your_sonar_password
REDIS_PASSWORD=your_redis_password

# Optional: Custom configurations
JENKINS_ADMIN_USER=admin
JENKINS_ADMIN_PASSWORD=your_jenkins_password
```

### Service Health Checks

All services include health checks. Monitor service status:

```bash
# Check all services
docker-compose ps

# View logs for specific service
docker-compose logs jenkins
docker-compose logs sonarqube
```

## 🔧 Available Stages

### Core Stages

#### `checkoutStage()`
- Checks out source code from SCM
- Extracts Git information (commit, branch, tag)
- Sets environment variables for downstream stages

#### `buildStage(String serviceDir)`
- Compiles Java source code using Maven
- Skips tests for faster compilation
- Uses local Maven repository for caching

#### `testStage(String serviceDir)`
- Runs unit tests with JUnit
- Generates JaCoCo code coverage reports
- Publishes test results and coverage metrics

#### `qualityGateStage(String serviceDir, String sonarServer)`
- Performs SonarQube code quality analysis
- Waits for quality gate results
- Fails build if quality gate criteria not met

### Conditional Stages (Release/Tag Only)

#### `packageStage(String serviceDir)`
- Creates deployable JAR artifacts
- Uses semantic versioning from Git tags
- Archives artifacts for later deployment

#### `dockerBuildStage(String serviceDir, String imageName)`
- Builds Docker images with proper tags
- Tags `latest` for main branch builds
- Uses Docker-in-Docker for security

#### `securityScanStage(String serviceDir, String imageName, String imageTag)`
- Runs OWASP dependency vulnerability scanning
- Optional Trivy container image scanning
- Publishes security reports

### Utility Stages

#### `cleanupStage()`
- Cleans Jenkins workspace
- Prunes unused Docker images
- Maintains clean build environment

#### `notifyStage(String status)`
- Sends build notifications
- Supports multiple notification channels
- Configurable based on build result

#### `isReleaseOrTag()`
- Helper function to determine deployment eligibility
- Returns true for release branches (`release/*`) or version tags (`v*.*.*`)

## 🛠️ Helper Classes

### GitHelper
```groovy
def gitHelper = new com.jenkins.helpers.GitHelper(this)

// Available methods
gitHelper.getShortCommit()     // Get short commit hash
gitHelper.getCommitMessage()   // Get commit message
gitHelper.getBranchName()      // Get current branch
gitHelper.getGitTag()          // Get Git tag if exists
gitHelper.getImageTag()        // Get Docker image tag (tag or commit)
```

### DockerHelper
```groovy
def dockerHelper = new com.jenkins.helpers.DockerHelper(this)

// Available methods
dockerHelper.buildImage(serviceDir, imageName, tag)
dockerHelper.tagImage(imageName, sourceTag, targetTag)
dockerHelper.pushImage(imageName, tag)
```

### NotifyHelper
```groovy
def notifyHelper = new com.jenkins.helpers.NotifyHelper(this)

// Available methods
notifyHelper.notify(status)  // Send notifications based on build status
```

## ⚙️ Configuration

### Jenkins Tool Configuration

1. **Maven Configuration**:
   - Go to Manage Jenkins → Global Tool Configuration
   - Add Maven installation named `Maven-3.9.11`

2. **JDK Configuration**:
   - Add JDK installation named `OpenJDK-21`

3. **SonarQube Configuration**:
   - Go to Configure System → SonarQube servers
   - Add server named `SonarQube`
   - URL: `http://sonarqube:9000`
   - Add authentication token

### SonarQube Setup

1. Access SonarQube at `http://localhost:9000`
2. Default credentials: `admin/admin`
3. Create a project and generate authentication token
4. Add token to Jenkins credentials

### Docker Registry Configuration

Configure Nexus as Docker registry:

1. Access Nexus at `http://localhost:8081`
2. Create Docker registry repositories
3. Configure Docker daemon to use insecure registry (for local development)

## 🌿 Branch Strategy

The pipeline follows a GitFlow-inspired branching strategy:

### Branch Types

- **`main`**: Production-ready code, automatically tagged as `latest`
- **`develop`**: Integration branch for development
- **`feature/*`**: Feature development branches
- **`release/*`**: Release preparation branches (triggers deployment)
- **`hotfix/*`**: Critical fixes for production

### Deployment Triggers

Deployments are triggered by:
- **Release branches**: `release/v1.2.3`
- **Version tags**: `v1.2.3` (semantic versioning)

### Versioning Strategy

- **Git tags**: `v1.2.3` → Docker tag `1.2.3`
- **No tag**: Uses commit hash for development builds
- **Main branch**: Always tagged as `latest`

## 📊 Monitoring and Reports

### Available Reports

1. **Test Results**: JUnit test reports with trends
2. **Code Coverage**: JaCoCo coverage reports
3. **Code Quality**: SonarQube analysis results
4. **Security**: OWASP dependency check reports
5. **Build Artifacts**: Downloadable JAR files

### Metrics and Dashboards

- Jenkins build history and trends
- SonarQube quality metrics dashboard
- Docker image registry in Nexus
- Portainer for container monitoring

## 🔍 Troubleshooting

### Common Issues

#### Docker Permission Issues
```bash
# Add Jenkins user to Docker group
docker exec -it jenkins bash
usermod -aG docker jenkins
```

#### SonarQube Connection Issues
```bash
# Check network connectivity
docker exec jenkins ping sonarqube
# Verify SonarQube is healthy
docker-compose ps sonarqube
```

#### Maven Dependency Issues
```bash
# Clear Maven cache
rm -rf ~/.m2/repository
```

#### Memory Issues
```bash
# Increase Docker memory limits
# Edit docker-compose.yml and adjust memory settings
```

### Log Locations

- **Jenkins**: `docker-compose logs jenkins`
- **SonarQube**: `docker-compose logs sonarqube`
- **Docker-in-Docker**: `docker-compose logs docker-dind`

### Debug Mode

Enable debug mode by setting environment variable:
```groovy
environment {
    DEBUG = 'true'
}
```

## 🤝 Contributing

We welcome contributions! Please follow these guidelines:

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes and test thoroughly
4. Commit your changes: `git commit -am 'Add some feature'`
5. Push to the branch: `git push origin feature/your-feature`
6. Submit a pull request

### Code Standards

- Follow Groovy best practices
- Add error handling and logging
- Include documentation for new features
- Test with different project structures

### Testing Changes

1. Test with a sample project
2. Verify all stages work correctly
3. Check error handling scenarios
4. Validate with different branch types

## 📁 Project Structure

```
jenkins-shared-library/
├── vars/                          # Pipeline steps
│   ├── buildStage.groovy
│   ├── checkoutStage.groovy
│   ├── cleanupStage.groovy
│   ├── dockerBuildStage.groovy
│   ├── isReleaseOrTag.groovy
│   ├── notifyStage.groovy
│   ├── packageStage.groovy
│   ├── qualityGateStage.groovy
│   ├── securityScanStage.groovy
│   └── testStage.groovy
├── src/com/jenkins/helpers/       # Helper classes
│   ├── DockerHelper.groovy
│   ├── GitHelper.groovy
│   └── NotifyHelper.groovy
├── docker-compose.yml             # Infrastructure setup
├── Dockerfile.jenkins-with-docker-cli
├── README.md                      # This file
└── LICENSE                       # MIT License
```

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Jenkins community for excellent documentation
- SonarQube team for code quality tools
- Docker team for containerization platform
- Open source community for various plugins and tools

---

**Made with ❤️ for the DevOps community**

For support or questions, please open an issue in the repository or contact the maintainers.