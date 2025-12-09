# Privacy Policy for LinkedIn Comment Responder

**Last Updated**: December 9, 2025

## Introduction

This Privacy Policy describes how LinkedIn Comment Responder ("I", "my", or "the Application") collects, uses, and protects your information when you use this personal developer application. This is a personal project created by an individual developer for automating LinkedIn comment responses.

## Information We Collect

### 1. LinkedIn Account Information

When you authorize our application through LinkedIn OAuth, we collect:

- **Profile Information**: Your LinkedIn profile ID, name, and email address
- **Post Data**: Content of your LinkedIn posts that you choose to monitor
- **Comment Data**: Comments made on your monitored posts, including commenter names and comment text
- **Interaction Data**: Records of responses generated and posted by our application

### 2. API Credentials

We require you to provide:

- LinkedIn OAuth Client ID and Client Secret
- LinkedIn Access Token and Refresh Token
- OpenAI API Key (or other LLM provider credentials)

### 3. Application Usage Data

We collect:

- Configuration settings (polling intervals, tone preferences, etc.)
- Interaction history (comments processed, responses generated)
- Error logs and system diagnostics
- API usage metrics

## How We Use Your Information

### Primary Uses

1. **Comment Monitoring**: To fetch and monitor comments on your LinkedIn posts
2. **Response Generation**: To generate contextually appropriate responses using AI
3. **Response Posting**: To post approved responses back to LinkedIn on your behalf
4. **History Tracking**: To maintain a record of all interactions for your review

### Secondary Uses

1. **System Improvement**: To improve response quality and system performance
2. **Error Handling**: To diagnose and fix technical issues
3. **Security**: To detect and prevent unauthorized access or abuse

## Data Storage and Security

### Storage Location

- **Local Storage Only**: All data is stored locally on your own server or computer
- **File-Based Storage**: Interaction history stored in JSON files in the configured storage directory
- **No Cloud Storage**: I do not have access to your data. Everything stays on your machine.
- **No Central Database**: There is no central database or server collecting data from users

### Security Measures

1. **Credential Protection**: API credentials stored as environment variables on your machine, never in code
2. **Access Control**: Only you have access to your local data storage
3. **Encryption**: You are responsible for encrypting credentials at rest on your system
4. **Secure Communication**: All API communications use HTTPS/TLS
5. **No Data Collection**: I (the developer) do not collect, access, or store any of your data

### Data Retention

- **Interaction History**: Retained on your local machine indefinitely unless you manually delete it
- **Archived Data**: Old interactions archived locally when storage capacity is reached
- **Logs**: Application logs retained on your machine according to your configuration (default: 30 days)
- **Developer Access**: I have no access to your data, logs, or any information processed by the application

## Data Sharing and Third Parties

### Third-Party Services

The application interacts with the following third-party services on your behalf:

