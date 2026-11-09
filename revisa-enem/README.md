# Revisa ENEM

App de revisão para o ENEM, sem anúncios: questões no modelo ENEM, questões novas todo dia,
videoaulas que abrem no SmartTube, resumos narrados, repertório curinga e modo escuro.

- `questoes.json`: pacote de questões extras. O app (APK) baixa este arquivo pela internet.
  Para publicar questões novas, acrescente itens em `questoes` e aumente `versao`.
- `app/revisa-enem.html`: código do app (versão web).
- `app/android/`: projeto Android (WebView) usado para gerar o APK.
  A chave de assinatura não fica no repositório.
