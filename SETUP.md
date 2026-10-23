# 环境配置与运行

## 1. 软件版本

需要安装：

- JDK 17 或 21（项目按 Java 17 编译，用更高版本也可以）
- Maven 3.9+
- Node.js 20+
- MySQL 8（可选）
- Docker（可选，用来快速启动 MySQL）

检查环境：

```powershell
java -version
mvn -v
node -v
npm -v
```

如果 `mvn` 找不到，把 Maven 的 `bin` 目录加入 `Path`，并把 `JAVA_HOME` 指向 JDK 目录。

## 2. 默认运行：H2

项目默认使用 H2 内存库，不需要安装 MySQL。启动时会自动建表，并插入演示账号和知识库数据；重启后数据重置。

```powershell
cd backend
mvn spring-boot:run
```

启动后访问：

- 应用：<http://127.0.0.1:8080>
- 接口文档：<http://127.0.0.1:8080/swagger-ui.html>
- H2 控制台：<http://127.0.0.1:8080/h2-console>

H2 连接信息：

```text
JDBC URL：jdbc:h2:mem:learnmate;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
用户名：sa
密码：留空
```

账号：

```text
admin / Admin@123     管理员
student / Student@123 普通用户
```

## 3. 使用 MySQL

### 3.1 Docker

在项目根目录执行：

```powershell
docker compose up -d
```

`docker-compose.yml` 会创建：

```text
数据库：learnmate
用户：learnmate
密码：LearnMate@2026
端口：3306
```

然后启动后端：

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

或者用 jar：

```powershell
java -jar target/learnmate.jar --spring.profiles.active=mysql
```

如果本机 3306 已被占用，把 `docker-compose.yml` 的端口改成 `3307:3306`，再把 `DB_URL` 里的端口改成 3307。

### 3.2 本机 MySQL

先建库和账号：

```sql
CREATE DATABASE learnmate
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'learnmate'@'localhost' IDENTIFIED BY 'LearnMate@2026';
GRANT ALL PRIVILEGES ON learnmate.* TO 'learnmate'@'localhost';
FLUSH PRIVILEGES;
```

表结构在 `backend/src/main/resources/db/schema.sql`，应用启动时会自动执行；也可以手动运行 `sql/init.sql`。

### 3.3 连接配置

`backend/src/main/resources/application-mysql.yml`：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/learnmate?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${DB_USER:learnmate}
    password: ${DB_PASSWORD:LearnMate@2026}
  sql:
    init:
      mode: always
      schema-locations: classpath:db/schema.sql
```

不想改文件时，用环境变量覆盖：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/learnmate?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USER="learnmate"
$env:DB_PASSWORD="你的密码"
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Linux/macOS 用 `export` 设置同样的变量。

## 4. 前端

开发模式：

```powershell
cd frontend
npm install
npm run dev
```

打开 <http://127.0.0.1:5173>。Vite 会把 `/api` 请求代理到 8080。

生产构建：

```powershell
cd frontend
npm run build
```

把 `frontend/dist/` 里的文件复制到 `backend/src/main/resources/static/`，然后：

```powershell
cd ../backend
mvn clean package
java -jar target/learnmate.jar
```

也可以直接运行：

```powershell
.\scripts\build-all.ps1
```

注意要用 `mvn clean package`。普通的 `package` 不会删除 `target/classes/static/assets` 里的旧文件，可能导致页面还加载旧 JS。

## 5. 大模型配置

默认 `LLM_ENABLED=false`，不调用外部模型，问答使用本地检索。

要接兼容 OpenAI Chat Completions 的服务，设置：

```powershell
$env:LLM_ENABLED="true"
$env:LLM_BASE_URL="https://api.deepseek.com"
$env:LLM_API_KEY="你的Key"
$env:LLM_MODEL="deepseek-chat"
cd backend
mvn spring-boot:run
```

`LLM_BASE_URL` 只写到域名或 `/v1`，不要自己再拼 `/chat/completions`。

Key 只放在环境变量里，不要写进 `application.yml`、前端代码或 Git。

## 6. 配置文件

- 默认 H2：`backend/src/main/resources/application.yml`
- MySQL：`backend/src/main/resources/application-mysql.yml`
- 测试：`backend/src/main/resources/application-test.yml`
- 建表：`backend/src/main/resources/db/schema.sql`
- MySQL 初始化：`sql/init.sql`
- 环境变量示例：`.env.example`

Spring Boot 不会自动读取 `.env`，需要在 PowerShell 或终端里手动设置环境变量。

常用变量：

```text
SERVER_PORT       后端端口，默认 8080
SPRING_PROFILES_ACTIVE   h2 或 mysql
JWT_SECRET        JWT 密钥，至少 32 字节
DB_URL / DB_USER / DB_PASSWORD
LLM_ENABLED / LLM_BASE_URL / LLM_API_KEY / LLM_MODEL
```

## 7. 测试

```powershell
cd backend
mvn test
```

共 16 个测试，使用 H2 内存库。

接口冒烟测试：

```powershell
python scripts/smoke_test.py --out docs/evidence/smoke-test.txt
```

## 8. 常见问题

**端口被占用**

```powershell
netstat -ano | findstr :8080
java -jar target/learnmate.jar --server.port=8081
```

**MySQL 连接失败**

先确认 MySQL 已启动、数据库已创建、用户名密码正确，再检查 URL 中的端口、时区和 `useSSL` 参数。

**页面还是旧样式**

执行 `mvn clean package`，确认 `backend/src/main/resources/static/assets` 里只有当前 hash 的文件。

**前端 5173 跨域失败**

检查 `vite.config.js` 的代理，以及 `SecurityConfig` 中允许的来源端口。换端口时两处都要改。

**没有大模型 Key**

可以直接用本地检索模式，页面会返回资料摘录和来源，不影响其他功能。
