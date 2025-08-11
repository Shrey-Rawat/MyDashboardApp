#!/bin/bash

echo "🔄 Starting directory migration from bestproductivityapp to mydashboardapp..."

# Find all source directories (not build directories) with the old package name
find . -type d -path "*/com/bestproductivityapp" \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" | while read old_dir; do
  
  # Create the new directory path
  new_dir=$(echo "$old_dir" | sed 's/bestproductivityapp/mydashboardapp/g')
  
  echo "📁 Moving: $old_dir -> $new_dir"
  
  # Create parent directory structure
  mkdir -p "$(dirname "$new_dir")"
  
  # Move the directory
  mv "$old_dir" "$new_dir"
done

echo "✅ Directory migration completed!"

# Clean up any empty bestproductivityapp directories
find . -type d -name "bestproductivityapp" -empty \
  -not -path "./build/*" \
  -not -path "./.gradle/*" \
  -not -path "./.kotlin/*" \
  -not -path "./*/build/*" \
  -exec rmdir {} \; 2>/dev/null || true

echo "🧹 Cleaned up empty directories"
echo "🎉 Migration complete!"
