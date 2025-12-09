# Enable GitHub Pages for Privacy Policy

Your privacy policy HTML file is ready and pushed to GitHub. Follow these steps to enable GitHub Pages:

## Quick Steps

### 1. Go to Repository Settings
Visit: https://github.com/sarangen2/linkedin-comment-responder/settings/pages

### 2. Configure GitHub Pages
- **Source**: Select "Deploy from a branch"
- **Branch**: Select "main"
- **Folder**: Select "/ (root)"
- Click **Save**

### 3. Wait for Deployment
- GitHub Pages will take 1-3 minutes to deploy
- You'll see a message: "Your site is live at..."

### 4. Your Privacy Policy URL
Once deployed, your privacy policy will be available at:
```
https://sarangen2.github.io/linkedin-comment-responder/privacy-policy.html
```

### 5. Verify Deployment
Test the URL after a few minutes:
```bash
curl -I https://sarangen2.github.io/linkedin-comment-responder/privacy-policy.html
```

You should see: `HTTP/2 200`

## Alternative: Using GitHub CLI

If you have GitHub CLI installed, you can enable it via command line:

```bash
# Enable GitHub Pages
gh api repos/sarangen2/linkedin-comment-responder/pages \
  -X POST \
  -f source[branch]=main \
  -f source[path]=/
```

## Add to LinkedIn Developer Portal

Once GitHub Pages is live:

1. Go to: https://www.linkedin.com/developers/apps
2. Select your app
3. Click **Settings** tab
4. Add Privacy Policy URL:
   ```
   https://sarangen2.github.io/linkedin-comment-responder/privacy-policy.html
   ```
5. Click **Update**

## Troubleshooting

### Pages Not Showing Up
- Wait 3-5 minutes after enabling
- Check repository visibility (must be Public for free GitHub Pages)
- Verify the file exists in the main branch

### 404 Error
- Ensure the file is named exactly `privacy-policy.html`
- Check that it's in the root directory (not in a subfolder)
- Verify GitHub Pages is enabled in Settings

### Need Private Repository?
- GitHub Pages on private repos requires GitHub Pro
- Alternative: Use a separate public repo just for the privacy policy
- Or deploy to Netlify, Vercel, or Cloudflare Pages (free for static sites)

## Files Ready for GitHub Pages

✅ `privacy-policy.html` - Main privacy policy page
✅ Repository is public
✅ File is in root directory
✅ All changes pushed to main branch

## Next Steps After Enabling

1. Test the URL in your browser
2. Add the URL to LinkedIn Developer Portal
3. Update your application's settings to reference the privacy policy
4. Consider adding a link in your README

---

**Note**: If your repository is private, you'll need GitHub Pro for GitHub Pages, or you can:
- Make the repository public
- Create a separate public repo for just the privacy policy
- Use a free static hosting service (Netlify, Vercel, Cloudflare Pages)
