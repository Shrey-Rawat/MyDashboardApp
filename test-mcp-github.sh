#!/bin/bash

# MCP GitHub Server Test Script
# This script tests various GitHub operations that would normally be done via MCP

echo "🧪 Testing MCP GitHub Server Operations"
echo "======================================="

echo ""
echo "📁 Repository Information:"
echo "--------------------------"
gh repo view --json name,description,owner,pushedAt,stargazerCount,forkCount,isPrivate,defaultBranchRef

echo ""
echo "🏃 Recent Workflow Runs:"
echo "-----------------------"
gh run list --limit 3

echo ""
echo "📋 Pull Requests:"
echo "----------------"
gh pr list --limit 3

echo ""
echo "🐛 Issues:"
echo "---------"
gh issue list --limit 3

echo ""
echo "🏷️  Releases:"
echo "------------"
gh release list --limit 3

echo ""
echo "🌿 Branches:"
echo "-----------"
gh api repos/{owner}/{repo}/branches --jq '.[].name' | head -5

echo ""
echo "📊 Repository Statistics:"
echo "-----------------------"
echo "Languages:"
gh api repos/{owner}/{repo}/languages --jq 'to_entries | map("\(.key): \(.value)")[]' | head -5

echo ""
echo "🔄 Workflow Status Summary:"
echo "--------------------------"
for workflow in "Build" "CI" "Test" "Lint"; do
    status=$(gh run list --workflow="$workflow" --limit 1 --json conclusion --jq '.[0].conclusion // "running"')
    echo "$workflow: $status"
done

echo ""
echo "✅ MCP GitHub Server Test Complete!"
echo ""
echo "This script demonstrates the types of operations that the MCP GitHub server"
echo "would perform programmatically through its API interface."
