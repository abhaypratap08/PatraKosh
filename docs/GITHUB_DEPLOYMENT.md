# GitHub Pages Deployment Guide

This guide will help you deploy PatraKosh to GitHub Pages with a custom `.io` domain.

## 🚀 Quick Start: GitHub Pages Deployment

### Step 1: Create GitHub Repository
1. Go to [GitHub](https://github.com) and sign in
2. Click **"New repository"**
3. Repository name: `patrakosh`
4. Description: `Secure File Storage Application`
5. Set to **Public**
6. Click **"Create repository"**

### Step 2: Push Your Code
```bash
# Initialize git if not already done
git init
git add .
git commit -m "Initial commit: PatraKosh file storage application"

# Add remote (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/patrakosh.git
git branch -M main
git push -u origin main
```

### Step 3: Enable GitHub Pages
1. Go to your repository: `https://github.com/YOUR_USERNAME/patrakosh`
2. Click **Settings** tab
3. Scroll down to **"Pages"** section
4. Under **"Build and deployment"**:
   - Source: **Deploy from a branch**
   - Branch: **main**
   - Folder: **/root**
   - Click **Save**

### Step 4: Wait for Deployment
- GitHub will build and deploy your site
- This usually takes 1-2 minutes
- Your site will be available at: `https://YOUR_USERNAME.github.io/patrakosh`

## 🌐 Custom .io Domain Setup

### Option 1: GitHub's Free .io Domain
If your username is `patrakosh`, you get:
- **Site URL**: `https://patrakosh.github.io`
- **Repository**: Must be named `patrakosh.github.io`

#### Setup Steps:
1. Create repository named `patrakosh.github.io`
2. Push your code to this repository
3. Enable GitHub Pages (it auto-enables for user/organization sites)
4. Your site will be live at `https://patrakosh.github.io`

### Option 2: Custom Domain with .io TLD
For a custom domain like `patrakosh.io`:

#### Purchase Domain
1. Buy domain from registrar (Namecheap, GoDaddy, etc.)
2. Point DNS to GitHub Pages

#### DNS Configuration
Add these DNS records at your domain registrar:

```
Type: A
Name: @
Value: 185.199.108.153
TTL: 3600

Type: A
Name: @
Value: 185.199.109.153
TTL: 3600

Type: A
Name: @
Value: 185.199.110.153
TTL: 3600

Type: A
Name: @
Value: 185.199.111.153
TTL: 3600

Type: CNAME
Name: www
Value: YOUR_USERNAME.github.io
TTL: 3600
```

#### Configure in GitHub
1. Go to repository **Settings** → **Pages**
2. Under **"Custom domain"**:
   - Enter: `patrakosh.io`
   - Click **Add domain**
3. GitHub will show DNS records to add
4. Wait for DNS propagation (usually 24-48 hours)

## 🔧 Automated Deployment (Recommended)

The `.github/workflows/deploy.yml` file enables automatic deployment:

### How it Works:
1. **Push to main** → Triggers GitHub Actions
2. **Build step** → Creates optimized build
3. **Deploy step** → Publishes to GitHub Pages
4. **Live URL** → Automatically updated

### Benefits:
- ✅ Automatic deployments
- ✅ No manual steps needed
- ✅ Rollback capability
- ✅ Build status tracking

## 📱 Testing Your Deployment

### Local Testing
```bash
# Start local server
python -m http.server 8080

# Open browser
open http://localhost:8080
```

### Production Testing
1. Visit your deployed URL
2. Test all features:
   - Login/Signup
   - File upload
   - File operations (rename, delete, share)
   - Search functionality
   - Mobile responsiveness

## 🛠️ Troubleshooting

### Common Issues:

#### 1. 404 Errors
**Problem**: Pages not found after deployment
**Solution**: 
- Check repository name matches URL
- Ensure `index.html` is in root
- Verify GitHub Pages is enabled

#### 2. Jekyll Build Errors
**Problem**: GitHub tries to build as Jekyll site
**Solution**: Add `.nojekyll` file:
```bash
touch .nojekyll
git add .nojekyll
git commit -m "Disable Jekyll"
git push
```

#### 3. CSS/JS Not Loading
**Problem**: Assets not found
**Solution**:
- Check file paths are relative
- Verify assets are committed
- Check case sensitivity

#### 4. Custom Domain Not Working
**Problem**: DNS not resolving
**Solution**:
- Wait 24-48 hours for propagation
- Verify DNS records
- Check GitHub Pages settings
- Use `dig` command to verify:
```bash
dig patrakosh.io
```

### Debug Commands:
```bash
# Check GitHub Pages status
curl -I https://YOUR_USERNAME.github.io/patrakosh

# Check DNS propagation
nslookup patrakosh.io

# Verify SSL certificate
openssl s_client -connect patrakosh.io:443
```

## 📊 Analytics and Monitoring

### GitHub Pages Analytics
1. Go to repository **Settings** → **Pages**
2. Scroll to **"GitHub Pages analytics"**
3. View traffic data

### Google Analytics
Add to your `index.html`:
```html
<!-- Google Analytics -->
<script async src="https://www.googletagmanager.com/gtag/js?id=GA_MEASUREMENT_ID"></script>
<script>
  window.dataLayer = window.dataLayer || [];
  function gtag(){dataLayer.push(arguments);}
  gtag('js', new Date());
  gtag('config', 'GA_MEASUREMENT_ID');
</script>
```

## 🔒 Security Considerations

### HTTPS Enforcement
GitHub Pages automatically:
- ✅ Provides SSL certificates
- ✅ Redirects HTTP to HTTPS
- ✅ Supports HSTS

### Content Security
- No server-side code execution
- Static files only
- Safe for public file storage demo

## 🚀 Advanced Features

### Custom 404 Page
Create `404.html`:
```html
<!DOCTYPE html>
<html>
<head>
    <title>Page Not Found - PatraKosh</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; }
        h1 { color: #0061ff; }
        a { color: #0061ff; text-decoration: none; }
    </style>
</head>
<body>
    <h1>404 - Page Not Found</h1>
    <p>The page you're looking for doesn't exist.</p>
    <a href="/">Go to PatraKosh Home</a>
</body>
</html>
```

### Environment Variables
For production configuration:
```javascript
// In your JavaScript
const isProduction = window.location.hostname.includes('github.io');
const API_URL = isProduction 
  ? 'https://your-api.com' 
  : 'http://localhost:8080';
```

## 📋 Deployment Checklist

### Pre-deployment:
- [ ] All files committed to git
- [ ] Repository is public
- [ ] GitHub Pages enabled
- [ ] Custom domain configured (if needed)
- [ ] DNS records set (if using custom domain)

### Post-deployment:
- [ ] Site loads correctly
- [ ] All features work
- [ ] Mobile responsive
- [ ] SSL certificate valid
- [ ] Analytics working

## 🆘 Emergency Rollback

If deployment fails:
```bash
# Revert to previous commit
git log --oneline
git checkout PREVIOUS_COMMIT_HASH
git push --force-with-lease

# Or disable GitHub Pages temporarily
# Go to Settings → Pages → Source: None
```

---

## 🎉 Success!

Once deployed, your PatraKosh application will be available at:
- **GitHub Pages**: `https://YOUR_USERNAME.github.io/patrakosh`
- **Custom .io**: `https://patrakosh.io`

Visitors can immediately use all file management features without any installation!
