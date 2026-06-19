# Utilitários do Sistema de Portaria v1.5.1

Esta pasta contém ferramentas e scripts utilitários para manutenção, monitoramento e validação do sistema de portaria.

## 📁 Conteúdo

### 🔍 Scripts de Monitoramento

#### `monitor_performance.py`

**Descrição**: Monitor avançado de performance do sistema OCR

- Analisa logs detalhadamente usando regex
- Gera estatísticas de confiança das detecções
- Monitora autorizações de veículos
- Verifica health do pool de conexões
- Detecta e reporta erros

**Uso**:

```bash
cd utils
python monitor_performance.py
```

**Saída**: Relatório completo de performance no console

#### `monitor_performance.bat`

**Descrição**: Versão batch do monitor (funcionalidade básica)

- Monitor simplificado para Windows
- Análise básica de logs
- Contadores de detecções e autorizações

**Uso**:

```cmd
cd utils
monitor_performance.bat
```

### ✅ Scripts de Validação

#### `validacao_sistema.bat`

**Descrição**: Validação completa do sistema

- Verifica compilação Maven
- Testa dependências OCR (Tesseract)
- Valida estrutura de arquivos
- Executa teste funcional rápido

**Uso**:

```cmd
cd utils
validacao_sistema.bat
```

**Checklist de validação**:

- ✅ Compilação sem erros
- ✅ Dependências OCR disponíveis
- ✅ Banco de dados acessível
- ✅ Estrutura de logs configurada
- ✅ Teste funcional aprovado

## 🛠️ Scripts de Manutenção Futuros

### Sugestões para expansão

#### `backup_sistema.py`

- Backup automático do banco de dados
- Compressão de logs antigos
- Verificação de integridade

#### `limpeza_logs.py`

- Rotação automática de logs
- Compressão de arquivos antigos
- Manutenção de espaço em disco

#### `saude_sistema.py`

- Health check completo
- Métricas de CPU/RAM
- Status de conectividade

#### `relatorio_diario.py`

- Relatório automatizado diário
- Envio por email
- Dashboard de métricas

## 📊 Exemplos de Uso

### Análise Rápida de Performance

```bash
# Executar da pasta raiz do projeto
python utils/monitor_performance.py
```

### Validação Antes do Deploy

```cmd
# Executar da pasta raiz do projeto
utils\validacao_sistema.bat
```

### Monitoramento Contínuo

```bash
# Agendar no cron/task scheduler
0 */6 * * * cd /path/to/projeto && python utils/monitor_performance.py >> logs/monitoring.log
```

## 🔧 Configuração

### Dependências Python

```bash
# Nenhuma dependência externa necessária
# Usa apenas bibliotecas padrão: re, os, datetime
```

### Variáveis de Ambiente

```bash
# Opcional: definir diretório de logs personalizado
export PORTARIA_LOG_DIR="/custom/path/logs"
```

## 📈 Métricas Disponíveis

### Performance OCR

- Taxa de detecção válida (%)
- Confiança média das detecções
- Distribuição por faixas de confiança
- Placas únicas identificadas

### Sistema

- Pool de conexões (reconexões)
- Autorizações vs negações
- Detecção de erros nos logs
- Uptime e estabilidade

### Alertas Automáticos

- ⚠️ Muitas reconexões de DB (>50)
- ⚠️ Performance baixa (<75%)
- ❌ Erros críticos detectados
- ⚠️ Logs não encontrados

## 🚀 Próximas Melhorias

1. **Integração com Prometheus/Grafana**
2. **Alertas via WhatsApp/Email**
3. **Dashboard web em tempo real**
4. **API REST para métricas**
5. **Machine Learning para detecção de anomalias**

---

**Versão**: 1.5.1  
**Criado**: Janeiro 2025  
**Mantido por**: GHG Sistemas
