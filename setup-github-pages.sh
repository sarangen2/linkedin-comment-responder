#!/bin/bash

# GitHub Pages Setup Script for LinkedIn Comment Responder Privacy Policy
# This script helps you push your privacy policy to GitHub Pages

echo "=========================================="
echo "GitHub Pages Setup for Privacy Policy"
echo "=========================================="
echo ""

# Step 1: Get user information
echo "Step 1: Configuration"
echo "---------------------"
read -p "Enter your GitHub username: " GITHUB_USERNAME
read -p "Enter your repository name (e.g., linkedin-comment-responder): " REPO_NAME
read -p "Enter your email address for privacy policy: " USER_EMAIL

echo ""
echo "Configuration:"
echo "  GitHub Username: $GITHUB_USERNAME"
echo "  Repository Name: $REPO_NAME"
echo "  Email: $USER_EMAIL"
echo ""
read -p "Is this correct? (y/n): " CONFIRM

if [ "$CONFIRM" != "y" ]; then
    echo "Setup cancelled. Please run the script again."
    exit 1
fi

# Step 2: Update email in privacy policy files
echo ""
echo "Step 2: Updating email in privacy policy files..."
sed -i '' "s/your-email@example.com/$USER_EMAIL/g" privacy-policy.html
sed -i '' "s/your-email@example.com/$USER_EMAIL/g" PRIVACY_POLICY.md
echo "✓ Email updated"

# Step 3: Update repository URL placeholders
echo ""
echo "Step 3: Updating repository URLs..."
sed -i '' "s|https://github.com/your-repo|https://github.com/$GITHUB_USERNAME/$REPO_NAME|g" privacy-policy.html
sed -i '' "s|https://github.com/your-repo|https://github.com/$GITHUB_USERNAME/$REPO_NAME|g" PRIVACY_POLICY.md
echo "✓ Repository URLs updated"

# Step 4: Set up Git remote
echo ""
echo "Step 4: Setting up Git remote..."
git remote remove origin 2>/dev/null
git remote add origin "https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
echo "✓ Git remote configured"

# Step 5: Show next steps
echo ""
echo "=========================================="
echo "Setup Complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo ""
echo "1. Create the repository on GitHub:"
echo "   - Go to: https://github.com/new"
echo "   - Name: $REPO_NAME"
echo "   - Visibility: Public"
echo "   - Do NOT initialize with README"
echo "   - Click 'Create repository'"
echo ""
echo "2. Push your code:"
echo "   git add ."
echo "   git commit -m 'Initial commit: LinkedIn Comment Responder with privacy policy'"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "3. Enable GitHub Pages:"
echo "   - Go to: https://github.com/$GITHUB_USERNAME/$REPO_NAME/settings/pages"
echo "   - Source: Deploy from a branch"
echo "   - Branch: main, Folder: / (root)"
echo "   - Click Save"
echo ""
echo "4. Your privacy policy URL will be:"
echo "   https://$GITHUB_USERNAME.github.io/$REPO_NAME/privacy-policy.html"
echo ""
echo "5. Add this URL to LinkedIn Developer Portal:"
echo "   - Go to: https://www.linkedin.com/developers/apps"
echo "   - Select your app → Settings"
echo "   - Add Privacy Policy URL"
echo ""
echo "=========================================="
