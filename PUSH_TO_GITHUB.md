# Push Privacy Policy to GitHub Pages - Quick Guide

## Option 1: Use the Setup Script (Easiest)

Run the interactive setup script:

```bash
./setup-github-pages.sh
```

The script will:
- Ask for your GitHub username
- Ask for your repository name
- Ask for your email address
- Update all files automatically
- Configure Git remote
- Show you the next steps

## Option 2: Manual Setup

### Step 1: Update Your Email

Replace `your-email@example.com` with your actual email:

```bash
# macOS/Linux
sed -i '' 's/your-email@example.com/YOUR_ACTUAL_EMAIL/g' privacy-policy.html
sed -i '' 's/your-email@example.com/YOUR_ACTUAL_EMAIL/g' PRIVACY_POLICY.md

# Or edit manually in your editor
```

### Step 2: Create GitHub Repository

1. Go to https://github.com/new
2. Repository name: `linkedin-comment-responder` (or your choice)
3. Description: "AI-powered LinkedIn comment response automation"
4. Visibility: **Public** (required for free GitHub Pages)
5. Do NOT check "Initialize with README"
6. Click **Create repository**

### Step 3: Configure Git Remote

```bash
# Remove old remote
git remote remove origin

# Add GitHub remote (replace YOUR_USERNAME and REPO_NAME)
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Verify
git remote -v
```

### Step 4: Push to GitHub

```bash
# Add all files
git add .

# Commit
git commit -m "Initial commit: LinkedIn Comment Responder with privacy policy"

# Rename branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

### Step 5: Enable GitHub Pages

1. Go to your repository: `https://github.com/YOUR_USERNAME/REPO_NAME`
2. Click **Settings** (top navigation)
3. Click **Pages** (left sidebar)
4. Under "Build and deployment":
   - Source: **Deploy from a branch**
   - Branch: **main**
   - Folder: **/ (root)**
5. Click **Save**
6. Wait 2-3 minutes

### Step 6: Get Your Privacy Policy URL

Your privacy policy will be at:
```
https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

Example:
```
https://saranvemu.github.io/linkedin-comment-responder/privacy-policy.html
```

### Step 7: Test It

```bash
# Test the URL (replace with yours)
curl -I https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

Should return: `HTTP/2 200`

### Step 8: Add to LinkedIn

1. Go to https://www.linkedin.com/developers/apps
2. Select your app
3. Click **Settings** tab
4. Add Privacy Policy URL
5. Click **Update**

## Quick Reference

```bash
# Update email
sed -i '' 's/your-email@example.com/YOUR_EMAIL/g' privacy-policy.html

# Set up GitHub
git remote remove origin
git remote add origin https://github.com/USERNAME/REPO.git

# Push
git add .
git commit -m "Add privacy policy"
git branch -M main
git push -u origin main
```

## Your Privacy Policy URL

```
https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

---

**Need help?** Run `./setup-github-pages.sh` for an interactive setup!
