#!/bin/bash

# AppNow Build Logic - Version Bump Script
# This script updates the version in build-config.properties and creates a git tag

set -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 <new-version>"
    echo "Example: $0 0.2.6"
    exit 1
fi

NEW_VERSION=$1
CONFIG_FILE="build-config.properties"

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: $CONFIG_FILE not found"
    exit 1
fi

# Get current version
CURRENT_VERSION=$(grep "^VERSION_NAME=" "$CONFIG_FILE" | cut -d'=' -f2)
echo "Current version: $CURRENT_VERSION"
echo "New version: $NEW_VERSION"

# Update version in build-config.properties
sed -i.bak "s/^VERSION_NAME=.*/VERSION_NAME=$NEW_VERSION/" "$CONFIG_FILE"
sed -i.bak "s/^CATALOG_VERSION=.*/CATALOG_VERSION=$NEW_VERSION/" "$CONFIG_FILE"
sed -i.bak "s/^FALLBACK_VERSION_NAME=.*/FALLBACK_VERSION_NAME=$NEW_VERSION/" "$CONFIG_FILE"
sed -i.bak "s/^FALLBACK_CATALOG_VERSION=.*/FALLBACK_CATALOG_VERSION=$NEW_VERSION/" "$CONFIG_FILE"

# Remove backup file
rm "$CONFIG_FILE.bak"

echo "✅ Updated $CONFIG_FILE with version $NEW_VERSION"

# Show what changed
echo ""
echo "Changes made:"
git diff "$CONFIG_FILE"

echo ""
echo "Next steps:"
echo "1. Review the changes above"
echo "2. Commit: git add $CONFIG_FILE && git commit -m \"Bump version to $NEW_VERSION\""
echo "3. Tag: git tag v$NEW_VERSION"
echo "4. Push: git push origin main && git push origin v$NEW_VERSION"
echo "5. CI will automatically publish the new version to GitHub Packages"
