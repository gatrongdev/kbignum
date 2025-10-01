# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.17] - 2025-01-XX

### Added
- **Division Configuration System**: New helper objects to make division operations more intuitive and type-safe
  - `PrecisionScale` object with constants for common decimal place requirements:
    - `CURRENCY` (2 decimals) - for money calculations
    - `EXCHANGE_RATE` (4 decimals) - for forex/currency conversion
    - `PERCENTAGE` (2 decimals) - for percentage calculations
    - `SCIENTIFIC` (10 decimals) - for scientific computations
    - `HIGH_PRECISION` (20 decimals) - for very precise calculations
    - `INTEREST_RATE` (6 decimals) - for interest rate calculations
    - `CRYPTOCURRENCY` (8 decimals) - for Bitcoin and crypto calculations
  - `DivisionConfig` data class that combines scale and rounding mode
  - `DivisionStrategy` object with predefined division strategies:
    - `CURRENCY` - 2 decimals with HALF_UP rounding
    - `FINANCIAL` - 2 decimals with HALF_EVEN (banker's) rounding
    - `EXCHANGE_RATE` - 4 decimals with HALF_UP rounding
    - `PERCENTAGE` - 2 decimals with HALF_UP rounding
    - `SCIENTIFIC` - 10 decimals with HALF_UP rounding
    - `HIGH_PRECISION` - 20 decimals with HALF_UP rounding
    - `INTEREST_RATE` - 6 decimals with HALF_UP rounding
    - `CRYPTOCURRENCY` - 8 decimals with HALF_UP rounding
    - `EXACT` - requires exact result or throws ArithmeticException
  - New `divide(other: KBigDecimal, config: DivisionConfig)` overload for cleaner division syntax

### Changed
- **Improved Division API**: Clients can now use predefined strategies instead of manually specifying scale and rounding mode

### Example Usage
```kotlin
// Before
val result = price.divide(quantity, 2, RoundingMode.HALF_UP)

// After - using strategy
val result = price.divide(quantity, DivisionStrategy.CURRENCY)

// After - using custom config
val result = price.divide(quantity, DivisionConfig(scale = 4, roundingMode = RoundingMode.HALF_EVEN))

// Using scale constants
val result = price.divide(quantity, PrecisionScale.CRYPTOCURRENCY, RoundingMode.HALF_UP)
```

## [0.0.16] - 2025-01-XX

### Fixed
- Enhanced KBigDecimal division for precision and special cases
- Added single-parameter divide function with automatic scale determination

### Changed
- Updated documentation to include new single-parameter divide function

## [0.0.1] - Initial Release

### Added
- `KBigDecimal` interface for arbitrary precision decimal arithmetic
- `KBigInteger` interface for arbitrary precision integer arithmetic
- `KBigMath` utility class for advanced mathematical operations
- Operator overloading support for natural mathematical syntax
- Extension functions for easy type conversion
- Factory methods for creating big numbers from various types
- Comprehensive test coverage
- Android and iOS platform support
- Support for basic arithmetic operations (add, subtract, multiply, divide)
- Support for advanced operations (sqrt, factorial, gcd, lcm, isPrime, pow)
- Rounding mode support for decimal operations
- Platform-optimized implementations using native libraries

[Unreleased]: https://github.com/gatrongdev/kbignum/compare/v0.0.17...HEAD
[0.0.17]: https://github.com/gatrongdev/kbignum/compare/v0.0.16...v0.0.17
[0.0.16]: https://github.com/gatrongdev/kbignum/releases/tag/v0.0.16
[0.0.1]: https://github.com/gatrongdev/kbignum/releases/tag/v0.0.1
