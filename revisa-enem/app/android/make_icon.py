import sys, os
from PIL import Image, ImageDraw, ImageFont
res = sys.argv[1]
N = 512
img = Image.new('RGBA', (N, N), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.rounded_rectangle([16, 16, N-16, N-16], radius=110, fill=(42, 71, 196, 255))
d.rectangle([118, 330, 394, 380], fill=(247, 218, 74, 255))  # traço de marca-texto
font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', 300)
bbox = d.textbbox((0, 0), 'E', font=font)
w, h = bbox[2]-bbox[0], bbox[3]-bbox[1]
d.text(((N-w)/2 - bbox[0], (N-h)/2 - bbox[1] - 20), 'E', font=font, fill=(255, 255, 255, 255))
for name, px in [('mdpi', 48), ('hdpi', 72), ('xhdpi', 96), ('xxhdpi', 144), ('xxxhdpi', 192)]:
    p = os.path.join(res, 'mipmap-' + name)
    os.makedirs(p, exist_ok=True)
    img.resize((px, px), Image.LANCZOS).save(os.path.join(p, 'ic_launcher.png'))
img.resize((192, 192), Image.LANCZOS).save(os.path.join(os.path.dirname(res), 'icon-preview.png'))
