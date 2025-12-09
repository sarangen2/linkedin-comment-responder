# GitHub Pages Setup Instructions

Follow these steps to publish your privacy policy to GitHub Pages.

## Prerequisites

- GitHub account
- Git installed (you have this ✓)
- Privacy policy files ready (you have this ✓)

## Step-by-Step Instructions

### Step 1: Create a New GitHub Repository

1. Go to [GitHub](https://github.com)
2. Click the **+** icon (top right) → **New repository**
3. Repository settings:
   - **Name**: `linkedin-comment-responder` (or your preferred name)
   - **Description**: "AI-powered LinkedIn comment response automation"
   - **Visibility**: Public (required for free GitHub Pages)
   - **Initialize**: Do NOT check any boxes (we already have files)
4. Click **Create repository**

### Step 2: Update Your Email in Privacy Policy

Before pushing, update the placeholder email:

```bash
# Replace with your actual email
sed -i '' 's/your-email@example.com/YOUR_ACTUAL_EMAIL@example.com/g' privacy-policy.html
sed -i '' 's/your-email@example.com/YOUR_ACTUAL_EMAIL@example.com/g' PRIVACY_POLICY.md
```

### Step 3: Initialize Git for This Project

```bash
# Remove old remote (Bitbucket)
git remote remove origin

# Add your new GitHub repository as origin
# Replace YOUR_USERNAME and REPO_NAME with your actual values
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Verify the remote
git remote -v
```

### Step 4: Add and Commit Files

```bash
# Add all project files
git add .

# Commit with a message
git commit -m "Initial commit: LinkedIn Comment Responder with privacy policy"

# Push to GitHub
git push -u origin main
```

If you get an error about branch name, try:
```bash
# Rename branch to main if needed
git branch -M main
git push -u origin main
```

### Step 5: Enable GitHub Pages

1. Go to your repository on GitHub
2. Click **Settings** (top navigation)
3. Scroll down and click **Pages** (left sidebar)
4. Under "Build and deployment":
   - **Source**: Deploy from a branch
   - **Branch**: main
   - **Folder**: / (root)
5. Click **Save**
6. Wait 2-3 minutes for deployment

### Step 6: Get Your Privacy Policy URL

After deployment completes, your privacy policy will be available at:

```
https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

Example:
```
https://saranvemu.github.io/linkedin-comment-responder/privacy-policy.html
```

### Step 7: Test Your URL

```bash
# Test that it's accessible (replace with your actual URL)
curl -I https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

Should return: `HTTP/2 200`

### Step 8: Add to LinkedIn Developer Portal

1. Go to [LinkedIn Developers](https://www.linkedin.com/developers/apps)
2. Select your app
3. Click **Settings** tab
4. Find "Privacy Policy URL" field
5. Paste your GitHub Pages URL
6. Click **Update**

## Troubleshooting

### Issue: "Repository already exists"
- Choose a different repository name
- Or use an existing repository

### Issue: "Permission denied"
- Make sure you're logged into GitHub
- You may need to set up SSH keys or use a personal access token

### Issue: "Branch 'main' doesn't exist"
```bash
git branch -M main
git push -u origin main
```

### Issue: "GitHub Pages not showing"
- Wait 5-10 minutes for initial deployment
- Check Settings → Pages for deployment status
- Ensure repository is public

## Quick Commands Reference

```bash
# Update email in privacy policy
sed -i '' 's/your-email@example.com/YOUR_EMAIL@example.com/g' privacy-policy.html

# Set up GitHub remote
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/REPO_NAME.git

# Push to GitHub
git add .
git commit -m "Add privacy policy"
git push -u origin main
```

## Your Privacy Policy URL Format

```
https://YOUR_USERNAME.github.io/REPO_NAME/privacy-policy.html
```

This is the URL you'll provide to LinkedIn Developer Portal.

---

**Need help?** See [PRIVACY_POLICY_SETUP.md](PRIVACY_POLICY_SETUP.md) for more details.
