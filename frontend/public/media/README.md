# Media Asset Specification

This directory holds the static and optimized media assets for the Velvet Salon frontend redesign.

## Recommended Specifications for Next Phase

### 1. Hero Background Video
- **Purpose**: Creates an immediate, immersive "sensory elegance" impression.
- **Spec**: 10-15 seconds, seamless loop, muted, no audio track.
- **Format**: MP4 or WebM, H.264/H.265 compressed.
- **Resolution**: 1080p (or 720p if bit rate is high enough) in 16:9 or 21:9.
- **Size Limit**: Under 2-4 MB total.
- **Poster Image**: Requires a high-quality WebP still frame to show while the video loads, preventing CLS.
- **Content**: Abstract salon aesthetic—slow-motion water splashing, steam, flowing hair, or warm amber lighting.

### 2. Service Imagery (Hair, Color, Spa)
- **Purpose**: High-quality editorial shots for the featured services section.
- **Spec**: 4:5 or 16:10 aspect ratio.
- **Format**: WebP (optimized for web).
- **Style**: Moody, editorial, high contrast. "Premium magazine" look.

### 3. Staff Portraits
- **Purpose**: Uniform and elegant team representation.
- **Spec**: 1:1 or 4:5 aspect ratio.
- **Format**: WebP.
- **Style**: Black-and-white or desaturated, shot against a consistent studio backdrop. Will regain color on hover.

### 4. Ambient Textures (Optional)
- **Purpose**: Dark gold/charcoal abstract textures for fallback backgrounds or glassmorphism backing.
- **Format**: WebP.

*Note: Do not commit large uncompressed media files to the repository. All files must be web-optimized.*
