#!/bin/bash

echo "=========================================="
echo "Push to GitHub - LinkedIn Comment Responder"
echo "=========================================="
echo ""

# Configuration
GITHUB_USERNAME="sarangen2"
REPO_NAME="linkedin-comment-responder"
USER_EMAIL="vsarankumar2003@gmail.com"

echo "Configuration:"
echo "  GitHub Username: $GITHUB_USERNAME"
echo "  Repository Name: $REPO_NAME"
echo "  Email: $USER_EMAIL"
echo ""

# Step 1: Remove old remote
echo "Step 1: Removing old remote..."
git remote remove origin 2>/dev/null
echo "✓ Old remote removed"

# Step 2: Add GitHub remote
echo ""
echo "Step 2: Adding GitHub remote..."
git remote add origin "https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
echo "✓ GitHub remote added"

# Step 3: Show remote
echo ""
echo "Step 3: Verifying remote..."
git remote -v

# Step 4: Instructions for next steps
echo ""
echo "=========================================="
echo "Next Steps - Run These Commands:"
echo "=========================================="
echo ""
echo "1. First, create the repository on GitHub:"
echo "   Go to: https://github.com/new"
echo "   - Name: $REPO_NAME"
echo "   - Visibility: Private (or Public for free GitHub Pages)"
echo "   - Do NOT initialize with README"
echo "   - Click 'Create repository'"
echo ""
echo "2. Then run these commands to push:"
echo ""
echo "   git add ."
echo "   git commit -m 'Initial commit: LinkedIn Comment Responder with privacy policy'"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. If prompted for credentials:"
echo "   - Username: $GITHUB_USERNAME"
echo "   - Password: Use a Personal Access Token"
echo "   - Generate token at: https://github.com/settings/tokens"
echo ""
echo "4. Enable GitHub Pages:"
echo "   - Go to: https://github.com/$GITHUB_USERNAME/$REPO_NAME/settings/pages"
echo "   - Source: Deploy from a branch"
echo "   - Branch: main, Folder: / (root)"
echo "   - Click Save"
echo "   - Note: Requires GitHub Pro for private repos"
echo ""
echo "5. Your privacy policy URL will be:"
echo "   https://$GITHUB_USERNAME.github.io/$REPO_NAME/privacy-policy.html"
echo ""
echo "=========================================="
echo ""
echo "✅ Email already updated in privacy policy files"
echo "✅ Git remote configured"
echo ""
echo "Ready to push? Follow the steps above!"
echo ""
