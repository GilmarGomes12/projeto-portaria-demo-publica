# Integração Python-Java para Detecção de Placas

## Arquitetura Simplificada

### Scripts Python (Responsabilidades)

1. **detector_placa_final.py** - Funções de OCR e detecção de placas
2. **monitor_portao.py** - Monitoramento contínuo de câmeras com detecção de movimento

### Java (Responsabilidades)

- Iniciar/parar processos Python
- Receber placas detectadas via stdout
- Consultar banco de dados
- Lógica de negócio (autorização/negação)
- Interface do usuário (popups)

## Como Usar

### 1. Teste Básico (Imagem/Câmera USB)

```bash
python detector_placa_final.py 0  # Câmera USB 0
python detector_placa_final.py "caminho/para/imagem.jpg"
```

### 2. Monitoramento Contínuo (Câmera IP)

```bash
python monitor_portao.py "rtsp://usuario:senha@192.168.1.100:554/stream1"
python monitor_portao.py 0  # Também funciona com câmera USB
```

## Comunicação Java ↔ Python

### Saídas do Python que o Java deve capturar

**Monitor iniciado com sucesso:**

STATUS: Monitoramento iniciado com sucesso.

**Placa detectada:**

PLACA:ABC1234

**Erros:**

ERRO_FATAL: Nao foi possivel conectar a camera rtsp://...
AVISO: Perda de sinal da camera. Tentando reconectar...

### Exemplo de integração Java

```java
public class MonitorPlacas {
    private Process processPython;
    private BufferedReader reader;
    
    public void iniciarMonitoramento(String urlCamera) {
        try {
            // Inicia o processo Python
            ProcessBuilder pb = new ProcessBuilder(
                "python", 
                "Scripts/monitor_portao.py", 
                urlCamera
            );
            processPython = pb.start();
            
            // Captura o output
            reader = new BufferedReader(new InputStreamReader(
                processPython.getInputStream()
            ));
            
            // Thread para ler output continuamente
            new Thread(() -> {
                String linha;
                try {
                    while ((linha = reader.readLine()) != null) {
                        processarOutput(linha);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void processarOutput(String linha) {
        if (linha.startsWith("PLACA:")) {
            String placa = linha.substring(6); // Remove "PLACA:"
            // Aqui você chama sua lógica de negócio Java
            verificarPlacaNoBanco(placa);
        } else if (linha.startsWith("STATUS:")) {
            System.out.println("Monitor iniciado: " + linha);
        } else if (linha.startsWith("ERRO_FATAL:")) {
            System.err.println("Erro crítico: " + linha);
            // Talvez parar o monitoramento e alertar o usuário
        }
    }
    
    private void verificarPlacaNoBanco(String placa) {
        // Sua lógica existente aqui
        // Consulta BD, decide se libera/nega, mostra popup
    }
    
    public void pararMonitoramento() {
        if (processPython != null) {
            processPython.destroy();
        }
    }
}
```

## Configuração de Câmeras IP

### Formatos de URL RTSP comuns

- `rtsp://192.168.1.100:554/stream1`
- `rtsp://admin:senha123@192.168.1.100:554/cam/realmonitor?channel=1&subtype=0`
- `rtsp://usuario:senha@192.168.1.100:8554/unicast`

### Para descobrir a URL da sua câmera

1. Verifique o manual da câmera
2. Acesse a interface web da câmera
3. Use ferramentas como VLC Media Player para testar URLs

## Parâmetros Ajustáveis

### No monitor_portao.py

- `COOLDOWN_SEGUNDOS = 10` - Tempo entre detecções
- `cv2.contourArea(contorno) > 5000` - Tamanho mínimo para detectar movimento
- `altura//2` - Área de interesse (metade inferior da imagem)

### Dependências Python necessárias

```bash
pip install opencv-python pytesseract
```

### Tesseract OCR

- Baixar de: [GitHub - UB-Mannheim/tesseract](https://github.com/UB-Mannheim/tesseract/wiki)
- Descomentar e ajustar a linha no detector_placa_final.py se necessário:

```python
pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'
```
