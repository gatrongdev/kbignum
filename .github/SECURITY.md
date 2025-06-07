# Security Policy

## 🔒 Supported Versions

We actively maintain and provide security updates for the following versions of KBigNum:

| Version | Supported          |
| ------- | ------------------ |
| 0.x.x   | ✅ Full support    |

## 🚨 Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue, please follow these steps:

### 1. **DO NOT** create a public GitHub issue

Security vulnerabilities should not be reported through public GitHub issues.

### 2. Report privately

Please report security vulnerabilities through one of these channels:

- **GitHub Security Advisories**: [Report a vulnerability](https://github.com/gatrongdev/kbignum/security/advisories/new)
- **Email**: Send details to the maintainers (create a private issue for contact)

### 3. Include detailed information

When reporting a vulnerability, please include:

- Description of the vulnerability
- Steps to reproduce the issue
- Potential impact
- Suggested fix (if you have one)
- Your contact information

### 4. Response timeline

- **Acknowledgment**: Within 48 hours
- **Initial assessment**: Within 1 week
- **Status updates**: Weekly until resolved
- **Resolution**: Depends on severity and complexity

## 🛡️ Security Measures

KBigNum implements several security measures:

### Code Quality
- **Static Analysis**: GitHub's default CodeQL setup for vulnerability detection
- **Code Quality**: Detekt static analysis for Kotlin-specific issues  
- **Dependency Scanning**: Dependabot for automated dependency vulnerability checks
- **Code Review**: All changes require review before merging

### Build Security
- **Signed Releases**: All releases are cryptographically signed
- **Reproducible Builds**: Build process is transparent and reproducible
- **Secure CI/CD**: GitHub Actions with security best practices

### Dependency Management
- **Regular Updates**: Dependencies are regularly updated
- **Vulnerability Monitoring**: Automated scanning for known vulnerabilities
- **Minimal Dependencies**: We keep dependencies to a minimum

## 🔍 Security Scanning

Our automated security scanning includes:

- **CodeQL**: GitHub's default CodeQL setup for security vulnerability detection
- **Dependency Scanning**: Dependabot for automated dependency updates and vulnerability alerts
- **Supply Chain Security**: GitHub's dependency graph, security advisories, and secret scanning

## 📚 Security Best Practices

When using KBigNum in your projects:

### 1. Keep Updated
Always use the latest version to ensure you have security fixes.

```kotlin
dependencies {
    implementation("io.github.gatrongdev:kbignum:LATEST_VERSION")
}
```

### 2. Validate Input
When working with arbitrary precision numbers from external sources:

```kotlin
// Good: Validate input
try {
    val number = userInput.toKBigDecimal()
    // Process number
} catch (e: NumberFormatException) {
    // Handle invalid input
}

// Bad: No validation
val number = userInput.toKBigDecimal() // May throw exception
```

### 3. Handle Large Numbers Carefully
Be aware of memory implications when working with very large numbers:

```kotlin
// Consider memory limits for very large calculations
val result = if (number.precision() > MAX_SAFE_PRECISION) {
    // Handle large numbers carefully
    handleLargeNumber(number)
} else {
    number.multiply(other)
}
```

## 🎯 Scope

This security policy applies to:

- KBigNum library code
- Build and release infrastructure
- Documentation and examples

It does **not** cover:

- Third-party dependencies (report to their maintainers)
- Applications using KBigNum (responsibility of the application developer)
- Development tools and environments

## 📝 Security Advisories

Security advisories will be published at:

- [GitHub Security Advisories](https://github.com/gatrongdev/kbignum/security/advisories)
- Release notes for patched versions
- This security policy (updated as needed)

## 🙏 Recognition

We appreciate responsible disclosure of security vulnerabilities. Contributors who report valid security issues will be:

- Credited in the security advisory (unless they prefer to remain anonymous)
- Mentioned in release notes
- Added to our security contributors list

## 📞 Contact

For security-related questions or concerns:

- Create a private security advisory on GitHub
- Follow responsible disclosure practices
- Allow reasonable time for fixes before public disclosure

---

**Last updated**: January 2025

Thank you for helping keep KBigNum secure! 🔒