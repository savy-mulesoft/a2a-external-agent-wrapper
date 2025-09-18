#!/bin/bash

# Script to push to both Git repositories
# Usage: ./push-to-both.sh [commit-message]

echo "🚀 Pushing to both Git repositories..."

# Check if we're in a git repository
if [ ! -d ".git" ]; then
    echo "❌ Error: Not in a git repository"
    exit 1
fi

# Check if there are changes to commit
if [ -n "$(git status --porcelain)" ]; then
    echo "📝 Changes detected. Committing..."
    if [ -n "$1" ]; then
        git add .
        git commit -m "$1"
    else
        echo "❌ Error: Please provide a commit message"
        echo "Usage: ./push-to-both.sh 'Your commit message'"
        exit 1
    fi
else
    echo "✅ No changes to commit"
fi

# Push to origin (main account)
echo "📤 Pushing to origin (main account)..."
if git push origin main; then
    echo "✅ Successfully pushed to origin"
else
    echo "❌ Failed to push to origin"
fi

# Push to savy (second account)
echo "📤 Pushing to savy (second account)..."
if git push savy main; then
    echo "✅ Successfully pushed to savy"
else
    echo "❌ Failed to push to savy"
fi

echo "🎉 Push process completed!"
