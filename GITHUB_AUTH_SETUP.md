# GitHub Authentication Setup

You need to authenticate with GitHub before you can push code. Here are your options:

## Option 1: GitHub CLI (Recommended - Easiest)

### Install GitHub CLI
```bash
# macOS with Homebrew
brew install gh

# Or download from: https://cli.github.com/
```

### Login with GitHub CLI
```bash
# Login to GitHub
gh auth login

# Follow the prompts:
# - What account: GitHub.com
# - Protocol: HTTPS
# - Authenticate: Login with a web browser
# - Copy the one-time code and press Enter
# - Browser will open, paste code and authorize
```

### Verify Login
```bash
gh auth status
```

Once logged in with `gh`, Git will automatically use your credentials!

---

## Option 2: Personal Access Token (Classic Method)

### Step 1: Generate Token
1. Go to: https://github.com/settings/tokens
2. Click **"Generate new token"** → **"Generate new token (classic)"**
3. Settings:
   - **Note**: "LinkedIn Comment Responder"
   - **Expiration**: 90 days (or your preference)
   - **Scopes**: Check ✅ **repo** (full control of private repositories)
4. Click **"Generate token"**
5. **COPY THE TOKEN** (you won't see it again!)

### Step 2: Save Token Securely
```bash
# Store in macOS Keychain (recommended)
# When you push, use:
# Username: sarangen2
# Password: <paste your token>

# Git will remember it in Keychain
```

### Step 3: Configure Git to Use Keychain
```bash
git config --global credential.helper osxkeychain
```

---

## Option 3: SSH Keys (Most Secure)

### Step 1: Check for Existing SSH Keys
```bash
ls -la ~/.ssh
# Look for: id_rsa.pub or id_ed25519.pub
```

### Step 2: Generate New SSH Key (if needed)
```bash
ssh-keygen -t ed25519 -C "vsarankumar2003@gmail.com"
# Press Enter to accept default location
# Enter a passphrase (optional but recommended)
```

### Step 3: Add SSH Key to ssh-agent
```bash
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_ed25519
```

### Step 4: Copy Public Key
```bash
cat ~/.ssh/id_ed25519.pub
# Copy the entire output
```

### Step 5: Add to GitHub
1. Go to: https://github.com/settings/keys
2. Click **"New SSH key"**
3. Title: "MacBook - LinkedIn Project"
4. Key: Paste your public key
5. Click **"Add SSH key"**

### Step 6: Test Connection
```bash
ssh -T git@github.com
# Should see: "Hi sarangen2! You've successfully authenticated..."
```

### Step 7: Use SSH Remote URL
```bash
git remote remove origin
git remote add origin git@github.com:sarangen2/linkedin-comment-responder.git
```

---

## Quick Start: Which Method Should I Use?

### For Beginners: **GitHub CLI** (Option 1)
- Easiest to set up
- Handles everything automatically
- Just run: `brew install gh` then `gh auth login`

### For Quick Setup: **Personal Access Token** (Option 2)
- Works immediately
- No additional software needed
- Good for temporary access

### For Long-term: **SSH Keys** (Option 3)
- Most secure
- No password needed after setup
- Best for daily use

---

## My Recommendation

**Use GitHub CLI** - it's the easiest:

```bash
# Install
brew install gh

# Login
gh auth login

# That's it! Now you can push
```

---

## After Authentication

Once you're authenticated with any method above, run:

```bash
./push-to-github.sh
```

This will configure your remote and show you the commands to push.

---

## Troubleshooting

### "brew: command not found"
Install Homebrew first:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

### "Permission denied (publickey)"
- Your SSH key isn't set up correctly
- Use Personal Access Token method instead

### "Authentication failed"
- Token expired or incorrect
- Generate a new token
- Make sure you copied the entire token

---

## Need Help?

1. **Easiest**: Install GitHub CLI
   ```bash
   brew install gh
   gh auth login
   ```

2. **Quick**: Use Personal Access Token
   - Generate at: https://github.com/settings/tokens
   - Use as password when pushing

3. **Secure**: Set up SSH keys
   - Follow Option 3 above

---

**Ready?** Choose a method above and authenticate, then run `./push-to-github.sh`
