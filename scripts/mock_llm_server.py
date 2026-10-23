#!/usr/bin/env python3
"""本地 OpenAI 兼容 mock，仅用于验证客户端协议、超时与降级链路。"""
from __future__ import annotations

import json
import time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path

LOG = Path("docs/evidence/llm-request.json")


class Handler(BaseHTTPRequestHandler):
    def do_POST(self):  # noqa: N802
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length).decode("utf-8")
        payload = json.loads(raw)
        LOG.parent.mkdir(parents=True, exist_ok=True)
        LOG.write_text(json.dumps({
            "time": time.strftime("%Y-%m-%d %H:%M:%S"),
            "path": self.path,
            "authorization": self.headers.get("Authorization"),
            "payload": payload,
        }, ensure_ascii=False, indent=2), encoding="utf-8")

        answer = (
            "根据课程资料，RAG 的检索流程可以分为五步：文档采集、文本切块、"
            "向量化、相似度检索、提示词组装与生成 [1]。"
            "工程上要控制 Top-K 和相似度阈值，并在资料不足时明确拒答 [2]。"
        )
        body = {
            "id": "mock-chat-1",
            "object": "chat.completion",
            "created": int(time.time()),
            "model": payload.get("model", "mock-model"),
            "choices": [{
                "index": 0,
                "message": {"role": "assistant", "content": answer},
                "finish_reason": "stop",
            }],
            "usage": {"prompt_tokens": 120, "completion_tokens": 60, "total_tokens": 180},
        }
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, fmt, *args):  # noqa: A003
        print("[mock-llm]", fmt % args)


if __name__ == "__main__":
    print("mock LLM listening on http://127.0.0.1:9999")
    ThreadingHTTPServer(("127.0.0.1", 9999), Handler).serve_forever()