1. **LinkedIn API**
   - Purpose: Fetch comments and post responses
   - Data Shared: Your access token, post IDs, comment IDs, response text
   - Privacy Policy: [LinkedIn Privacy Policy](https://www.linkedin.com/legal/privacy-policy)
   - Note: Data is sent directly from your machine to LinkedIn

2. **OpenAI API** (or other LLM provider)
   - Purpose: Generate response text
   - Data Shared: Post content, comment text, commenter names
   - Privacy Policy: [OpenAI Privacy Policy](https://openai.com/policies/privacy-policy)
   - Note: Data is sent directly from your machine to OpenAI

### Developer Access

- **No Data Collection**: I (the developer) do not collect, access, or receive any of your data
- **No Analytics**: The application does not send usage analytics or telemetry
- **No Tracking**: There is no tracking, monitoring, or data collection by the developer
- **Open Source**: You can review the source code to verify these claims

### No Data Selling

I do not sell, rent, or trade your personal information to anyone. I don't have access to your data in the first place.

### No Marketing

I do not use your information for marketing purposes. This is a personal developer project.

## Your Rights and Choices

### Access and Control

You have the right to:

1. **Access Your Data**: View all stored interaction history via the API or storage files
2. **Export Your Data**: Export interaction history in JSON or CSV format
3. **Delete Your Data**: Delete interaction history or specific records at any time
4. **Revoke Access**: Revoke the application's LinkedIn API access at any time

### How to Exercise Your Rights

1. **View History**: `curl http://localhost:8080/api/management/history`
2. **Export History**: `curl -O http://localhost:8080/api/management/history/export?format=json`
3. **Delete Data**: Manually delete files in your storage directory
4. **Revoke Access**: Go to [LinkedIn Apps](https://www.linkedin.com/mypreferences/d/apps) and remove the application

## LinkedIn API Compliance

### OAuth Permissions

Our application requests the following LinkedIn API scopes:

- `r_liteprofile`: Read your basic profile information
- `r_emailaddress`: Read your email address
- `w_member_social`: Post comments on your behalf
- `rw_organization_admin`: Manage organization pages (if applicable)

### Data Usage Compliance

We comply with LinkedIn's API Terms of Use:

- We only access data you explicitly authorize
- We do not store LinkedIn data longer than necessary
- We do not use LinkedIn data for purposes other than stated
- We respect LinkedIn's rate limits and usage policies

## OpenAI API Compliance

### Data Processing

When using OpenAI's API:

- Post content and comments are sent to OpenAI for response generation
- OpenAI may use this data according to their [API Data Usage Policy](https://openai.com/policies/api-data-usage-policies)
- As of March 1, 2023, OpenAI does not use API data to train their models
- Data sent to OpenAI is retained for 30 days for abuse monitoring, then deleted

### Alternative LLM Providers

You can configure alternative LLM providers (Anthropic, AWS Bedrock, etc.) with their own privacy policies.

## Children's Privacy

Our application is not intended for use by children under 13 years of age. We do not knowingly collect information from children under 13.

## International Data Transfers

- **LinkedIn**: Data may be transferred to LinkedIn's servers globally
- **OpenAI**: Data may be transferred to OpenAI's servers (primarily US-based)
- **Your Server**: Your data is stored on your own server/computer

## Data Breach Notification

Since all data is stored locally on your machine and I (the developer) have no access to it:

1. You are responsible for securing your own data
2. Any data breach would be on your local system, not a central server
3. The application can notify you via configured channels (email/Slack) if it detects issues
4. I (the developer) will not be aware of any data breaches since I don't have access to your data

## Changes to This Privacy Policy

I may update this Privacy Policy from time to time. Changes will be:

- Posted in this document with an updated "Last Updated" date
- Announced in the project's release notes on GitHub
- Highlighted in the application documentation

You are encouraged to review this policy periodically for any changes.

## Your Responsibilities

As the application operator, you are responsible for:

1. **Securing Credentials**: Protecting your API keys and access tokens
2. **Data Protection**: Implementing appropriate security measures for your server
3. **Compliance**: Ensuring your use complies with applicable laws and regulations
4. **User Consent**: Obtaining consent from commenters if required by law

## Legal Basis for Processing (GDPR)

If you are in the European Economic Area (EEA), our legal basis for processing your information is:

1. **Consent**: You have given explicit consent for the application to process your data
2. **Legitimate Interest**: Processing is necessary for the legitimate interest of automating comment responses
3. **Contract**: Processing is necessary to provide the service you requested

## Contact Information

For privacy-related questions or concerns:

- **Email**: [vsarankumar2003@gmail.com]
- **GitHub Issues**: [repository-url]/issues
- **Documentation**: See [README.md](README.md) for more information

## Compliance and Certifications

### Standards

This application follows industry best practices:

- OWASP security guidelines
- OAuth 2.0 security best practices
- API security standards

### Open Source

This is an open-source personal project. You can:

- Review the source code for security and privacy practices
- Conduct your own security audits
- Verify that no data is sent to the developer
- Report security issues via GitHub
- Fork and modify the code as needed

## Disclaimer

This application is provided "as is" without warranties. This is a personal developer project, not a commercial service. You use it at your own risk. I am not responsible for:

- Data loss or corruption on your local system
- Unauthorized access to your data
- Misuse of the application
- Violations of LinkedIn's or OpenAI's terms of service
- Any damages or issues arising from use of this application

You are responsible for:
- Securing your own credentials and data
- Complying with LinkedIn's and OpenAI's terms of service
- Backing up your data
- Ensuring appropriate use of the application

## Acknowledgment

By using LinkedIn Comment Responder, you acknowledge that you have read and understood this Privacy Policy and agree to its terms.

---

## Quick Reference

### What We Collect
- LinkedIn profile info, posts, and comments
- API credentials (stored locally)
- Interaction history

### What I (the Developer) Don't Do
- ❌ Collect or access your data
- ❌ Store data in the cloud or on any server
- ❌ Sell your data
- ❌ Use data for marketing
- ❌ Track or monitor your usage
- ❌ Send analytics or telemetry

### Your Control
- ✅ All data stored locally
- ✅ Export anytime
- ✅ Delete anytime
- ✅ Revoke access anytime

### Third Parties
- LinkedIn API (for posting)
- OpenAI API (for AI responses)
- No other third parties

---

**For more information, see our [README.md](README.md) or contact us at [vsarankumar2003@gmail.com]**
