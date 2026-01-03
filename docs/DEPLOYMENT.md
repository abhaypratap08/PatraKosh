# Deployment Guide for PatraKosh.io

This guide covers deploying PatraKosh to a production environment with a .io domain for public access.

## 🌐 Production Deployment Options

### Option 1: Static Hosting (Recommended for Demo)
For the frontend demo at patrakosh.io, we recommend using static hosting services.

#### Netlify Deployment
1. **Build the frontend**
   ```bash
   # Create a production build
   mkdir dist
   cp index.html dist/
   cp -r assets dist/  # if you have assets folder
   ```

2. **Deploy to Netlify**
   ```bash
   # Install Netlify CLI
   npm install -g netlify-cli
   
   # Deploy
   netlify deploy --prod --dir=dist
   ```

3. **Configure Custom Domain**
   - Add `patrakosh.io` in Netlify dashboard
   - Update DNS records to point to Netlify

#### Vercel Deployment
```bash
# Install Vercel CLI
npm i -g vercel

# Deploy
vercel --prod
```

### Option 2: Full Stack Deployment
For complete backend + frontend deployment:

#### Docker Deployment
1. **Create Dockerfile**
   ```dockerfile
   # Multi-stage build for production
   FROM openjdk:17-jdk-slim as build
   WORKDIR /app
   COPY pom.xml .
   COPY src ./src
   RUN mvn clean package -DskipTests

   FROM openjdk:17-jre-slim
   WORKDIR /app
   COPY --from=build /app/target/*.jar app.jar
   EXPOSE 8080
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

2. **Create docker-compose.yml**
   ```yaml
   version: '3.8'
   services:
     app:
       build: .
       ports:
         - "8080:8080"
       environment:
         - SPRING_PROFILES_ACTIVE=prod
       depends_on:
         - db
     
     db:
       image: mysql:8.0
       environment:
         MYSQL_ROOT_PASSWORD: your_password
         MYSQL_DATABASE: patrakosh_db
       volumes:
         - mysql_data:/var/lib/mysql

   volumes:
     mysql_data:
   ```

3. **Deploy to Cloud**
   ```bash
   docker-compose up -d
   ```

#### Cloud Platform Deployment

##### AWS EC2 + RDS
1. **Launch EC2 Instance**
   - Ubuntu 20.04 LTS
   - t3.micro (for demo)
   - Security Group: Open 80, 443, 8080

2. **Setup RDS Database**
   - MySQL 8.0
   - Multi-AZ for production
   - Configure security groups

3. **Deploy Application**
   ```bash
   # SSH into EC2
   ssh -i your-key.pem ubuntu@your-ec2-ip
   
   # Install Java and Maven
   sudo apt update
   sudo apt install openjdk-17-jdk maven
   
   # Clone and build
   git clone https://github.com/yourusername/PatraKosh.git
   cd PatraKosh
   mvn clean package -DskipTests
   
   # Run with production profile
   java -jar target/patrakosh-*.jar --spring.profiles.active=prod
   ```

##### Heroku Deployment
```bash
# Install Heroku CLI
# Create Procfile
echo "web: java -jar target/patrakosh-*.jar --server.port=$PORT" > Procfile

# Deploy
heroku create patrakosh
git push heroku main
```

## 🔧 Production Configuration

### Application Properties
Create `application-prod.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://your-production-db:3306/patrakosh_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# File Storage
storage.type=s3
storage.bucket=patrakosh-files
storage.region=us-east-1

# Server Configuration
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.root=WARN
logging.level.com.patrakosh=INFO

# Performance
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

### Environment Variables
Set these in your hosting environment:

```bash
# Database
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_password

# Security
JWT_SECRET=your_256_bit_secret_key

# Storage (if using S3)
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
S3_BUCKET_NAME=patrakosh-files
```

## 🌐 Domain Setup

### DNS Configuration for patrakosh.io

