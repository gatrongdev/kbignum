# Contributing to KBigNum 🚀

Thank you for your interest in contributing to KBigNum! This guide will help you get started with contributing to our Kotlin Multiplatform arbitrary precision mathematics library.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Release Process](#release-process)
- [Style Guidelines](#style-guidelines)

## 🤝 Code of Conduct

This project adheres to a code of conduct. By participating, you are expected to uphold this standard. Please be respectful, inclusive, and constructive in all interactions.

## 🚀 Getting Started

### Prerequisites

- **JDK 17+** (recommend JDK 17 or 21)
- **Android SDK** (latest stable)
- **Git**
- **IDE**: IntelliJ IDEA or Android Studio (recommended)

### Types of Contributions

We welcome contributions in the form of:

- 🐛 **Bug fixes**
- ✨ **New features**
- 📚 **Documentation improvements**
- 🧪 **Test coverage improvements**
- 🔧 **Performance optimizations**
- 🎨 **Code quality improvements**

## 🛠️ Development Setup

1. **Fork and clone the repository**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/kbignum.git
   cd kbignum
   ```

2. **Set up the development environment**:
   ```bash
   # Make scripts executable
   chmod +x scripts/*.sh
   
   # Run initial build
   ./gradlew build
   ```

3. **Verify your setup**:
   ```bash
   # Run tests
   ./gradlew test
   
   # Run quality checks
   ./gradlew runAllChecks
   
   # Generate documentation
   ./gradlew dokkaHtml
   ```

4. **Import into your IDE**:
   - Open the project in IntelliJ IDEA or Android Studio
   - Import as a Gradle project
   - Wait for indexing to complete

## 🔄 Making Changes

### Branching Strategy

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. **Branch naming conventions**:
   - `feature/description` - New features
   - `fix/description` - Bug fixes
   - `docs/description` - Documentation updates
   - `refactor/description` - Code refactoring
   - `test/description` - Test improvements

### Commit Messages

We use [Conventional Commits](https://www.conventionalcommits.org/) for automated changelog generation:

```
type(scope): description

[optional body]

[optional footer]
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Test changes
- `chore`: Build process or auxiliary tool changes

**Examples**:
```bash
feat(math): add square root function for KBigDecimal
fix(android): resolve precision loss in division operation
docs(readme): update installation instructions
test(integer): add edge case tests for multiplication
```

### Code Organization

```
shared/src/
├── commonMain/kotlin/          # Platform-agnostic code
├── commonTest/kotlin/          # Common tests
├── androidMain/kotlin/         # Android-specific implementations
├── androidUnitTest/kotlin/     # Android tests
├── iosMain/kotlin/            # iOS-specific implementations
└── iosTest/kotlin/            # iOS tests
```

## 🧪 Testing

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific platform tests
./gradlew testDebugUnitTest           # Android
./gradlew iosSimulatorArm64Test       # iOS Simulator

# Run with coverage
./gradlew test koverXmlReport
```

### Test Guidelines

1. **Write comprehensive tests** for new features
2. **Test edge cases** and error conditions
3. **Use descriptive test names**:
   ```kotlin
   @Test
   fun `should return zero when adding positive and negative numbers of same magnitude`() {
       // Test implementation
   }
   ```

4. **Follow the AAA pattern**:
   ```kotlin
   @Test
   fun testExample() {
       // Arrange
       val input = "123.456".toKBigDecimal()
       
       // Act
       val result = input.multiply(KBigDecimalFactory.create("2"))
       
       // Assert
       assertEquals("246.912", result.toString())
   }
   ```

### Test Coverage

- Aim for **>90% code coverage**
- Focus on **critical paths** and **edge cases**
- Test **both positive and negative scenarios**

## 📝 Submitting Changes

### Pull Request Process

1. **Update your branch**:
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run quality checks**:
   ```bash
   ./gradlew runAllChecks
   ```

3. **Push your changes**:
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create a Pull Request**:
   - Use the provided PR template
   - Include a clear description
   - Reference related issues
   - Add screenshots if applicable

### PR Requirements

- ✅ All tests pass
- ✅ Code quality checks pass
- ✅ Documentation updated (if needed)
- ✅ No merge conflicts
- ✅ Follows coding standards

### Review Process

1. **Automated checks** run on your PR
2. **Code review** by maintainers
3. **Feedback and iteration** if needed
4. **Merge** once approved

## 🚀 Release Process

### Version Management

We use [Semantic Versioning](https://semver.org/):

- **MAJOR** (X.y.z): Breaking changes
- **MINOR** (x.Y.z): New features (backward compatible)
- **PATCH** (x.y.Z): Bug fixes

### Creating a Release

1. **Update version**:
   ```bash
   ./scripts/update-version.sh 1.0.1
   ```

2. **Review and commit**:
   ```bash
   git add .
   git commit -m "bump: version 1.0.1"
   ```

3. **Push to main**:
   ```bash
   git push origin main
   ```

4. **Automated release** will be triggered by GitHub Actions

### Release Workflow

The automated release process:

1. **Detects version changes** in `build.gradle.kts`
2. **Runs full test suite** and quality checks
3. **Publishes to Maven Central**
4. **Generates changelog** from commits
5. **Creates GitHub release** with artifacts

## 🎨 Style Guidelines

### Code Style

We use **ktlint** for code formatting:

```bash
# Check code style
./gradlew ktlintCheck

# Auto-fix formatting issues
./gradlew ktlintFormat
```

### Kotlin Guidelines

1. **Use descriptive names**:
   ```kotlin
   // Good
   fun calculateSquareRoot(value: KBigDecimal, scale: Int): KBigDecimal
   
   // Bad
   fun calc(v: KBigDecimal, s: Int): KBigDecimal
   ```

2. **Prefer immutability**:
   ```kotlin
   // Good
   val result = value.add(other)
   
   // Avoid mutable state when possible
   ```

3. **Use extension functions** for natural APIs:
   ```kotlin
   fun String.toKBigDecimal(): KBigDecimal = KBigDecimalFactory.create(this)
   ```

4. **Document public APIs**:
   ```kotlin
   /**
    * Calculates the square root of this decimal number.
    * 
    * @param scale the number of decimal places in the result
    * @return the square root with the specified scale
    * @throws ArithmeticException if this number is negative
    */
   fun sqrt(scale: Int): KBigDecimal
   ```

### Documentation

- **Use KDoc** for public APIs
- **Include examples** in documentation
- **Keep README.md** up to date
- **Write clear commit messages**

## 🐛 Reporting Issues

### Bug Reports

Include:
- **Clear description** of the issue
- **Steps to reproduce**
- **Expected vs actual behavior**
- **Environment details** (OS, Kotlin version, etc.)
- **Sample code** if applicable

### Feature Requests

Include:
- **Use case description**
- **Proposed API** or behavior
- **Justification** for the feature
- **Implementation ideas** (optional)

## 📚 Resources

- **Documentation**: [GitHub Pages](https://gatrongdev.github.io/kbignum/)
- **API Reference**: Generated by Dokka
- **Examples**: See `README.md` and test files
- **Issues**: [GitHub Issues](https://github.com/gatrongdev/kbignum/issues)

## 🙏 Recognition

Contributors are recognized in:
- **Release notes**
- **Contributors list**
- **Git history**

Thank you for contributing to KBigNum! 🎉

---

**Questions?** Feel free to ask in issues or discussions. We're here to help!

**Last updated**: January 2025