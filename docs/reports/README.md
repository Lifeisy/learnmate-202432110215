# 实验报告目录

## 最终提交版（一个文档）

按《团队规范实训》模板合并五个项目，共 26 页：

- [`202432110215李耀.docx`](202432110215李耀.docx)（可编辑）
- [`202432110215李耀.pdf`](202432110215李耀.pdf)（打印/预览）

文档结构：封面 → 目录 → 项目一至项目五；每个项目按“项目介绍、相关开发技术、项目设计（概要+详细+数据库）、运行结果、心得（使用 AI 情况）”展开。代码细节放在项目源码中，报告不再单列“关键代码”章节；AI 部分只保留辅助工具、使用环节、提示词要点与人工核对记录。

## 五份独立过程报告（备查）

| 实验 | 报告文件 | 对应代码/证据 |
|---|---|---|
| 实验一 搭建 Web 基础项目 | `实验一_Web基础项目_李耀.docx` | `backend/`、`docs/screenshots/lab1-startup.png` |
| 实验二 前后端分离与 RESTful API | `实验二_前后端分离与REST_李耀.docx` | `frontend/`、登录/总览/用户/问答截图 |
| 实验三 Git 协作与持续集成 | `实验三_Git协作与CI_李耀.docx` | Git 图、`ci.yml`、PR 模板、冲突记录 |
| 实验四 登录、权限与 AI 安全 | `实验四_登录权限与AI安全_李耀.docx` | JWT、BCrypt、RBAC、注入防护、HTTP 验收 |
| 实验五 LLM 与 RAG 原型 | `实验五_LLM与RAG原型_李耀.docx` | `ai/`、mock 协议图、问答截图 |

## 生成方式

```powershell
python scripts/build_combined_report.py docs/reports/combined-pages.json
python scripts/finalize_combined.py docs/reports/combined-pages.json
python scripts/build_reports.py
```

`finalize_combined.py` 从 `docs/reports/source/combined-with-code.docx` 读取底稿，删除“关键代码”章节和残留代码块，压缩 AI 使用表述，并按实际页数更新目录。

报告中的截图来自 `docs/screenshots/`，测试与命令证据来自 `docs/evidence/`。

## 说明

- 实验一默认使用 H2 内存库便于验收，MySQL 8 通过 `application-mysql.yml` 切换。
- 实验三的 CI 配置已提交；由于本机没有 GitHub 远程，报告使用“本地等价命令”证据，并明确不冒充云端 Actions 结果。
- 实验五未使用真实外部大模型 Key；本地 mock 仅验证 OpenAI 兼容协议与 RAG+LLM 切换。报告中对此有明确标注。
