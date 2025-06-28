# 📧 Gmail Email Configuration Guide

## Current Status
❌ **Your email is NOT configured yet!**

The `application.properties` file still contains placeholder values:
```
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

## ✅ Step-by-Step Setup

### 1. Enable 2-Factor Authentication (2FA) on Gmail
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Under "Signing in to Google", click "2-Step Verification"
3. Follow the setup process if not already enabled

### 2. Generate Gmail App Password
1. Go to [Google Account Security](https://myaccount.google.com/security)
2. Search for "App passwords" or go to [App Passwords](https://myaccount.google.com/apppasswords)
3. Select "Mail" as the app
4. Select "Other" for device and name it "Tourism App"
5. Copy the 16-character password (e.g., `abcd efgh ijkl mnop`)

### 3. Update application.properties
Replace the placeholder values in `src/main/resources/application.properties`:

```properties
# Replace with your actual Gmail address
spring.mail.username=your-actual-email@gmail.com

# Replace with the 16-character App Password (no spaces)
spring.mail.password=abcdefghijklmnop
```

### 4. Test Your Configuration

#### Option A: Use the test script
```bash
setup-gmail-email.bat
```

#### Option B: Manual API testing
```bash
# Check configuration
curl http://localhost:8080/api/auth/test-email-config

# Send test email
curl -X POST "http://localhost:8080/api/auth/test-send-email?email=your-email@gmail.com"
```

#### Option C: Register a test account
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"your-email@gmail.com","password":"password123","role":"GUEST"}'
```

## 🔍 Debugging - Where to Find Information

### In Application Logs, Look For:
1. **Email Configuration Check:**
   ```
   === ATTEMPTING TO SEND CONFIRMATION EMAIL ===
   To: your-email@gmail.com
   From: your-configured-email@gmail.com
   Confirmation URL: http://localhost:8080/api/auth/confirm?token=...
   ```

2. **Success Message:**
   ```
   ✅ CONFIRMATION EMAIL SENT SUCCESSFULLY to: your-email@gmail.com
   ```

3. **Failure Message:**
   ```
   ❌ FAILED TO SEND CONFIRMATION EMAIL to: your-email@gmail.com
   🔗 MANUAL CONFIRMATION URL (use this if email fails): http://localhost:8080/api/auth/confirm?token=...
   ```

### If Email Fails, Use Manual Confirmation:
1. Copy the token from the logs
2. Visit: `http://localhost:8080/api/auth/confirm?token=YOUR_TOKEN_HERE`

## 🚨 Common Issues

### "Email configuration not properly set"
- **Cause:** Still using placeholder values
- **Solution:** Update `application.properties` with real credentials

### "Authentication failed" or "535 Authentication failed"
- **Cause:** Wrong password or 2FA not enabled
- **Solution:** Use App Password, not regular password

### "Connection timeout"
- **Cause:** Network/firewall issues
- **Solution:** Check internet connection and firewall settings

### Email sent successfully but not received
- **Cause:** Email in spam folder or delayed delivery
- **Solution:** 
  - Check spam/junk folder
  - Wait 5-10 minutes
  - Check Gmail "Sent" folder to confirm it was sent

## 🧪 Quick Test Commands

After updating your configuration, test with these commands:

```bash
# 1. Check configuration
curl http://localhost:8080/api/auth/test-email-config

# 2. Send test email to yourself
curl -X POST "http://localhost:8080/api/auth/test-send-email?email=YOUR_EMAIL@gmail.com"

# 3. Register and trigger confirmation email
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"YOUR_EMAIL@gmail.com","password":"password123","role":"GUEST"}'
```

## ✅ Success Indicators

You'll know it's working when:
1. ✅ Configuration test shows "Ready" status
2. ✅ Logs show "CONFIRMATION EMAIL SENT SUCCESSFULLY"
3. ✅ You receive the confirmation email in your inbox
4. ✅ Clicking the email link confirms your account

---

**Need help?** Check the application logs first - they contain detailed error information and manual confirmation URLs if email delivery fails.
