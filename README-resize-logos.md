# Logo Resizer Script

This script resizes PNG logo images to 128x128 pixels while preserving transparency and aspect ratio.

## Quick Start

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Place your PNG logo files** in the same directory as the script

3. **Run the resize script:**
   ```bash
   npm run resize
   ```
   
   Or directly:
   ```bash
   node resize-logos.js
   ```

## What it does

- ✅ Finds all `.png` files in the current directory
- ✅ Resizes them to 128x128 pixels
- ✅ Preserves aspect ratio using `fit: "contain"`
- ✅ Maintains transparent background
- ✅ Saves resized versions with `_resized` suffix
- ✅ Shows file size reduction statistics

## Example

Input files:
- `logo1.png` (2048x2048, 500KB)
- `company-logo.png` (1024x768, 300KB)
- `brand.png` (512x512, 150KB)

Output files:
- `logo1_resized.png` (128x128, ~15KB)
- `company-logo_resized.png` (128x128, ~12KB)
- `brand_resized.png` (128x128, ~8KB)

## Requirements

- Node.js (version 14 or higher)
- Sharp library (automatically installed with `npm install`)

## Troubleshooting

If you get a "sharp not found" error, install it manually:
```bash
npm install sharp
```

## Features

- 🖼️ Preserves transparency
- 📐 Maintains aspect ratio
- 📊 Shows compression statistics
- 🔄 Batch processing
- 🛡️ Safe (doesn't overwrite originals)
