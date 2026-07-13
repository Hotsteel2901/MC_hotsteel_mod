#!/usr/bin/env python3
"""Process AI-generated textures into Minecraft item textures (16x16 PNG with transparency).

Pipeline:
1. Load source image (1024x1024 or similar)
2. Crop to subject (remove dark background margin)
3. Make background transparent (based on color similarity to corner)
4. Pad to square with transparency
5. Downscale to 16x16 with LANCZOS (preserves pixel art better than nearest)
6. Optional: posterize colors to limited palette
"""
import sys
import os
from PIL import Image, ImageFilter
import numpy as np


def process_texture(src_path, dst_path, target_size=16, bg_threshold=25, padding_pct=8, palette_colors=12):
    im = Image.open(src_path).convert('RGBA')
    w, h = im.size
    arr = np.array(im)

    # Detect background color from corners (assume background is uniform dark)
    corners = np.concatenate([
        arr[0:8, 0:8].reshape(-1, 4),
        arr[0:8, -8:].reshape(-1, 4),
        arr[-8:, 0:8].reshape(-1, 4),
        arr[-8:, -8:].reshape(-1, 4),
    ])
    bg_color = corners[:, :3].mean(axis=0)

    # Build alpha: pixel is opaque if it's significantly different from bg
    diff = np.abs(arr[:, :, :3].astype(int) - bg_color.astype(int)).max(axis=2)
    alpha = np.clip(diff * (255 / max(bg_threshold, 1)), 0, 255).astype(np.uint8)

    # Smooth the alpha edge slightly
    alpha_im = Image.fromarray(alpha, mode='L').filter(ImageFilter.GaussianBlur(0.6))
    alpha = np.array(alpha_im)

    # Apply new alpha
    arr[:, :, 3] = alpha

    # Find tight bounding box of non-transparent pixels
    mask = alpha > 8
    if not mask.any():
        print(f'  warning: no opaque pixels in {src_path}', file=sys.stderr)
        return False

    ys, xs = np.where(mask)
    y0, y1 = ys.min(), ys.max() + 1
    x0, x1 = xs.min(), xs.max() + 1
    cropped = Image.fromarray(arr[y0:y1, x0:x1])
    cw, ch = cropped.size

    # Pad to square using transparent
    side = max(cw, ch)
    pad = int(side * padding_pct / 100)
    side_padded = side + pad * 2
    square = Image.new('RGBA', (side_padded, side_padded), (0, 0, 0, 0))
    off_x = (side_padded - cw) // 2
    off_y = (side_padded - ch) // 2
    square.paste(cropped, (off_x, off_y), cropped)

    # Downscale to target using LANCZOS then crisp nearest for pixel art look
    small = square.resize((target_size, target_size), Image.LANCZOS)

    # Optional: posterize to reduce color palette (helps match vanilla style)
    small_rgb = small.convert('RGB')
    small_q = small_rgb.quantize(colors=palette_colors, method=Image.Quantize.MEDIANCUT).convert('RGB')
    out = Image.merge('RGBA', (*small_q.split(), small.split()[3]))

    # Save
    os.makedirs(os.path.dirname(dst_path), exist_ok=True)
    out.save(dst_path, 'PNG', optimize=True)
    return True


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('usage: process_texture.py <src> <dst> [size=16] [palette=12]', file=sys.stderr)
        sys.exit(2)
    src = sys.argv[1]
    dst = sys.argv[2]
    size = int(sys.argv[3]) if len(sys.argv) > 3 else 16
    palette = int(sys.argv[4]) if len(sys.argv) > 4 else 12
    ok = process_texture(src, dst, target_size=size, palette_colors=palette)
    if ok:
        print(f'  ok -> {dst} ({size}x{size})')
    else:
        print(f'  fail', file=sys.stderr)
        sys.exit(1)