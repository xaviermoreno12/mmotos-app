#!/usr/bin/env python3
"""
M MOTOS CORE — Servidor de desarrollo
Sirve el frontend compilado (dist/) y hace proxy de /api a Spring Boot.

Uso:
    python serve.py

Requisitos previos:
    npm run build   (solo la primera vez o cuando hay cambios)
"""

import http.server
import socketserver
import urllib.request
import urllib.error
import os
import subprocess
import sys
from pathlib import Path

PORT = 5173
API_TARGET = "http://localhost:8080"
DIST_DIR = Path(__file__).parent / "dist"


def build_if_needed():
    if not DIST_DIR.exists() or not (DIST_DIR / "index.html").exists():
        print("Compilando frontend (npm run build)...")
        result = subprocess.run(
            ["npm", "run", "build"],
            cwd=Path(__file__).parent,
            shell=True,
        )
        if result.returncode != 0:
            print("Error al compilar. Verifica npm run build manualmente.")
            sys.exit(1)
        print("Compilacion exitosa.")


class SPAHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(DIST_DIR), **kwargs)

    # ------------------------------------------------------------------ GET
    def do_GET(self):
        if self.path.startswith("/api"):
            self._proxy()
        else:
            file_path = DIST_DIR / self.path.lstrip("/").split("?")[0]
            if not file_path.exists() or file_path.is_dir():
                self.path = "/index.html"
            super().do_GET()

    # ----------------------------------------------------------------- POST
    def do_POST(self):
        if self.path.startswith("/api"):
            self._proxy()
        else:
            self.send_error(405)

    # ------------------------------------------------------------------ PUT
    def do_PUT(self):
        if self.path.startswith("/api"):
            self._proxy()
        else:
            self.send_error(405)

    # --------------------------------------------------------------- PROXY
    def _proxy(self):
        target = API_TARGET + self.path
        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length) if length > 0 else None

        headers = {
            k: v for k, v in self.headers.items()
            if k.lower() not in ("host", "content-length")
        }

        try:
            req = urllib.request.Request(
                target, data=body, method=self.command, headers=headers
            )
            with urllib.request.urlopen(req, timeout=15) as resp:
                self._send_proxy_response(resp.status, resp.headers, resp.read())
        except urllib.error.HTTPError as e:
            self._send_proxy_response(e.code, e.headers, e.read())
        except Exception as exc:
            self.send_error(502, f"Backend no disponible: {exc}")

    def _send_proxy_response(self, status, headers, body):
        self.send_response(status)
        skip = {"transfer-encoding", "connection"}
        for k, v in headers.items():
            if k.lower() not in skip:
                self.send_header(k, v)
        self.send_header("Content-Length", len(body))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print(f"  {self.address_string()}  {fmt % args}")


# ======================================================================= MAIN
if __name__ == "__main__":
    build_if_needed()

    socketserver.TCPServer.allow_reuse_address = True
    with socketserver.TCPServer(("", PORT), SPAHandler) as httpd:
        print(f"\n  M MOTOS CORE  >>  http://localhost:{PORT}")
        print(f"  API proxy     >>  {API_TARGET}")
        print("  Ctrl+C para detener\n")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n  Servidor detenido.")
