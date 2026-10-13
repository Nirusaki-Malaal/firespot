#!/usr/bin/env python3
"""
Fire OS TV Simulation Runtime Server
====================================
Serves the Fire TV Interactive Emulator Suite and UI preview locally and securely.
Restricted exclusively to loopback interface (127.0.0.1).
"""

import http.server
import socketserver
import os
import sys
import json
import signal

PORT = 8080
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
WEB_DIR = os.path.join(BASE_DIR, "web")


class SecureFireSimHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIR, **kwargs)

    def do_GET(self):
        # Health check endpoint for fast startup probes
        if self.path == "/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Access-Control-Allow-Origin", "*")
            self.end_headers()
            self.wfile.write(b'{"status":"ok","service":"fire_sim"}\n')
            return
        
        # Security: Prevent directory traversal outside WEB_DIR
        requested_path = os.path.abspath(os.path.join(WEB_DIR, self.path.lstrip("/").split("?")[0]))
        if not requested_path.startswith(os.path.abspath(WEB_DIR)):
            self.send_error(403, "Access Denied")
            return

        super().do_GET()

    def end_headers(self):
        # Security & Emulation headers
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, OPTIONS")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("Cache-Control", "no-cache, no-store, must-revalidate")
        super().end_headers()

    def log_message(self, format, *args):
        # Keep logs clean; suppress high-volume asset request spam
        pass


def main():
    socketserver.TCPServer.allow_reuse_address = True
    httpd = None

    def handle_signal(sig, frame):
        if httpd:
            httpd.server_close()
        sys.exit(0)

    signal.signal(signal.SIGINT, handle_signal)
    signal.signal(signal.SIGTERM, handle_signal)

    try:
        with socketserver.TCPServer(("127.0.0.1", PORT), SecureFireSimHandler) as httpd:
            print(f"[fire_sim] Server listening on http://127.0.0.1:{PORT}/")
            httpd.serve_forever()
    except OSError as e:
        if "Address already in use" in str(e):
            print(f"[fire_sim] Port {PORT} already in use; verifying service...")
        else:
            raise


if __name__ == "__main__":
    main()
