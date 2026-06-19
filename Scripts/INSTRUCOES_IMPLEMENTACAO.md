# 🎯 INSTRUÇÕES ESPECÍFICAS PARA SEU PROJETO DE PORTARIA

## 🚨 AÇÃO IMEDIATA NECESSÁRIA

### 1️⃣ **INSTALAR DEPENDÊNCIAS PYTHON**

```bash
cd c:\Development\projeto-portaria-1.5\Scripts
configurar_python.bat
```

### 2️⃣ **TESTAR IMEDIATAMENTE**

```bash
# Testar com câmera USB (se tiver)
python teste_deteccao.py 1

# Ou testar com uma imagem de placa
python teste_deteccao.py 3
```

---

## 🔄 INTEGRAÇÃO COM SEU CÓDIGO JAVA EXISTENTE

### 📍 **Localização dos Arquivos Java**

Baseado na estrutura do seu projeto, você deve integrar no arquivo principal da portaria.

### 🔧 **Passos de Integração**

#### **Passo 1: Copiar a Classe Java**

Copie o conteúdo de `MonitorPlacasService.java` para seu projeto Java principal.

#### **Passo 2: Adaptar o Callback**

```java
MonitorPlacasService.PlacaDetectadaCallback callback = 
    new MonitorPlacasService.PlacaDetectadaCallback() {
    
    @Override
    public void onPlacaDetectada(String placa) {
        // SUBSTITUIR pela sua lógica existente de verificação de placa
        verificarPlacaNoBancoDados(placa);
        exibirPopupAutorizacao(placa);
    }
    
    @Override
    public void onErro(String erro) {
        // Seu sistema de log/notificação existente
        Logger.error("Erro detecção placas: " + erro);
    }
    
    @Override
    public void onStatusMudou(String status) {
        // Atualizar status na interface se necessário
        atualizarStatusInterface(status);
    }
};
```

#### **Passo 3: Inicializar o Monitor**

```java
// No método de inicialização da sua aplicação
MonitorPlacasService monitorPlacas = new MonitorPlacasService(callback);

// Para câmera USB
monitorPlacas.iniciarMonitoramento("0");

// Para câmera IP (substitua pela URL real da sua câmera)
// monitorPlacas.iniciarMonitoramento("rtsp://admin:senha@192.168.1.100:554/stream1");
```

---

## 📹 CONFIGURAÇÃO DA CÂMERA

### 🔍 **Descobrir URL RTSP da Sua Câmera**

1. **Acesse a interface web da câmera** (geralmente `http://IP_DA_CAMERA`)
2. **Procure por "RTSP" ou "Stream"** nas configurações
3. **Teste a URL** com VLC Media Player primeiro

### 📋 **URLs Comuns por Marca**

Hikvision: rtsp://admin:senha@IP:554/Streaming/Channels/101
Dahua:     rtsp://admin:senha@IP:554/cam/realmonitor?channel=1&subtype=0
Intelbras: rtsp://admin:senha@IP:554/unicast
Axis:      rtsp://admin:senha@IP:554/axis-media/media.amp

---

## ⚙️ CONFIGURAÇÕES RECOMENDADAS

### 🎯 **Para Ambiente de Portaria**

Edite `monitor_portao.py` e ajuste:

```python
# Linha ~21: Tempo entre detecções (segundos)
COOLDOWN_SEGUNDOS = 15  # Aumentar para 15s em ambiente real

# Linha ~47: Tamanho mínimo para detectar carro
if cv2.contourArea(contorno) > 8000:  # Aumentar para carros maiores

# Linha ~34: Área de interesse
roi = frame[altura//3:, :]  # Focar no terço inferior (onde passam os carros)
```

### 🔊 **Área de Detecção Visual**

Para ver a área que está sendo monitorada, descomente estas linhas em `monitor_portao.py`:

```python
# Adicionar após linha ~35 para debug visual
cv2.rectangle(frame, (0, altura//3), (frame.shape[1], frame.shape[0]), (0, 255, 0), 2)
cv2.imshow("Monitor Portao - Area Verde = Deteccao", frame)
cv2.waitKey(1)
```

---

## 🚦 FLUXO OPERACIONAL

### 📋 **Como Funcionará na Prática**

1. **Sistema inicia** → Python monitora câmera em background
2. **Carro se aproxima** → Movimento detectado na área de interesse
3. **OCR ativado** → Placa é lida e enviada para Java
4. **Java processa** → Consulta banco + exibe popup automático
5. **Cooldown ativo** → Sistema "descansa" 15 segundos
6. **Processo se repete** → Pronto para próximo carro

### 🎛️ **Controles do Porteiro**

O porteiro NÃO precisa mais:

- ❌ Clicar em "Detectar Placa"
- ❌ Posicionar câmera manualmente
- ❌ Aguardar processamento

O sistema automaticamente:

- ✅ Detecta quando carro chega
- ✅ Lê a placa
- ✅ Mostra popup de autorização
- ✅ Registra no banco de dados

---

## 🐛 SOLUÇÃO DE PROBLEMAS

### ❌ **"Erro ao conectar câmera"**

1. Verifique se a URL RTSP está correta
2. Teste no VLC Player primeiro
3. Verifique usuário/senha da câmera

### ❌ **"Tesseract não encontrado"**

1. Baixe: [Tesseract OCR](https://github.com/UB-Mannheim/tesseract/wiki)
2. Instale no caminho padrão
3. Descomente linha do tesseract_cmd no código

### ❌ **"Placa não detectada"**

1. Ajuste área de interesse (`altura//3`)
2. Diminua tamanho mínimo de movimento (`5000`)
3. Teste com imagem estática primeiro

### ❌ **"Muitas detecções falsas"**

1. Aumente `COOLDOWN_SEGUNDOS`
2. Aumente tamanho mínimo de movimento
3. Ajuste área de interesse

---

## 📊 MONITORAMENTO E LOGS

### 📝 **Logs Úteis**

```bash
# Monitorar output em tempo real
python monitor_portao.py 0

# Resultado:
STATUS: Monitoramento iniciado com sucesso.
PLACA:ABC1234
PLACA:XYZ5678
AVISO: Perda de sinal da camera. Tentando reconectar...
```

### 📈 **Métricas Importantes**

- Placas detectadas por hora
- Taxa de erro de OCR
- Tempo de resposta do sistema
- Falsos positivos/negativos

---

## 🎯 PRÓXIMA AÇÃO

**AGORA MESMO:**

1. Execute `configurar_python.bat`
2. Teste com `python teste_deteccao.py 1`
3. Se funcionar, integre no seu Java
4. Configure URL da câmera real
5. Ajuste parâmetros conforme ambiente

## 🚀 Sistema Pronto

O sistema está pronto para revolucionar sua portaria!
