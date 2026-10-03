from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import asyncio
import edge_tts
import os

VOICE_MAP = {
    "thiha": "my-MM-ThihaNeural",
    "nilar": "my-MM-NilarNeural"
}

STYLE_MAP = {
    "Normal": {"rate": 0, "pitch": 0},
    "News": {"rate": -8, "pitch": -2},
    "Story": {"rate": -22, "pitch": 4}
}


class TTSHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        if self.path == "/":
            response = b"RECAP MM AI TTS SERVER OK"
            self.send_response(200)
            self.send_header("Content-Type", "text/plain")
            self.send_header("Content-Length", str(len(response)))
            self.end_headers()
            self.wfile.write(response)
        else:
            self.send_error(404)

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
            style = data.get(
                "style",
                "Normal"
            )

            # Android slider values
            speed = int(
                data.get("speed", 100)
            )

            pitch = int(
                data.get("pitch", 0)
            )

            volume = int(
                data.get("volume", 100)
            )

            # Safe ranges
            speed = max(
                50,
                min(150, speed)
            )

            pitch = max(
                -10,
                min(10, pitch)
            )

            volume = max(
                0,
                min(100, volume)
            )

            style_settings = STYLE_MAP.get(
                style,
                STYLE_MAP["Normal"]
            )

            # Combine style + user controls
            final_rate = (
                speed
                - 100
                + style_settings["rate"]
            )

            final_pitch = (
                pitch
                + style_settings["pitch"]
            )

            # edge-tts volume:
            # 100 = +0%
            # 50  = -50%
            # 0   = -100%
            final_volume = (
                volume - 100
            )

            final_rate = max(
                -50,
                min(100, final_rate)
            )

            final_pitch = max(
                -50,
                min(50, final_pitch)
            )

            final_volume = max(
                -100,
                min(100, final_volume)
            )

            rate_string = (
                f"{final_rate:+d}%"
            )

            pitch_string = (
                f"{final_pitch:+d}Hz"
            )

            volume_string = (
                f"{final_volume:+d}%"
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

            print(
                "TTS:",
                voice_name,
                "speed=", speed,
                "pitch=", pitch,
                "volume=", volume,
                "=>",
                rate_string,
                pitch_string,
                volume_string
            )

            asyncio.run(
                edge_tts.Communicate(
                    text,
                    voice,
                    rate=rate_string,
                    pitch=pitch_string,
                    volume=volume_string
                ).save(filename)
            )

            with open(
                filename,
                "rb"
            ) as f:
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

            print(
                "TTS ERROR:",
                str(e)
            )

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


port = int(
    os.environ.get(
        "PORT",
        "8765"
    )
)

server = HTTPServer(
    ("0.0.0.0", port),
    TTSHandler
)

print("RECAP MM AI TTS SERVER")
print(f"Running on port {port}")

server.serve_forever()
