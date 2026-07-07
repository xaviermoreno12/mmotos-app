#!/bin/bash
# Script de deploy del OCR service en el VPS de Hostinger
# Ejecutar desde el VPS via SSH: bash deploy-ocr.sh

set -e

echo "=== 1. Verificando RAM disponible ==="
free -h
echo ""

echo "=== 2. Agregando swap de 2GB (por si el VPS tiene poca RAM) ==="
if [ ! -f /swapfile ]; then
    fallocate -l 2G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    echo "Swap creado."
else
    echo "Swap ya existe."
fi

echo "=== 3. Instalando dependencias del sistema ==="
apt-get update -q
apt-get install -y python3-pip python3-dev libgl1 libglib2.0-0

echo "=== 4. Instalando Flask + EasyOCR (puede tardar 5-10 min) ==="
pip3 install flask easyocr pillow

echo "=== 5. Copiando archivos ==="
cp ocr_service.py /root/ocr_service.py
cp ocr.service /etc/systemd/system/ocr.service

echo "=== 6. Activando servicio ==="
systemctl daemon-reload
systemctl enable ocr
systemctl start ocr

echo ""
echo "=== Esperando 60s para que EasyOCR cargue los modelos... ==="
sleep 60

echo "=== Estado del servicio ==="
systemctl status ocr --no-pager

echo ""
echo "=== Prueba rapida ==="
curl -s http://localhost:5050/health
echo ""
echo "=== DEPLOY COMPLETO ==="
