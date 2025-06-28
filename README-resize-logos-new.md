# Logo Resizer Scripts

This directory contains Node.js scripts to resize PNG logo images using the Sharp library.

## Available Scripts

### 1. `resize-logos.js` (Fixed Size)
Resizes all PNG images to a fixed 128x128 pixels with transparent background.

### 2. `resize-logos-improved.js` (Proportional Scaling)
**NEW:** Scales images down to 1% of their original dimensions (100x smaller) while preserving aspect ratio.

## Features

- ✅ **Preserves transparency** (PNG alpha channel)
- ✅ **Maintains aspect ratio** (proportional scaling)
- ✅ **Batch processing** (processes all PNG files in directory)
- ✅ **File size reporting** (shows before/after sizes)
- ✅ **Smart centering** (centers small images on 128x128 canvas)
- ✅ **Safe naming** (adds suffix to avoid overwriting originals)

## Installation

1. Install dependencies:
```bash
npm install
# or
npm run install-deps
```

## Usage

### Method 1: Scale to 1% (Recommended for huge logos)
```bash
# Scale images to 1% of original size (100x smaller)
npm run resize-small
# or
node resize-logos-improved.js
```

**Example:**
- Original: 4000x4000px → New: 40x40px
- Original: 2000x1500px → New: 20x15px

### Method 2: Fixed 128x128 size
```bash
# Resize to fixed 128x128 pixels
npm run resize
# or
node resize-logos.js
```

## Output Files

- **Method 1**: `logo.png` → `logo_small.png`
- **Method 2**: `logo.png` → `logo_resized.png`

## Technical Details

### Proportional Scaling Script Features:
- Calculates 1% of original dimensions
- Minimum size: 1x1 pixel (prevents 0-size images)
- For very small results (<64px): Centers on 128x128 transparent canvas
- Preserves original aspect ratio
- Maintains PNG transparency

### Image Processing Options:
- **Compression**: Level 6 (balanced quality/size)
- **Quality**: 90% (high quality)
- **Transparency**: Fully preserved
- **Background**: Transparent (RGBA: 0,0,0,0)

## Example Output

```
Found 4 PNG file(s) to resize:
  - logo1.png
  - logo2.png
  - logo3.png
  - logo4.png

Processing logo1.png...
  📏 Original: 4000x4000
  📏 New: 40x40 (1% scale)
  🎯 Centering on 128x128 canvas
  ✓ Created logo1_small.png
  📊 Size: 1247.3KB → 2.1KB (99.8% reduction)

🎉 All logos resized successfully!

📝 Summary:
  - Images scaled down to 1% of original dimensions (100x smaller)
  - Aspect ratio preserved
  - Transparency maintained
  - Small images centered on 128x128 canvas
  - Output files saved with "_small" suffix
```

## Requirements

- Node.js 14+ 
- Sharp library (automatically installed)

## File Structure

```
├── resize-logos.js              # Fixed 128x128 resizing
├── resize-logos-improved.js     # Proportional 1% scaling
├── package.json                 # Dependencies and scripts
├── README-resize-logos.md       # This file
├── your-logo.png               # Your original logos
└── your-logo_small.png         # Generated small versions
```

## Troubleshooting

If you get a "Sharp not found" error:
```bash
npm install sharp
```

If images appear too small, you can modify the scaling factor in the script:
```javascript
// Change 0.01 (1%) to 0.05 (5%) for larger results
const newWidth = Math.round(originalWidth * 0.05);
```
