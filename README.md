# PatraKosh - Secure File Storage Application

A modern, Dropbox-inspired file storage application built with a focus on simplicity, security, and user experience. PatraKosh provides comprehensive file management capabilities with a clean, intuitive interface.

## 🚀 Features

### Core Functionality
- **Authentication System**: Secure login and signup with JWT-based session management
- **File Management**: Upload, download, rename, delete, and duplicate files
- **Advanced Search**: Real-time file search and filtering
- **Sharing System**: Public and private file sharing with shareable links
- **Storage Analytics**: Real-time storage usage statistics and file tracking
- **Drag & Drop**: Intuitive file upload with progress tracking
- **Responsive Design**: Fully responsive interface for desktop and mobile devices

### User Experience
- **Modern UI**: Clean, minimalist design with smooth animations
- **Toast Notifications**: Non-intrusive feedback for all user actions
- **Context Menus**: Right-click style dropdown menus for file operations
- **Progress Tracking**: Visual feedback for upload and download operations
- **Persistent Storage**: Files and user data persist across sessions

## 🛠️ Tech Stack

### Frontend
- **HTML5**: Semantic markup with modern web standards
- **CSS3**: Custom CSS with CSS variables and modern layout techniques
- **Vanilla JavaScript**: No framework dependencies for maximum performance
- **LocalStorage**: Client-side persistence for files and user sessions

### Backend (Spring Boot)
- **Java 17**: Modern Java with latest language features
- **Spring Boot 3.x**: Enterprise-grade application framework
- **Spring Security**: Authentication and authorization
- **JWT**: Token-based authentication
- **MySQL**: Relational database for data persistence
- **Maven**: Dependency management and build automation

### Infrastructure
- **MinIO/S3**: Object storage for file management
- **RESTful API**: Clean, well-documented API endpoints
- **Database Schema**: Comprehensive schema with views and stored procedures

## 📁 Project Structure

```
PatraKosh/
├── src/main/java/com/patrakosh/
│   ├── api/                    # REST API controllers
│   ├── service/                # Business logic layer
│   ├── repository/             # Data access layer
│   ├── model/                 # Entity classes
│   └── config/                # Configuration classes
├── src/main/resources/
│   ├── application.properties    # Application configuration
│   └── database_schema.sql     # Database schema
├── index.html                 # Frontend application
├── pom.xml                   # Maven configuration
└── README.md                 # This file
```

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- Node.js (for development tools)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/PatraKosh.git
   cd PatraKosh
   ```

2. **Database Setup**
   ```bash
   mysql -u root -p
   CREATE DATABASE patrakosh_db;
   SOURCE src/main/resources/database_schema.sql;
   ```

3. **Configure Application**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/patrakosh_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access the Application**
   Open your browser and navigate to `http://localhost:8080`

## 🌐 Live Demo

Visit our live demo: **https://patrakosh.github.io**

### Demo Features
- Full functionality without registration required
- Test file upload and management
- Experience the complete user interface
- Sample files pre-loaded for testing

### Quick Access
- **GitHub Pages**: https://patrakosh.github.io
- **Custom Domain**: https://patrakosh.io (when configured)

## 🚀 GitHub Deployment

### Automatic Deployment
This repository includes GitHub Actions for automatic deployment to GitHub Pages:

1. **Push to main branch** → Auto-deploys to GitHub Pages
2. **Pull requests** → Preview builds
3. **Zero configuration** → Works out of the box

### Manual Deployment Steps
```bash
# 1. Create repository on GitHub
git clone https://github.com/YOUR_USERNAME/patrakosh.git
cd patrakosh

# 2. Add your files
git add .
git commit -m "Deploy PatraKosh file storage application"
git push origin main

# 3. Enable GitHub Pages
# Go to Settings → Pages → Source: Deploy from branch → main
```

### Custom .io Domain
For `patrakosh.io` domain:
1. Create repository: `patrakosh.github.io`
2. Push code to this repository
3. Configure DNS records (see `docs/GITHUB_DEPLOYMENT.md`)
4. Site will be live at `https://patrakosh.io`

**See `docs/GITHUB_DEPLOYMENT.md` for detailed deployment instructions.**

## 📊 Database Schema

The application uses a comprehensive database schema with the following key tables:

- **users**: User accounts and authentication
- **files**: File metadata and storage information
- **folders**: Hierarchical folder structure
- **shares**: File sharing configurations
- **versions**: File versioning system
- **activity_logs**: User activity tracking
- **sessions**: User session management

## 🔧 API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `POST /api/auth/logout` - User logout

### File Management
- `GET /api/files` - List user files
- `POST /api/files/upload` - Upload files
- `GET /api/files/{id}/download` - Download file
- `PUT /api/files/{id}/rename` - Rename file
- `DELETE /api/files/{id}` - Delete file
- `POST /api/files/{id}/duplicate` - Duplicate file

### Sharing
- `POST /api/files/{id}/share` - Create share link
- `GET /api/shared/{token}` - Access shared file

## 🎨 Frontend Features

### Modern Design Elements
- **CSS Variables**: Consistent theming and easy customization
- **Responsive Grid**: Flexible layout system
- **Smooth Animations**: CSS transitions and keyframe animations
- **Component-based**: Modular, reusable UI components

### Interactive Elements
- **Drag & Drop**: HTML5 File API integration
- **Progress Bars**: Visual upload/download progress
- **Modal Dialogs**: Contextual user interactions
- **Toast Notifications**: Non-blocking user feedback

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Input Validation**: Client and server-side validation
- **XSS Protection**: Cross-site scripting prevention
- **CSRF Protection**: Cross-site request forgery prevention
- **Secure File Storage**: Sandboxed file access

## 📱 Mobile Compatibility

The application is fully responsive and optimized for mobile devices:
- Touch-friendly interface
- Adaptive layouts
- Optimized performance
- Mobile-specific interactions

## 🧪 Testing

### Frontend Testing
```bash
# Install dependencies
npm install

# Run tests
npm test

# Start development server
npm start
```

### Backend Testing
```bash
# Run unit tests
mvn test

# Run integration tests
mvn verify

# Generate test coverage report
mvn jacoco:report
```

## 📈 Performance

### Optimization Features
- **Lazy Loading**: On-demand file loading
- **Caching**: Browser and server-side caching
- **Compression**: Gzip compression for assets
- **Minification**: Optimized CSS and JavaScript

### Metrics
- **Page Load**: < 2 seconds initial load
- **File Upload**: Progress tracking with resume capability
- **Search**: Real-time search with debouncing
- **Memory Usage**: Optimized for large file collections

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow the existing code style
- Write tests for new features
- Update documentation
- Ensure all tests pass

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Boot Team**: For the excellent framework
- **MySQL Team**: For the reliable database
- **Open Source Community**: For the amazing tools and libraries

## 📞 Support

- **Documentation**: [Wiki](https://github.com/yourusername/PatraKosh/wiki)
- **Issues**: [GitHub Issues](https://github.com/yourusername/PatraKosh/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/PatraKosh/discussions)

## 🗺️ Roadmap

### Upcoming Features
- [ ] Real-time collaboration
- [ ] Advanced file versioning
- [ ] Mobile applications (iOS/Android)
- [ ] Enterprise SSO integration
- [ ] Advanced analytics dashboard
- [ ] File encryption
- [ ] API rate limiting
- [ ] Bulk operations

### Performance Improvements
- [ ] CDN integration
- [ ] Database optimization
- [ ] Caching improvements
- [ ] Load balancing

---

**PatraKosh** - Secure, Simple, Powerful File Storage

Made with ❤️ by the PatraKosh Team