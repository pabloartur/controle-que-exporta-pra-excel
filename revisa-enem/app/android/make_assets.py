import re, sys, os, urllib.request
src, out_dir = sys.argv[1], sys.argv[2]
html = open(src, encoding='utf-8').read()
font_css = ''
# Tenta embutir as fontes no APK (funciona offline); se falhar, usa as fontes do sistema
try:
    url = re.search(r'href="(https://fonts\.googleapis\.com/css2[^"]+)"', html).group(1).replace('&amp;', '&')
    req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Mobile Safari/537.36'})
    css = urllib.request.urlopen(req, timeout=30).read().decode()
    blocks = re.findall(r'/\* ([\w-]+) \*/\s*(@font-face\s*\{[^}]+\})', css)
    keep = []
    for i, (subset, block) in enumerate(blocks):
        if subset not in ('latin', 'latin-ext'):
            continue
        furl = re.search(r'url\((https://[^)]+)\)', block).group(1)
        name = 'f%d.woff2' % i
        data = urllib.request.urlopen(furl, timeout=30).read()
        open(os.path.join(out_dir, 'fonts', name), 'wb').write(data)
        keep.append(block.replace(furl, 'fonts/' + name))
    font_css = '\n'.join(keep)
    print('fonts embedded:', len(keep))
except Exception as e:
    print('fonts not embedded:', e)
html = re.sub(r'<link rel="(?:preconnect|stylesheet)"[^>]*>\n?', '', html)
head = ('<!doctype html>\n<html lang="pt-BR">\n<head>\n<meta charset="utf-8">\n'
        '<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">\n'
        '<style>body{margin:0}[hidden]{display:none!important}img{max-width:100%}\n' + font_css + '\n</style>\n</head>\n<body>\n')
open(os.path.join(out_dir, 'index.html'), 'w', encoding='utf-8').write(head + html + '\n</body>\n</html>\n')