#### If using Netlify/Vercel
1. **Add domain in hosting panel**
2. **Update DNS records**
   ```
   Type: CNAME
   Name: @
   Value: netlify-provider-domain.com
   
   Type: CNAME
   Name: www
   Value: netlify-provider-domain.com
   ```

#### If using custom server
1. **A Record**
   ```
   Type: A
   Name: @
   Value: your-server-ip
   TTL: 300
   ```

2. **AAAA Record** (for IPv6)
   ```
   Type: AAAA
   Name: @
   Value: your-ipv6-address
   TTL: 300
   ```

### SSL Certificate
Most hosts provide free SSL certificates:
- **Let's Encrypt** (free, auto-renew)
- **Cloudflare** (free with proxy)
- **AWS Certificate Manager** (free for AWS resources)

## 📊 Monitoring and Analytics

### Application Monitoring
```bash
# Add Spring Boot Actuator
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

# Configure endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### Logging Setup
```properties
# Production logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=/var/log/patrakosh/application.log
```

### Performance Monitoring
Consider adding:
- **New Relic** for APM
- **Datadog** for metrics
- **Sentry** for error tracking
- **Google Analytics** for user analytics

## 🔒 Security Hardening

### Web Server Security
```nginx
# Nginx configuration
server {
    listen 443 ssl http2;
    server_name patrakosh.io www.patrakosh.io;
    
    # SSL configuration
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    
    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";
    
    # Proxy to Spring Boot
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### Firewall Rules
```bash
# UFW setup
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable
```

## 🚀 CI/CD Pipeline

### GitHub Actions
Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      
  deploy:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Deploy to Netlify
        uses: netlify/actions/cli@master
        with:
          args: deploy --prod --dir=.
        env:
          NETLIFY_AUTH_TOKEN: ${{ secrets.NETLIFY_AUTH_TOKEN }}
          NETLIFY_SITE_ID: ${{ secrets.NETLIFY_SITE_ID }}
```

## 📈 Performance Optimization

### Frontend Optimization
```bash
# Minify CSS/JS
npm install -g minify
minify index.html > index.min.html

# Enable compression
gzip -k index.html
```

### Backend Optimization
```properties
# Connection pooling
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Cache configuration
spring.cache.type=redis
spring.redis.host=localhost
spring.redis.port=6379
```

## 🔍 Testing Production

### Health Checks
```bash
# Application health
curl https://patrakosh.io/actuator/health

# SSL certificate
openssl s_client -connect patrakosh.io:443 -servername patrakosh.io

# Performance test
ab -n 1000 -c 10 https://patrakosh.io/
```

### Monitoring Setup
- **Uptime monitoring**: UptimeRobot, Pingdom
- **Error tracking**: Sentry, Bugsnag
- **Performance**: Google PageSpeed Insights
- **Security**: SSL Labs, SecurityHeaders.com

## 📋 Deployment Checklist

### Pre-deployment
- [ ] All tests passing
- [ ] Environment variables configured
- [ ] Database backed up
- [ ] SSL certificates ready
- [ ] DNS records configured
- [ ] Monitoring tools set up

### Post-deployment
- [ ] Health checks passing
- [ ] SSL certificate valid
- [ ] Performance metrics collected
- [ ] Error tracking active
- [ ] Backup procedures verified

## 🆘 Troubleshooting

### Common Issues
1. **Database connection failed**
   - Check credentials in environment variables
   - Verify database is accessible
   - Check firewall rules

2. **File upload not working**
   - Verify storage permissions
   - Check disk space
   - Review file size limits

3. **SSL certificate issues**
   - Verify certificate chain
   - Check domain ownership
   - Review SSL configuration

### Emergency Procedures
```bash
# Quick rollback
git checkout previous-stable-tag
docker-compose up -d

# Database restore
mysql -u root -p patrakosh_db < backup.sql

# Check logs
docker-compose logs -f app
```

---

This deployment guide ensures PatraKosh runs reliably in production with proper security, monitoring, and performance optimization.
