from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import asyncio
import edge_tts
import os

VOICE_MAP = {
    "thiha": "my-MM-ThihaNeural",
    "nilar": "my-MM-NilarNeural"
}

class TTSHandler(BaseHTTPRequestHandler):

    def do_POST(self):

        if self.path != "/tts":
            self.send_error(404)
            return

        try:
            length = int(
                self.headers.get("Content-Length", 0)
            )

            body = self.rfile.read(length)

            data = json.loads(
                body.decode("utf-8")
            )

            text = data.get("text", "")
            voice_name = data.get(
                "voice",
                "thiha"
            )

            if not text:
                raise ValueError(
                    "Text is empty"
                )

            if len(text) > 3000:
                raise ValueError(
                    "Text limit is 3000 characters"
                )

            voice = VOICE_MAP.get(
                voice_name,
                "my-MM-ThihaNeural"
            )

            filename = "tts_output.mp3"

            asyncio.run(
                edge_tts.Communicate(
                    text,
                    voice
                ).save(filename)
            )

            with open(filename, "rb") as f:
                audio = f.read()

            self.send_response(200)

            self.send_header(
                "Content-Type",
                "audio/mpeg"
            )

            self.send_header(
                "Content-Length",
                str(len(audio))
            )

            self.end_headers()

            self.wfile.write(audio)

        except Exception as e:

            error = json.dumps({
                "error": str(e)
            }).encode("utf-8")

            self.send_response(500)

            self.send_header(
                "Content-Type",
                "application/json"
            )

            self.send_header(
                "Content-Length",
                str(len(error))
            )

            self.end_headers()

            self.wfile.write(error)


port = int(os.environ.get("PORT", "8765"))
server = HTTPServer(
    ("0.0.0.0", port),
    TTSHandler
)

print("RECAP MM AI TTS SERVER")
print("Running on port 8765")

server.serve_forever()
