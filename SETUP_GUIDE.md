# Complete Setup Guide

This guide will walk you through setting up the entire Jenkins Shared Library infrastructure from scratch.

## 📋 Prerequisites

Before starting, ensure you have:

- **Docker**: Version 24.0+ installed
- **Docker Compose**: Version 3.8+ support
- **Git**: For repository management
- **At least 8GB RAM**: For running all services
- **At least 20GB disk space**: For volumes and images

## 🚀 Step-by-Step Setup

### Step 1: Clone and Prepare

```bash
# Clone your shared library repository
git clone <your-shared-library-repo>
cd jenkins-shared-library

# Create environment file
cat > .env << EOF
SONAR_DB_PASSWORD=sonar123
REDIS_PASSWORD=redis123
JENKINS_ADMIN_USER=admin
JENKINS_ADMIN_PASSWORD=admin123
EOF

# Make the environment file secure
chmod 600 .env
```

### Step 2: Start Infrastructure

```bash
# Start all services
docker-compose up -d

# Check service status (wait for all to be healthy)
docker-compose ps

# View logs if needed
docker-compose logs -f jenkins
```

Wait for all services to show as "healthy" status. This may take 5-10 minutes for initial setup.

### Step 3: Access Services

| Service | URL | Default Credentials |
|---------|-----|-------------------|
| Jenkins | http://localhost:8080 | admin/admin (after setup) |
| SonarQube | http://localhost:9000 | admin/admin |
| Nexus | http://localhost:8081 | admin/admin123 |
| Portainer | https://localhost:9443 | Create on first visit |

### Step 4: Configure Jenkins

#### 4.1 Initial Setup

1. Access Jenkins at `http://localhost:8080`
2. Get the initial admin password:
   ```bash
   docker-compose exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. Install suggested plugins
4. Create admin user or continue as admin

#### 4.2 Install Required Plugins

Go to **Manage Jenkins** → **Manage Plugins** → **Available** and install:

- Pipeline
- Pipeline: Groovy
- Docker Pipeline
- Docker Commons
- SonarQube Scanner for Jenkins
- JaCoCo plugin
- OWASP Dependency-Check Plugin
- HTML Publisher plugin
- Blue Ocean (optional, for better UI)

#### 4.3 Configure Global Tools

Go to **Manage Jenkins** → **Global Tool Configuration**:

**Maven Configuration:**
- Add Maven → Name: `Maven-3.9.11`
- Install automatically
- Version: 3.9.11

**JDK Configuration:**
- Add JDK → Name: `OpenJDK-21`
- Install automatically
- Version: OpenJDK 21

**Docker Configuration:**
- Usually auto-detected if Docker is available

#### 4.4 Configure SonarQube Integration

1. **Generate SonarQube Token:**
   - Access SonarQube at `http://localhost:9000`
   - Login with `admin/admin`
   - Go to **My Account** → **Security** → **Generate Tokens**
   - Generate token named `jenkins`

2. **Add SonarQube Server in Jenkins:**
   - Go to **Manage Jenkins** → **Configure System**
   - Find **SonarQube servers** section
   - Add SonarQube server:
     - Name: `SonarQube`
     - Server URL: `http://sonarqube:9000`
     - Server authentication token: Add secret text credential with the token

#### 4.5 Setup Shared Library

1. Go to **Manage Jenkins** → **Configure System**
2. Find **Global Pipeline Libraries** section
3. Add library:
   - **Name**: `jenkins-shared-library`
   - **Default version**: `main`
   - **Retrieval method**: Modern SCM
   - **Source Code Management**: Git
   - **Project Repository**: Your shared library repository URL
   - **Credentials**: Add if repository is private

### Step 5: Configure SonarQube

1. Access SonarQube at `http://localhost:9000`
2. Login with `admin/admin`
3. Change default password when prompted
4. Create a new project:
   - Project key: `test-project`
   - Display name: `Test Project`
   - Generate token for Jenkins integration

### Step 6: Configure Nexus Repository

1. Access Nexus at `http://localhost:8081`
2. Sign in with `admin/admin123`
3. Create repositories:
   - **Maven Central Proxy**: For Maven dependencies
   - **Docker Hosted**: For your Docker images
   - **Raw Hosted**: For other artifacts

#### 6.1 Configure Docker Registry

1. Create Docker hosted repository:
   - Name: `docker-hosted`
   - HTTP port: `8082`
   - Enable Docker V1 API: checked

2. Configure Docker client (for development):
   ```bash
   # Add insecure registry to Docker daemon
   echo '{"insecure-registries":["localhost:8082"]}' | sudo tee /etc/docker/daemon.json
   sudo systemctl restart docker
   ```

### Step 7: Test the Setup

#### 7.1 Create Test Pipeline

