#!/bin/bash
# Script de Configuração do Ambiente Ubuntu - Sistema de Portaria v1.6.1

set -e

echo "=========================================================="
echo "  CONFIGURAÇÃO AMBIENTE UBUNTU - Sistema Portaria v1.6.1"
echo "=========================================================="
echo

if [ "$EUID" -eq 0 ]; then 
    echo "[ERRO] Não execute como root/sudo"
    exit 1
fi

echo "[1/7] Atualizando sistema..."
sudo apt update

echo "[2/7] Instalando Java 21 e Maven..."
sudo apt install -y openjdk-21-jdk maven

echo "[3/7] Instalando Python 3..."
sudo apt install -y python3 python3-pip python3-venv python3-dev

echo "[4/7] Instalando Tesseract OCR..."
sudo apt install -y tesseract-ocr tesseract-ocr-por libtesseract-dev libleptonica-dev
sudo apt install -y libgl1-mesa-glx libglib2.0-0 libsm6 libxext6 libxrender-dev libgomp1

echo "[5/7] Instalando Git e ferramentas..."
sudo apt install -y git curl wget build-essential pkg-config

echo "[6/7] Configurando variáveis de ambiente..."
if ! grep -q "JAVA_HOME" ~/.bashrc; then
    echo "" >> ~/.bashrc
    echo "export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64" >> ~/.bashrc
    echo "export PATH=\$JAVA_HOME/bin:\$PATH" >> ~/.bashrc
fi

echo "[7/7] Criando diretório do projeto..."
mkdir -p ~/Projetos/projeto-portaria

echo ""
echo "================================================"
echo "  AMBIENTE CONFIGURADO COM SUCESSO!"
echo "================================================"
echo ""
echo "Próximos passos:"
echo "  1. source ~/.bashrc"
echo "  2. Copiar projeto para: ~/Projetos/projeto-portaria"
echo "  3. Ajustar caminhos Windows->Linux nos arquivos Java"
echo "  4. cd ~/Projetos/projeto-portaria/projeto-portaria"
echo "  5. python3 -m venv venv && source venv/bin/activate"
echo "  6. pip install opencv-python pytesseract numpy pillow"
echo "  7. mvn clean install"
echo "  8. java -jar target/projeto-portaria-1.5.jar"
echo ""