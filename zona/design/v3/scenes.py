"""Procedural cinematic key art for Zona mockups (no third-party imagery)."""
import sys
import numpy as np
from PIL import Image, ImageFilter

OUT = sys.argv[1]
rng = np.random.default_rng(7)


def hexrgb(h):
    h = h.lstrip('#')
    return np.array([int(h[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float32) / 255.0


def vgrad(h, w, stops):
    """Vertical gradient from list of (pos, hex)."""
    ys = np.linspace(0, 1, h)
    img = np.zeros((h, w, 3), np.float32)
    pos = [p for p, _ in stops]
    cols = np.array([hexrgb(c) for _, c in stops])
    for ch in range(3):
        img[:, :, ch] = np.interp(ys, pos, cols[:, ch])[:, None]
    return img


def glow(img, cx, cy, r, col, strength=1.0):
    h, w, _ = img.shape
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.sqrt(((xx - cx * w) / w) ** 2 + ((yy - cy * h) / w) ** 2)
    a = np.clip(1 - d / r, 0, 1) ** 2.2 * strength
    return img + a[:, :, None] * hexrgb(col)


def disc(img, cx, cy, r, col):
    h, w, _ = img.shape
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.sqrt((xx - cx * w) ** 2 + (yy - cy * h) ** 2)
    a = np.clip((r * w - d) / 2.0, 0, 1)
    return img * (1 - a[:, :, None]) + a[:, :, None] * hexrgb(col)


def ridge(w, base, amp, rough, seed, octaves=6):
    r = np.random.default_rng(seed)
    x = np.linspace(0, 1, w)
    y = np.zeros(w)
    f, a = 2.0, amp
    for _ in range(octaves):
        ph = r.uniform(0, 2 * np.pi)
        ph2 = r.uniform(0, 2 * np.pi)
        y += a * (np.sin(x * f * np.pi * 2 + ph) * 0.6 + np.sin(x * f * 1.7 * np.pi * 2 + ph2) * 0.4)
        f *= 2.1
        a *= rough
    return base + y


def layer(img, line, col, fog_col=None, fog=0.0):
    h, w, _ = img.shape
    yy = np.arange(h)[:, None]
    lim = (line * h)[None, :]
    a = np.clip(yy - lim + 1, 0, 1)
    c = hexrgb(col)
    if fog_col is not None:
        depth = np.clip((yy - lim) / (0.35 * h), 0, 1)
        fc = hexrgb(fog_col)
        colmap = c[None, None, :] * (1 - fog * (1 - depth[:, :, None])) + fc[None, None, :] * fog * (1 - depth[:, :, None])
    else:
        colmap = c[None, None, :]
    return img * (1 - a[:, :, None]) + a[:, :, None] * colmap


def fogband(img, y, thick, col, alpha):
    h, w, _ = img.shape
    yy = np.linspace(0, 1, h)[:, None]
    a = np.exp(-((yy - y) / thick) ** 2) * alpha
    return img * (1 - a[:, :, None]) + a[:, :, None] * hexrgb(col)


def stars(img, n, top=0.6):
    h, w, _ = img.shape
    for _ in range(n):
        x, y = rng.integers(0, w), rng.integers(0, int(h * top))
        b = rng.uniform(0.3, 1.0)
        img[y, x] = np.clip(img[y, x] + b, 0, 1)
    return img


def poly(img, pts, col):
    """Fill polygon given in relative coords."""
    from PIL import ImageDraw
    h, w, _ = img.shape
    m = Image.new('L', (w, h), 0)
    ImageDraw.Draw(m).polygon([(x * w, y * h) for x, y in pts], fill=255)
    a = np.asarray(m.filter(ImageFilter.GaussianBlur(0.6)), np.float32)[:, :, None] / 255
    return img * (1 - a) + a * hexrgb(col)


def rects(img, boxes, col):
    from PIL import ImageDraw
    h, w, _ = img.shape
    m = Image.new('L', (w, h), 0)
    d = ImageDraw.Draw(m)
    for x0, y0, x1, y1 in boxes:
        d.rectangle([x0 * w, y0 * h, x1 * w, y1 * h], fill=255)
    a = np.asarray(m, np.float32)[:, :, None] / 255
    return img * (1 - a) + a * hexrgb(col)


def water(img, horizon, tint, strength=0.65):
    h, w, _ = img.shape
    hy = int(horizon * h)
    above = img[:hy][::-1]
    n = h - hy
    refl = above[:n] if above.shape[0] >= n else np.concatenate([above, np.repeat(above[-1:], n - above.shape[0], 0)])
    refl = np.asarray(Image.fromarray((np.clip(refl, 0, 1) * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3)), np.float32) / 255
    yy = np.arange(n)[:, None]
    ripple = (np.sin(yy * 0.9 + np.arange(w)[None, :] * 0.02) * 0.5 + 0.5)[:, :, None] * 0.08
    t = hexrgb(tint)
    img[hy:] = refl * strength + t * (1 - strength) - ripple
    return img


def finish(img, grain=0.035, vig=0.55):
    h, w, _ = img.shape
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.sqrt(((xx / w) - 0.5) ** 2 + ((yy / h) - 0.5) ** 2)
    img = img * (1 - vig * np.clip(d - 0.25, 0, 1) ** 1.5 * 2)[:, :, None]
    img = img + rng.normal(0, grain, (h, w, 1))
    img = np.clip(img, 0, 1) ** (1 / 1.05)
    return img


def save(img, name):
    Image.fromarray((np.clip(img, 0, 1) * 255).astype(np.uint8)).save(f'{OUT}/{name}.jpg', quality=84, optimize=True, progressive=True)


def lighthouse(img, x, base, s):
    img = poly(img, [(x - 0.012 * s, base), (x - 0.007 * s, base - 0.16 * s), (x + 0.007 * s, base - 0.16 * s), (x + 0.012 * s, base)], '#05080b')
    img = rects(img, [(x - 0.011 * s, base - 0.185 * s, x + 0.011 * s, base - 0.16 * s)], '#05080b')
    img = glow(img, x, base - 0.172 * s, 0.09, '#ffe6a8', 0.9)
    return img


def scene_north(w, h, name):
    img = vgrad(h, w, [(0, '#0b1a24'), (0.45, '#2f5a6b'), (0.62, '#8fb3b8'), (1, '#0a1218')])
    img = glow(img, 0.7, 0.5, 0.5, '#9cc6c9', 0.45)
    img = layer(img, ridge(w, 0.56, 0.035, 0.5, 1), '#1d3640', '#6f9aa3', 0.5)
    img = layer(img, ridge(w, 0.6, 0.02, 0.45, 2), '#0f2027')
    img = water(img, 0.62, '#0d1d24', 0.55)
    img = lighthouse(img, 0.27, 0.6, 1.0 if w > h else 0.8)
    img = fogband(img, 0.6, 0.05, '#a7c7c9', 0.25)
    save(finish(img), name)


def scene_harbor(w, h, name):
    img = vgrad(h, w, [(0, '#2a1638'), (0.35, '#b2456b'), (0.55, '#f39a5b'), (0.62, '#ffd59a'), (1, '#1a0f1c')])
    img = glow(img, 0.5, 0.6, 0.6, '#ffb36b', 0.8)
    img = disc(img, 0.5, 0.58, 0.07, '#ffe7b8')
    img = layer(img, ridge(w, 0.6, 0.015, 0.5, 4), '#3d1f37', '#e07a62', 0.6)
    img = water(img, 0.63, '#2a1328', 0.6)
    for bx in (0.18, 0.72, 0.84):
        img = poly(img, [(bx - 0.05, 0.64), (bx + 0.05, 0.64), (bx + 0.035, 0.67), (bx - 0.035, 0.67)], '#160a14')
        img = rects(img, [(bx - 0.002, 0.52, bx + 0.002, 0.64)], '#160a14')
    save(finish(img), name)


def scene_night_flight(w, h, name):
    img = vgrad(h, w, [(0, '#03060f'), (0.5, '#0c1d3d'), (0.75, '#2b4a7c'), (1, '#0b1428')])
    img = stars(img, int(w * h / 900), 0.55)
    img = glow(img, 0.72, 0.22, 0.25, '#cfe0ff', 0.35)
    img = disc(img, 0.72, 0.22, 0.045, '#eef3ff')
    for i, y in enumerate((0.66, 0.74, 0.84)):
        img = layer(img, ridge(w, y, 0.03, 0.55, 10 + i), ['#2c4573', '#1b2f55', '#0d1a33'][i], '#6d8cc4', 0.45)
    img = poly(img, [(0.3, 0.42), (0.42, 0.405), (0.45, 0.4), (0.42, 0.415), (0.3, 0.43)], '#050913')
    img = poly(img, [(0.36, 0.418), (0.4, 0.418), (0.33, 0.46)], '#050913')
    img = glow(img, 0.45, 0.4, 0.02, '#ff5a5a', 0.9)
    save(finish(img), name)


def scene_dunes(w, h, name):
    img = vgrad(h, w, [(0, '#3b1d12'), (0.4, '#c8642e'), (0.55, '#f2b46b'), (1, '#2a120a')])
    img = disc(img, 0.35, 0.42, 0.11, '#ffd9a0')
    img = glow(img, 0.35, 0.42, 0.5, '#ff9a4a', 0.6)
    for i, y in enumerate((0.55, 0.64, 0.76)):
        img = layer(img, ridge(w, y, 0.05 - i * 0.01, 0.35, 20 + i, 4), ['#a24d23', '#6e2f16', '#3a170b'][i], '#f0a462', 0.5)
    img = poly(img, [(0.63, 0.635), (0.637, 0.585), (0.644, 0.635)], '#1a0a04')
    save(finish(img), name)


def scene_forest(w, h, name):
    img = vgrad(h, w, [(0, '#c9d6cc'), (0.5, '#7d978a'), (1, '#0d1a14')])
    img = glow(img, 0.6, 0.25, 0.5, '#f4f1dc', 0.5)
    for i in range(4):
        y = 0.5 + i * 0.11
        col = ['#5d786c', '#3d5a4d', '#22392f', '#0c1913'][i]
        r = np.random.default_rng(30 + i)
        n = 9 + i * 5
        for _ in range(n):
            x = r.uniform(-0.05, 1.05)
            th = r.uniform(0.18, 0.32) * (1 + i * 0.25)
            wd = th * 0.28
            img = poly(img, [(x, y - th), (x + wd, y + 0.05), (x - wd, y + 0.05)], col)
        img = layer(img, np.full(w, y + 0.04), col)
        img = fogband(img, y - 0.02, 0.05, '#d6e0d6', 0.35 - i * 0.07)
    save(finish(img), name)


def scene_neon(w, h, name):
    img = vgrad(h, w, [(0, '#090314'), (0.55, '#2a0b3d'), (0.75, '#7a1e5c'), (1, '#07030d')])
    img = glow(img, 0.5, 0.78, 0.6, '#ff3d8b', 0.5)
    r = np.random.default_rng(40)
    x = 0.0
    boxes, wins = [], []
    while x < 1:
        bw = r.uniform(0.05, 0.13)
        top = r.uniform(0.3, 0.62)
        boxes.append((x, top, x + bw, 1))
        for wy in np.arange(top + 0.02, 0.95, 0.025):
            for wx in np.arange(x + 0.01, x + bw - 0.01, 0.018):
                if r.random() < 0.35:
                    wins.append((wx, wy, wx + 0.007, wy + 0.009))
        x += bw + r.uniform(0.0, 0.015)
    img = rects(img, boxes, '#0c0614')
    img = rects(img, [b for i, b in enumerate(wins) if i % 3], '#ffc2e2')
    img = rects(img, [b for i, b in enumerate(wins) if not i % 3], '#7ef2ff')
    img = fogband(img, 0.8, 0.08, '#ff4f9e', 0.25)
    save(finish(img), name)


def scene_orbit(w, h, name):
    img = vgrad(h, w, [(0, '#010205'), (1, '#060a18')])
    img = stars(img, int(w * h / 500), 1.0)
    img = glow(img, 0.5, 1.15, 0.9, '#3b7bff', 0.55)
    img = disc(img, 0.5, 1.25 if h > w else 1.6, 0.75, '#050a1a')
    img = glow(img, 0.78, 0.3, 0.12, '#ffd6a1', 0.6)
    img = disc(img, 0.78, 0.3, 0.05, '#e8b98a')
    save(finish(img, vig=0.3), name)


def scene_peaks(w, h, name):
    img = vgrad(h, w, [(0, '#1b1440'), (0.35, '#6b4c9a'), (0.55, '#e7a3b5'), (1, '#120e24')])
    img = glow(img, 0.5, 0.5, 0.4, '#ffd0d6', 0.4)
    img = layer(img, ridge(w, 0.5, 0.11, 0.48, 50), '#e9dcf0', '#ffffff', 0.15)
    img = layer(img, ridge(w, 0.58, 0.08, 0.5, 51), '#7a6aa6', '#cdb6e0', 0.45)
    img = layer(img, ridge(w, 0.7, 0.05, 0.5, 52), '#3a2f63', '#9a86c4', 0.4)
    img = layer(img, ridge(w, 0.82, 0.03, 0.5, 53), '#161230')
    save(finish(img), name)


SCENES = {'north': scene_north, 'harbor': scene_harbor, 'flight': scene_night_flight, 'dunes': scene_dunes,
          'forest': scene_forest, 'neon': scene_neon, 'orbit': scene_orbit, 'peaks': scene_peaks}

for key, fn in SCENES.items():
    fn(600, 900, f'p-{key}')
    fn(1280, 720, f'w-{key}')
print('ok')