1. In Jenkins, click **New Item**
2. Enter item name: `test-pipeline`
3. Select **Pipeline** and click OK
4. In Pipeline section, select **Pipeline script** and paste:

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
        IMAGE_NAME       = 'test/hello-world'
        SONARQUBE_SERVER = 'SonarQube'
    }
    
    stages {
        stage('Test Checkout') {
            steps {
                script {
                    // Create a simple test project
                    sh '''
                        mkdir -p src/main/java/com/test
                        cat > src/main/java/com/test/HelloWorld.java << 'EOF'
package com.test;
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
EOF
                        
                        cat > pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.test</groupId>
    <artifactId>hello-world</artifactId>
    <version>1.0.0</version>
    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
    </properties>
</project>
EOF
                    '''
                    echo "✅ Test project created"
                }
            }
        }
        
        stage('Test Build') {
            steps { buildStage(env.SERVICE_DIR) }
        }
    }
    
    post {
        always { cleanupStage() }
    }
}
```

5. Save and click **Build Now**

#### 7.2 Verify Services

Check that all services are working:

```bash
# Check service health
docker-compose ps

# Test connectivity between services
docker-compose exec jenkins ping sonarqube
docker-compose exec jenkins ping nexus
docker-compose exec jenkins curl -f http://sonarqube:9000/api/system/status

# Check Jenkins logs for any errors
docker-compose logs jenkins
```

## 🔧 Customization Options

### Environment Variables

Create custom configurations in `.env`:

```bash
# Custom database passwords
SONAR_DB_PASSWORD=your_secure_password
REDIS_PASSWORD=your_redis_password

# Jenkins customization
JENKINS_ADMIN_USER=your_admin_user
JENKINS_ADMIN_PASSWORD=your_secure_password

# Memory settings
JENKINS_JAVA_OPTS=-Xmx2048m -Xms1024m
SONAR_JAVA_OPTS=-Xmx2048m
NEXUS_JAVA_OPTS=-Xms1024m -Xmx2048m
```

### Volume Customization

To use custom volume locations, modify `docker-compose.yml`:

```yaml
volumes:
  jenkins_home:
    driver: local
    driver_opts:
      o: bind
      type: none
      device: /your/custom/path/jenkins
```

### Network Configuration

For production, consider:
- Using external networks
- Adding SSL certificates
- Configuring firewall rules
- Setting up reverse proxy

## 🔐 Security Considerations

### Production Setup

1. **Change Default Passwords:**
   ```bash
   # Generate secure passwords
   openssl rand -base64 32
   ```

2. **Configure SSL/TLS:**
   - Use proper SSL certificates
   - Configure HTTPS for all services
   - Set up reverse proxy (nginx/traefik)

3. **Network Security:**
   - Use internal networks for service communication
   - Expose only necessary ports
   - Configure firewall rules

4. **Access Control:**
   - Configure Jenkins security realm
   - Set up LDAP/AD integration
   - Use role-based access control

### Backup Strategy

```bash
# Create backup script
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup/jenkins-$DATE"

# Stop services
docker-compose stop

# Backup volumes
docker run --rm -v jenkins_home:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/jenkins_home.tar.gz -C /data .
docker run --rm -v sonarqube_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/sonarqube_data.tar.gz -C /data .
docker run --rm -v nexus_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/nexus_data.tar.gz -C /data .

# Start services
docker-compose start

echo "Backup completed: $BACKUP_DIR"
EOF

chmod +x backup.sh
```

## 🚨 Troubleshooting

### Common Issues and Solutions

#### 1. Jenkins Won't Start
```bash
# Check logs
docker-compose logs jenkins

# Check volume permissions
docker-compose exec jenkins ls -la /var/jenkins_home

# Fix permissions if needed
docker-compose exec jenkins chown -R jenkins:jenkins /var/jenkins_home
```

#### 2. SonarQube Database Connection Issues
```bash
# Check database status
docker-compose logs sonarqube_db

# Restart database
docker-compose restart sonarqube_db sonarqube
```

#### 3. Docker-in-Docker Issues
```bash
# Check Docker socket
docker-compose exec jenkins ls -la /var/run/docker.sock

# Check Docker daemon
docker-compose exec jenkins docker info
```

#### 4. Memory Issues
```bash
# Check current memory usage
docker stats

# Increase memory limits in docker-compose.yml
# Add under service:
deploy:
  resources:
    limits:
      memory: 4G
```

#### 5. Network Connectivity Issues
```bash
# Check network
docker network ls
docker network inspect jenkins-shared-library_jenkins_network

# Test connectivity
docker-compose exec jenkins ping sonarqube
docker-compose exec jenkins nslookup nexus
```

### Performance Tuning

1. **Java Memory Settings:**
   ```yaml
   environment:
     - JAVA_OPTS=-Xmx2048m -Xms1024m -XX:MaxPermSize=512m
   ```

2. **Maven Memory:**
   ```bash
   export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
   ```

3. **Docker Limits:**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2.0'
         memory: 4G
   ```

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Review service logs: `docker-compose logs [service-name]`
3. Check service health: `docker-compose ps`
4. Open an issue in the repository with:
   - Error messages
   - Service logs
   - System information
   - Steps to reproduce

## 🎉 Next Steps

After successful setup:

1. Create your first real pipeline
2. Configure additional notifications (Slack, email)
3. Set up automated backups
4. Configure monitoring and alerting
5. Add more projects and services
6. Customize the shared library for your needs

Happy building! 🚀