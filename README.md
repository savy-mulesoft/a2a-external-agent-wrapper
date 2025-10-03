# A2A External Wrapper

A MuleSoft application template for A2A (Application-to-Application) external integrations with automated deployment capabilities.

## 🚀 Quick Deployment

### Automated Local Deployment
```bash
# Deploy with automatic version increment
./auto-deploy-to-exchange.sh

# Deploy with specific version type
./auto-deploy-to-exchange.sh minor
./auto-deploy-to-exchange.sh major

# Deploy with custom version
./auto-deploy-to-exchange.sh 1.2.3
```

### GitHub Actions CI/CD
- **Push to main** → Automatic deployment with patch version increment
- **Create tag** → Deploy specific version (`git tag v1.2.3 && git push origin v1.2.3`)
- **Manual trigger** → Use GitHub Actions UI for controlled deployment

## 📋 Features

- ✅ **Automated Version Management** - Intelligent version increment and conflict detection
- ✅ **Exchange Integration** - Direct deployment to Anypoint Exchange
- ✅ **CI/CD Pipeline** - GitHub Actions workflow for automated deployments
- ✅ **Git Integration** - Automatic commits, tags, and releases
- ✅ **Error Handling** - Rollback on failure and clear error messages
- ✅ **Multiple Deployment Options** - Local script and cloud-based CI/CD

## 🔧 Setup

### Prerequisites
- Maven 3.6+
- Java 8+
- Git
- Anypoint Platform account with Exchange permissions

### Local Setup
1. Clone the repository
2. Ensure `settings.xml` contains valid Anypoint credentials
3. Make deployment script executable: `chmod +x auto-deploy-to-exchange.sh`

### GitHub Actions Setup
1. Add repository secrets:
   - `ANYPOINT_USERNAME` - Your Anypoint Platform username
   - `ANYPOINT_PASSWORD` - Your Anypoint Platform password
2. Push to main branch or use manual workflow trigger

## 📖 Documentation

- **[Automated Deployment Guide](AUTOMATED_DEPLOYMENT_GUIDE.md)** - Complete guide for automated deployments
- **[Exchange Deployment Guide](EXCHANGE_DEPLOYMENT_GUIDE.md)** - Manual deployment instructions
- **[Deployment Scripts](maven-publish-to-exchange.sh)** - Legacy deployment scripts

## 🎯 Current Version

**Version**: 1.0.4  
**Exchange URL**: https://anypoint.mulesoft.com/exchange/e5c02810-ef86-427e-8e6b-f3d3abe55974/a2a-external-wrapper/

## 🔗 Quick Links

- [Anypoint Exchange Asset](https://anypoint.mulesoft.com/exchange/e5c02810-ef86-427e-8e6b-f3d3abe55974/a2a-external-wrapper/)
- [GitHub Actions Workflows](.github/workflows/)
- [Deployment Logs](https://github.com/savy-mulesoft/a2a-external-agent-wrapper/actions)

## 🛠️ Development

### Local Development
```bash
# Run tests
mvn clean test

# Package application
mvn clean package

# Deploy to local Mule runtime
./run-with-mule-runtime.sh
```

### Testing the A2A Endpoint
Test the A2A wrapper using JSON-RPC protocol:

```bash
curl --location 'http://localhost:8081/external-azure-fins' \
--header 'Content-Type: application/json' \
--data '{
    "jsonrpc":"2.0","id":"1","method":"message/send",
    "params":{"message":{"role":"user","parts":[{"kind":"text","text":"How do I improve my credit score?"}]}}
}'
```

**Expected Response:**
```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "id": null,
    "sessionId": null,
    "status": {
      "state": "completed",
      "timestamp": "2025-10-03T14:20:33Z",
      "message": {
        "role": "agent",
        "parts": [
          {
            "kind": "text",
            "text": "Thanks for your message. This is a standard demo reply from the Finance Agent. For credit health: pay on time, keep utilization under 30%, avoid hard inquiries, and review reports for inaccuracies. (This mock returns the same text for any prompt.)\n"
          }
        ]
      }
    }
  }
}
```

### Version Management
The application uses semantic versioning (MAJOR.MINOR.PATCH):
- **PATCH**: Bug fixes and small updates
- **MINOR**: New features, backward compatible
- **MAJOR**: Breaking changes

## 📊 Deployment Status

| Environment | Status | Version | Last Updated |
|-------------|--------|---------|--------------|
| Exchange | ✅ Active | 1.0.4 | Latest |
| GitHub | ✅ Active | Latest | Auto-sync |

## 🆘 Troubleshooting

### Common Issues
1. **Version Conflict (412 Error)**: Use `./auto-deploy-to-exchange.sh minor` to increment version
2. **Authentication Failed**: Verify credentials in `settings.xml` or GitHub secrets
3. **Permission Denied**: Contact Anypoint Platform administrator

### Debug Commands
```bash
# Check current version
grep -o '<version>[^<]*</version>' pom.xml | head -1

# Test deployment with debug
mvn clean deploy -s settings.xml -DskipTests -X

# View deployment help
./auto-deploy-to-exchange.sh --help
```

## 📝 Contributing

1. Create feature branch from `main`
2. Make changes and test locally
3. Create pull request
4. After merge, deployment happens automatically

## 📄 License

This project is licensed under the terms specified in the MuleSoft license agreement.

---

**Need Help?** Check the [Automated Deployment Guide](AUTOMATED_DEPLOYMENT_GUIDE.md) for detailed instructions.
