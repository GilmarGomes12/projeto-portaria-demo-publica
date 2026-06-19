# 🧪 Scripts de Testes

Esta pasta contém scripts para testes automatizados e validação do sistema de portaria.

## 📋 Scripts Disponíveis

### 🔍 Testes de OCR

- **`teste_ocr_corrigido.bat`** - Script de teste para detecção de placas via OCR
  - Testa a funcionalidade de reconhecimento de caracteres
  - Valida a detecção de placas em imagens
  - Útil para verificar se o sistema de OCR está funcionando corretamente

## 🔧 Como Usar

### Pré-requisitos

- Sistema Windows (para scripts .bat)
- Aplicação de portaria compilada e funcionando
- Imagens de teste com placas de veículos (se aplicável)

### Execução

```cmd
# Navegar para a pasta de testes
cd Scripts\Testes

# Executar teste de OCR
teste_ocr_corrigido.bat
```

## 📊 Tipos de Teste

### Testes Funcionais

- Validação de detecção de placas
- Verificação de precisão do OCR
- Teste de integração com banco de dados

### Testes de Performance

- Tempo de resposta da detecção
- Consumo de recursos durante OCR
- Estabilidade em execução contínua

## 📁 Estrutura Esperada

Scripts/Testes/
├── README.md                    # Este arquivo
├── teste_ocr_corrigido.bat     # Teste principal de OCR
├── imagens_teste/              # (futuro) Imagens para teste
└── resultados/                 # (futuro) Logs de resultados

## 🔗 Integração com Sistema

Os testes validam componentes do sistema principal:

- **OCR Engine**: Tesseract OCR para reconhecimento de placas
- **Banco de Dados**: Consultas na tabela de veículos
- **Interface**: Feedback visual dos resultados
- **Logs**: Registro de atividades e erros

## ⚠️ Notas Importantes

1. **Ambiente Controlado**: Execute testes em ambiente de desenvolvimento
2. **Backup**: Sempre tenha backup do banco antes de testes que modificam dados
3. **Logs**: Monitore os logs durante execução dos testes
4. **Resultados**: Documente resultados para análise posterior

## 🔗 Arquivos Relacionados

- **Scripts de Desenvolvimento**: `../Desenvolvimento/`
- **Código Principal**: `../../projeto-portaria/src/`
- **Documentação**: `../../Documentacao/2_Tecnica/`
- **Logs do Sistema**: `../../logs/`
