import json, re, sys
html_path, pack_path = sys.argv[1], sys.argv[2]
pack = json.load(open(pack_path, encoding='utf-8'))
js = json.dumps(pack, ensure_ascii=False, separators=(',', ':')).replace('</', '<\\/')
html = open(html_path, encoding='utf-8').read()
new, n = re.subn(r'/\*PACOTE\*/.*?/\*FIM\*/', lambda m: '/*PACOTE*/' + js + '/*FIM*/', html, count=1, flags=re.S)
assert n == 1, 'marcador do pacote não encontrado'
open(html_path, 'w', encoding='utf-8').write(new)
print('pacote embutido: versão', pack['versao'], 'com', len(pack['questoes']), 'questões')
