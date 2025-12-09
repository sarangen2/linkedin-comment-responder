# Privacy Policy Quick Start

Get your privacy policy published for LinkedIn Developer Portal in 5 minutes.

## ⚡ Fastest Method: GitHub Pages

### Prerequisites
- GitHub account
- Git installed
- Your code in a Git repository

### Steps

1. **Update Contact Info** (30 seconds)
   ```bash
   # Replace placeholder email
   sed -i 's/your-email@example.com/YOUR_ACTUAL_EMAIL@example.com/g' privacy-policy.html
   ```

2. **Push to GitHub** (1 minute)
   ```bash
   git add privacy-policy.html PRIVACY_POLICY.md
   git commit -m "Add privacy policy"
   git push origin main
   ```

3. **Enable GitHub Pages** (2 minutes)
   - Go to your repository on GitHub
   - Click **Settings** → **Pages**
   - Source: **main** branch, **/ (root)** folder
   - Click **Save**
   - Wait 2-3 minutes for deployment

4. **Get Your URL** (30 seconds)
   ```
   https://YOUR_USERNAME.github.io/linkedin-comment-responder/privacy-policy.html
   ```

5. **Add to LinkedIn** (1 minute)
   - Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
   - Select your app → **Settings** tab
   - Add Privacy Policy URL (from step 4)
   - Click **Update**

## ✅ Done!

Your privacy policy is now live and compliant with LinkedIn requirements.

## 🔍 Verify

Test your URL:
```bash
curl -I https://YOUR_USERNAME.github.io/linkedin-comment-responder/privacy-policy.html
```

Should return: `HTTP/2 200`

## 📝 Need More Options?

See [PRIVACY_POLICY_SETUP.md](PRIVACY_POLICY_SETUP.md) for:
- Netlify hosting
- Vercel hosting
- Your own web server
- Serving from Spring Boot app

## 🆘 Troubleshooting

**URL not accessible?**
- Wait 5 minutes for GitHub Pages deployment
- Check GitHub Pages is enabled in Settings
- Verify file name is exactly `privacy-policy.html`

**LinkedIn rejects URL?**
- Ensure HTTPS (GitHub Pages provides this automatically)
- Test URL in browser first
- Check for typos in URL

## 📞 Need Help?

See [PRIVACY_POLICY_SETUP.md](PRIVACY_POLICY_SETUP.md) for detailed troubleshooting.

---

**Total Time: ~5 minutes** ⏱️
