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

## Video Asset Update
- `hero-ambience-loop.mp4` (~2.6 MB) - Slow ambient zoom loop created locally via python script from the `hero-ambience.webp` image, replacing the static placeholder. The static `.webp` is retained as a poster and for reduced-motion fallback. Future phases may consider scroll-scrubbing or scroll-triggered effects.

## Recommended Specifications for Future Enhancements

### 1. Hero Background Video (Integrated)
- The hero section now features a cinematic MP4 loop with `motion-reduce:hidden` fallback.

### 2. Service Imagery (Hair, Color, Spa)
- Currently populated with high-quality generated WEBP placeholders. Can be replaced with real photography later matching the same aspect ratio (16:10).

### 3. Staff Portraits
- Currently populated with abstract backlit WEBP placeholders. Can be replaced with uniform desaturated real staff portraits (4:5 aspect ratio) when available.

*Note: Do not commit large uncompressed media files to the repository. All files must be web-optimized.*
