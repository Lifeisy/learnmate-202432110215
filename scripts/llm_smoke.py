#!/usr/bin/env python3
"""检查配置 LLM 后问答接口的返回。"""
from __future__ import annotations

import json
import urllib.request
from pathlib import Path

BASE = "http://127.0.0.1:8081"


def post(path: str, body: dict, token: str | None = None):
    headers = {"Content-Type": "application/json; charset=utf-8"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(
        BASE + path,
        data=json.dumps(body, ensure_ascii=False).encode("utf-8"),
        headers=headers,
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        return response.status, json.loads(response.read().decode("utf-8"))


status, login = post("/api/auth/login", {"username": "student", "password": "Student@123"})
token = login["data"]["token"]
status, chat = post("/api/ai/chat", {"question": "RAG 的检索流程是什么？"}, token)
data = chat["data"]
text = (
    "LLM 联调验收（OpenAI 兼容 mock）\n"
    f"登录状态：{status}\n"
    f"mode：{data['mode']}\n"
    f"model：{data['model']}\n"
    f"来源数：{len(data['sources'])}\n"
    f"answer：{data['answer']}\n"
    f"notice：{data.get('notice')}\n"
)
Path("docs/evidence/llm-response.txt").write_text(text, encoding="utf-8")
print(text)
