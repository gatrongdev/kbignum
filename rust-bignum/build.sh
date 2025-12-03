#!/bin/bash

# Build script for Rust FFI library for iOS targets
set -e

echo "Building Rust library for iOS targets..."

# iOS Device (ARM64)
echo "Building for iOS Device (aarch64-apple-ios)..."
cargo build --release --target aarch64-apple-ios

# iOS Simulator (ARM64 - M1 Macs)
echo "Building for iOS Simulator ARM64 (aarch64-apple-ios-sim)..."
cargo build --release --target aarch64-apple-ios-sim

# iOS Simulator (x86_64 - Intel Macs)
echo "Building for iOS Simulator x86_64 (x86_64-apple-ios)..."
cargo build --release --target x86_64-apple-ios

# Create output directory
mkdir -p ../libs/ios

# Copy libraries
echo "Copying libraries..."
cp target/aarch64-apple-ios/release/libkbignum_ffi.a ../libs/ios/libkbignum_ffi_arm64.a
cp target/aarch64-apple-ios-sim/release/libkbignum_ffi.a ../libs/ios/libkbignum_ffi_sim_arm64.a
cp target/x86_64-apple-ios/release/libkbignum_ffi.a ../libs/ios/libkbignum_ffi_sim_x86_64.a

# Create universal simulator library
echo "Creating universal simulator library..."
lipo -create \
    target/aarch64-apple-ios-sim/release/libkbignum_ffi.a \
    target/x86_64-apple-ios/release/libkbignum_ffi.a \
    -output ../libs/ios/libkbignum_ffi_sim.a

echo "Build complete! Libraries are in libs/ios/"
echo "  - libkbignum_ffi_arm64.a (iOS Device)"
echo "  - libkbignum_ffi_sim.a (iOS Simulator - Universal)"
