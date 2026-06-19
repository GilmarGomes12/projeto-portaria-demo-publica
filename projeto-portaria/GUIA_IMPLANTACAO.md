# GUIA RÁPIDO DE IMPLANTAÇÃO

## Sistema de Portaria v1.5.1 - Deploy em Produção

### 🚀 SISTEMA PRONTO PARA PRODUÇÃO

---

## ⚡ IMPLANTAÇÃO RÁPIDA (5 minutos)

### **1. PRÉ-REQUISITOS** ✅

```bash
☑️ Windows 10/11
☑️ Java 8+ instalado
☑️ Python 3.12+ instalado
☑️ 2 câmeras USB conectadas (índices 0 e 1)
☑️ OpenCV 4.11.0+ (pip install opencv-python)
☑️ Tesseract OCR instalado
```

### **2. VERIFICAÇÃO RÁPIDA**

```bash
# Teste as câmeras
python -c "import cv2; print('Câmera 0:', cv2.VideoCapture(0).isOpened()); print('Câmera 1:', cv2.VideoCapture(1).isOpened())"

# Teste o Tesseract
python -c "import pytesseract; print('Tesseract OK')"
```

### **3. CONFIGURAÇÃO INICIAL**

```java
// No seu código principal, use:
MultiCameraDetectorService service = new MultiCameraDetectorService();

// Para 2 câmeras automáticas (recomendado)
service.setUsarDetectorReal(true);  // OCR real
service.setIntervaloDeteccao(2000); // 2 segundos entre detecções
service.iniciarMonitoramentoCompleto(); // Inicia ambas as câmeras

// Para câmera específica (se necessário)
service.iniciarMonitoramento(TipoPortao.ENTRADA); // Só entrada
service.iniciarMonitoramento(TipoPortao.SAIDA);   // Só saída
```

---

## 🔧 CONFIGURAÇÕES DE PRODUÇÃO

### **Parâmetros Recomendados**

```java
// Configuração para ambiente real
service.setUsarDetectorReal(true);        // OCR real com Tesseract
service.setIntervaloDeteccao(2000);       // 2 segundos (bom equilíbrio)

// Para ambiente de teste/demo
service.setUsarDetectorReal(false);       // Modo simulado
service.setIntervaloDeteccao(5000);       // 5 segundos (mais lento)
```

### **Mapeamento de Câmeras**

📷 Câmera 0 = TipoPortao.ENTRADA (Portão de entrada)
📷 Câmera 1 = TipoPortao.SAIDA   (Portão de saída)

---

## 📊 MONITORAMENTO

### **Verificar Status**

```java
// Status detalhado
System.out.println(service.getStatusCompleto());

// Verificações individuais
boolean entradaOK = service.isMonitorando(TipoPortao.ENTRADA);
boolean saidaOK = service.isMonitorando(TipoPortao.SAIDA);
boolean algumaAtiva = service.isAlgumaMonitorando();
boolean todasAtivas = service.isTodasMonitorando();
```

### **Logs Importantes**

✅ [INFO] 🎬 Sistema multi-câmeras em operação
✅ [INFO] 🚗 PLACA DETECTADA - Entrada: ABC1234 (confiança: 95%)
⚠️ [WARN] ⚠️ Câmera de SAÍDA não disponível - continuando apenas com ENTRADA
❌ [ERROR] ✗ Erro ao iniciar monitoramento para Entrada: Camera not found

---

## 🛠️ SOLUÇÃO DE PROBLEMAS

### **Problema: "Camera not found"**

```bash
# Verificar câmeras disponíveis
python -c "
import cv2
for i in range(5):
    cap = cv2.VideoCapture(i)
    if cap.isOpened():
        print(f'Câmera {i}: DISPONÍVEL')
        cap.release()
    else:
        print(f'Câmera {i}: não disponível')
"
```

### **Problema: "Tesseract not found"**

```bash
# Windows - instalar Tesseract
# Download: https://github.com/tesseract-ocr/tesseract/releases
# Adicionar ao PATH: C:\Program Files\Tesseract-OCR

# Verificar instalação
tesseract --version
```

