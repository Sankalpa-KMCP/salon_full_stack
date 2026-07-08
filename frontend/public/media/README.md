# Media Asset Specification

This directory holds the static and optimized media assets for the Velvet Salon frontend redesign.

## Current Phase Assets (Generated WebP)
The following optimized placeholder assets have been generated and integrated into the landing page:
- `hero-ambience.webp` (~53 KB) - Abstract luxury salon atmosphere for the cinematic hero background.
- `service-precision-cut.webp` (~103 KB) - Close-up of professional shears for the Haircut service card.
- `service-color-ritual.webp` (~68 KB) - Warm lighting, color bowls and brushes for the Color service card.
- `service-spa-finish.webp` (~58 KB) - Folded towels and steam for the Spa service card and Sensory Story section.
- `portrait-stylist-1.webp` (~55 KB) - Abstract backlit silhouette for team member portraits.
**Total Media Size Added: ~337 KB**

## Recommended Specifications for Next Phase

### 1. Hero Background Video (Future Integration)
- **Purpose**: Creates an immediate, immersive "sensory elegance" impression. Will replace `hero-ambience.webp`.
- **Spec**: 10-15 seconds, seamless loop, muted, no audio track.
- **Format**: MP4 or WebM, H.264/H.265 compressed.
- **Resolution**: 1080p (or 720p if bit rate is high enough) in 16:9 or 21:9.
- **Size Limit**: Under 2-4 MB total.
- **Poster Image**: Requires a high-quality WebP still frame (like `hero-ambience.webp`) to show while the video loads, preventing CLS.
- **Content**: Abstract salon aesthetic—slow-motion water splashing, steam, flowing hair, or warm amber lighting.

### 2. Service Imagery (Hair, Color, Spa)
- Currently populated with high-quality generated WEBP placeholders. Can be replaced with real photography later matching the same aspect ratio (16:10).

### 3. Staff Portraits
- Currently populated with abstract backlit WEBP placeholders. Can be replaced with uniform desaturated real staff portraits (4:5 aspect ratio) when available.

*Note: Do not commit large uncompressed media files to the repository. All files must be web-optimized.*
