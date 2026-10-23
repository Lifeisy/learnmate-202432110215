# 脚本说明

需要在项目根目录或脚本所在目录附近执行，默认后端运行在 `http://127.0.0.1:8080`。

| 脚本 | 用途 |
|---|---|
| `build-all.ps1` | 构建前端、复制 dist 到后端 static、打包 jar |
| `ci-local.ps1` | 本地执行后端测试和前端构建 |
| `smoke_test.py` | 调用登录、用户 CRUD、问答、401/403 等接口做一轮检查 |
| `mock_llm_server.py` | 本地 OpenAI 兼容 mock，用于测试 LLM 客户端 |
| `llm_smoke.py` | 在 mock 服务运行时检查 RAG+LLM 返回 |

示例：

```powershell
# 先启动后端
cd backend
mvn spring-boot:run

# 另开终端
python scripts/smoke_test.py --out docs/evidence/smoke-test.txt
```

`build-all.ps1` 需要本机已安装 JDK、Maven、Node.js 和 npm。
