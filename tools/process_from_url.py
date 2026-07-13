#!/usr/bin/env python3
"""Download an AI-generated texture from a URL and process it into a Minecraft item PNG.

Usage: process_from_url.py <url> <dst_png> [size=16] [palette=12]
"""
import sys
import os
import urllib.request
from process_texture import process_texture

def main():
    if len(sys.argv) < 3:
        print('usage: process_from_url.py <url> <dst_png> [size=16] [palette=12]', file=sys.stderr)
        sys.exit(2)
    url = sys.argv[1]
    dst = sys.argv[2]
    size = int(sys.argv[3]) if len(sys.argv) > 3 else 16
    palette = int(sys.argv[4]) if len(sys.argv) > 4 else 12

    tmp = f'/tmp/_texgen_{os.getpid()}_{os.path.basename(dst)}'
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'opencode-mcmod-texgen/1.0'})
        with urllib.request.urlopen(req, timeout=60) as r:
            with open(tmp, 'wb') as f:
                f.write(r.read())
        ok = process_texture(tmp, dst, target_size=size, palette_colors=palette)
        if ok:
            print(f'  ok -> {dst} ({size}x{size})')
        else:
            print(f'  fail', file=sys.stderr)
            sys.exit(1)
    finally:
        if os.path.exists(tmp):
            os.unlink(tmp)

if __name__ == '__main__':
    main()