### **Problema: "Exit code 2"**

✅ NORMAL - Sistema corrigido distingue:
   exit(1) = Nenhuma placa detectada (NORMAL)
   exit(2) = Erro real do sistema (PROBLEMA)

### **Problema: Interface travando**

```java
// ✅ CORRIGIDO - Threading não-bloqueante
// O sistema usa ExecutorService e não trava mais a UI
```

---

## 🔄 OPERAÇÕES COMUNS

### **Iniciar Sistema Completo**

```java
MultiCameraDetectorService service = new MultiCameraDetectorService();
service.setCallback(this::onPlacaDetectada); // Seu callback
service.setUsarDetectorReal(true);
service.iniciarMonitoramentoCompleto();
```

### **Parar Sistema**

```java
service.pararMonitoramentoCompleto();
service.shutdown(); // Libera recursos
```

### **Reiniciar Câmera Específica**

```java
// Se uma câmera falhar
service.reiniciarCamera(TipoPortao.ENTRADA);
```

### **Diagnóstico Completo**

```java
// Executa teste em todas as câmeras
service.executarDiagnostico();
```

---

## 📋 CHECKLIST DE PRODUÇÃO

### **Antes do Deploy**

- [ ] ✅ Teste de validação executado com sucesso
- [ ] ✅ Ambas as câmeras testadas individualmente
- [ ] ✅ Tesseract instalado e funcionando
- [ ] ✅ Python e dependências instaladas
- [ ] ✅ Logs de aplicação configurados

### **Durante o Deploy**

- [ ] 🔄 Fazer backup da versão anterior
- [ ] 🔄 Substituir arquivos Java corrigidos
- [ ] 🔄 Substituir arquivo Python corrigido
- [ ] 🔄 Testar conexão com as 2 câmeras
- [ ] 🔄 Executar diagnóstico completo

### **Após o Deploy**

- [ ] 📊 Monitorar logs nas primeiras 2 horas
- [ ] 📊 Verificar detecções em ambos os portões
- [ ] 📊 Confirmar ausência de erros falsos
- [ ] 📊 Validar performance (sem travamentos)

---

## 🎯 RESULTADO ESPERADO

### **Sistema Funcionando**

🎬 Sistema multi-câmeras em operação
📷 Entrada (Câmera 0): ATIVO
📷 Saída (Câmera 1): ATIVO  
⚙️ Modo: REAL
⏱️ Intervalo: 2000ms
🚗 PLACA DETECTADA - ENTRADA: ABC1234 (confiança: 95%)
🚗 PLACA DETECTADA - SAÍDA: XYZ9876 (confiança: 87%)

### **Benefícios Alcançados**

- ✅ **Zero falsos erros** - Exit codes corrigidos
- ✅ **Estabilidade total** - Backend DirectShow  
- ✅ **Recuperação automática** - Sistema de retry
- ✅ **Operação simultânea** - 2 câmeras independentes
- ✅ **Logs detalhados** - Debugging completo
- ✅ **Interface responsiva** - Threading assíncrono

---

## 📞 SUPORTE

### **Em caso de problemas:**

1. 📋 **Verificar logs** - Todas as operações são logadas
2. 🔧 **Executar diagnóstico** - `service.executarDiagnostico()`
3. 🔄 **Reiniciar sistema** - `service.pararMonitoramentoCompleto()` + `service.iniciarMonitoramentoCompleto()`
4. 📊 **Verificar câmeras** - Testar conectividade individual

### **Arquivos de Log Importantes**

- Logs Java: Saída padrão da aplicação
- Logs Python: Integrados aos logs Java
- Métricas: Status disponível via `getStatusCompleto()`

---

## 🎯 SISTEMA PRONTO PARA PRODUÇÃO 24/7

**Autor:** Gilmar H Gomes  
**Data:** 08/06/2025
**Versão:** 1.5.1 PRODUCTION-READY ✅
