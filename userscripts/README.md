# Userscripts para SiteHub

## O que são Userscripts?

Userscripts são pequenos scripts em JavaScript que modificam o comportamento e a aparência de sites diretamente no WebView do SiteHub. Eles permitem adicionar funcionalidades, remover elementos indesejados ou melhorar a experiência de navegação.

## Formato `.shub`

O SiteHub usa um formato proprietário compactado chamado **`.shub`** (SiteHub Userscript Bundle). Um arquivo `.shub` é simplesmente um **ZIP** renomeado contendo:

```
meu-script.shub
├── manifest.json
└── script.js
```

### manifest.json

Arquivo de metadados obrigatório:

```json
{
  "name": "Nome do Script",
  "version": "1.0",
  "description": "Descrição do que o script faz",
  "match": [
    "*://*.exemplo.com/*",
    "*://exemplo.com/*"
  ]
}
```

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `name` | string | Nome exibido no app |
| `version` | string | Versão do script |
| `description` | string | Descrição curta |
| `match` | array | Padrões de URL onde o script será injetado |

### script.js

O código JavaScript que será injetado na página. Pode usar qualquer API do DOM:

```javascript
(function() {
    document.addEventListener('DOMContentLoaded', function() {
        document.title = "Modificado!";
    });
})();
```

## Como criar um .shub

### Método 1: Linha de comando (Linux/Termux)

```bash
mkdir meu-script
cd meu-script

# Crie o manifest.json
cat > manifest.json << 'EOF'
{
  "name": "Meu Script",
  "version": "1.0",
  "description": "Faz algo legal",
  "match": ["*://*.exemplo.com/*"]
}
EOF

# Crie o script.js
cat > script.js << 'EOF'
(function() {
    console.log("Script carregado!");
})();
EOF

# Compacte como .shub
zip -r ../meu-script.shub manifest.json script.js
```

### Método 2: Qualquer gerenciador de arquivos

1. Crie uma pasta com `manifest.json` e `script.js`
2. Compacte a pasta como ZIP
3. Renomeie a extensão de `.zip` para `.shub`

## Como importar no SiteHub

1. Abra o SiteHub
2. Pressione e segure o ícone de um site
3. Toque em **"Userscripts"**
4. Toque no botão **+**
5. Selecione o arquivo `.shub`
6. O script aparecerá na lista e pode ser ativado/desativado

## Exemplo incluído: Better xCloud

Na pasta `exemplos/better-xcloud/` você encontra o arquivo `better-xcloud.shub` pronto para importar.

Este script é baseado no projeto [better-xcloud](https://github.com/redphx/better-xcloud) e adiciona melhorias à interface do Xbox Cloud Gaming:

- Remove elementos desnecessários da interface
- Adiciona atalhos rápidos
- Melhora a responsividade dos controles touch
- Otimiza o layout para telas menores

### Como usar:

1. Copie `exemplos/better-xcloud/better-xcloud.shub` para seu dispositivo
2. Adicione o site `https://www.xbox.com/play` no SiteHub
3. Pressione e segure o ícone do Xbox Cloud
4. Toque em **Userscripts** > **+**
5. Selecione o arquivo `better-xcloud.shub`
6. Ative o script e abra o site

## Dicas

- Use `match` com padrões específicos para evitar injeção em sites errados
- Envolva seu código em uma IIFE `(function(){ ... })()` para evitar conflitos
- Use `DOMContentLoaded` ou `MutationObserver` para garantir que o DOM está pronto
- Scripts desativados não são injetados — use o toggle na lista de userscripts

## Estrutura de padrões `match`

| Padrão | Corresponde a |
|--------|---------------|
| `*://*.exemplo.com/*` | Qualquer subdomínio de exemplo.com |
| `*://exemplo.com/*` | Apenas exemplo.com |
| `*://*.xbox.com/play*` | Páginas do Xbox Cloud |
| `https://*/*` | Qualquer site HTTPS |
