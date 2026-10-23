# Spring Boot Web 开发要点

## 项目分层

Spring Boot 项目通常按 controller、service、mapper、entity、dto 分层。Controller 负责接收 HTTP 请求和参数校验，Service 负责业务规则与事务，Mapper 负责 SQL 映射，Entity 对应数据库表，DTO 用于对外传输并隐藏敏感字段。

分层的价值在于职责隔离：Controller 不直接写 SQL，Service 不依赖 Servlet API，Mapper 不处理权限。这样单元测试和后续改造都更容易。小项目也不要让 Controller 承担全部逻辑，否则权限、事务和复用会迅速失控。

## 数据源与 MyBatis 配置

在 application.yml 中配置 JDBC URL、驱动、用户名和密码。MySQL 连接串应指定字符集、时区和 SSL 参数。MyBatis 需要配置 mapper-locations、type-aliases-package 和 map-underscore-to-camel-case，使数据库的 display_name 能映射到 Java 的 displayName。

生产环境的数据库密码应来自环境变量或密钥管理服务，不能提交到 Git。实训环境可以用 H2 的 MySQL 兼容模式做可重复验收，同时保留 application-mysql.yml，切换 profile 即可连接 MySQL。

## RESTful 接口设计

REST 用资源路径表达对象，用 HTTP 方法表达动作。查询用户用 GET /api/users，新增用 POST /api/users，修改用 PUT /api/users/{id}，删除用 DELETE /api/users/{id}。状态码要语义化：成功 200，创建 201，参数错误 400，未登录 401，无权限 403，资源不存在 404，冲突 409。

统一响应体可以降低前端处理成本，例如 {code, message, data}。但不要把所有错误都包装成 HTTP 200，否则监控、网关和缓存都无法正确判断结果。分页接口应返回总数、页码、页大小和列表，而不是只返回数组。

## 参数校验与异常处理

使用 Jakarta Validation 注解声明约束，例如 @NotBlank、@Size、@Pattern。Controller 参数加 @Valid 后，校验失败会抛出 MethodArgumentNotValidException，可在 @RestControllerAdvice 中统一转换为 400 响应。

业务异常应区分“用户输入问题”和“系统故障”。前者返回可读提示，后者记录日志并返回通用错误，避免把堆栈和 SQL 暴露给前端。密码、令牌等敏感字段不得出现在日志和响应中。

## 内嵌容器与部署

Spring Boot 默认使用内嵌 Tomcat，mvn spring-boot:run 可直接启动，mvn package 会生成可执行 jar，java -jar learnmate.jar 即可部署。前端构建产物可以复制到 src/main/resources/static，由同一个端口提供页面和 API，减少跨域配置。

部署时要区分开发与生产 profile，生产环境关闭 H2 控制台、Swagger 可视需要关闭，限制 CORS 来源，配置日志滚动和健康检查。端口、数据库、JWT 密钥等都应通过环境变量注入。
