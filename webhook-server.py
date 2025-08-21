#!/usr/bin/env python3

import http.server
import socketserver
import subprocess
import json
import os
import hmac
import hashlib
from urllib.parse import urlparse, parse_qs

# Configuration
PORT = 8081
WEBHOOK_SECRET = "listen-app-secret-key-2024"  # You can change this
SCRIPT_DIR = "/home/henry/listen"
BUILD_SCRIPT = os.path.join(SCRIPT_DIR, "auto-build-deploy.sh")

class WebhookHandler(http.server.BaseHTTPRequestHandler):
    def do_POST(self):
        # Get the content length
        content_length = int(self.headers.get('Content-Length', 0))
        payload = self.rfile.read(content_length)
        
        # Verify webhook signature (optional but recommended)
        signature = self.headers.get('X-Hub-Signature-256', '')
        if signature:
            expected_signature = 'sha256=' + hmac.new(
                WEBHOOK_SECRET.encode('utf-8'),
                payload,
                hashlib.sha256
            ).hexdigest()
            
            if not hmac.compare_digest(signature, expected_signature):
                self.send_response(401)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'Invalid signature')
                return
        
        try:
            # Parse JSON payload
            data = json.loads(payload.decode('utf-8'))
            
            # Check if this is a push event to main branch
            if (data.get('ref') == 'refs/heads/master' and 
                data.get('repository', {}).get('name') == 'listen'):
                
                print(f"[{self.log_date_time_string()}] Push event detected for listen repo")
                
                # Start build process in background
                subprocess.Popen([
                    '/bin/bash', BUILD_SCRIPT
                ], cwd=SCRIPT_DIR, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
                
                # Send success response
                self.send_response(200)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'Webhook received, build started')
                
                print(f"[{self.log_date_time_string()}] Build process started")
            else:
                # Not a push to main branch or wrong repo
                self.send_response(200)
                self.send_header('Content-type', 'text/plain')
                self.end_headers()
                self.wfile.write(b'Ignored (not main branch or wrong repo)')
                
        except Exception as e:
            print(f"[{self.log_date_time_string()}] Error processing webhook: {e}")
            self.send_response(500)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write(b'Internal server error')
    
    def do_GET(self):
        # Simple health check endpoint
        self.send_response(200)
        self.send_header('Content-type', 'text/plain')
        self.end_headers()
        self.wfile.write(b'Listen webhook server is running')
    
    def log_message(self, format, *args):
        # Custom logging
        print(f"[{self.log_date_time_string()}] {format % args}")

if __name__ == "__main__":
    print(f"Starting webhook server on port {PORT}")
    print(f"Webhook URL: http://your-server-ip:{PORT}/")
    print(f"Build script: {BUILD_SCRIPT}")
    
    with socketserver.TCPServer(("", PORT), WebhookHandler) as httpd:
        print("Webhook server is running. Press Ctrl+C to stop.")
        httpd.serve_forever()
