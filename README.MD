
## 项目简介
    一个基于vert.x框架,以kotlin为主要开发语言的轻量级高并发rpc框架
## 引入依赖
+ gradle
```groovy
    implementation "io.github.bitsim:vrpc-core:1.0.1"
```
+ maven
```kotlin

<dependency>
    <groupId>io.github.bitsim</groupId>
    <artifactId>vrpc-core</artifactId>
    <version>1.0.1</version>
</dependency>

```
## 待完成
- [ ] 负载均衡策略
- [ ] 超时重试
- [ ] 事件驱动架构?
- [ ] 服务降级?
- [ ] 多语言支持?
- [x] 插件化,提供SPI拓展机制
- [x] 支持多种序列化方式
- [x] 自定义传输协议
## 使用示例

### Kotlin

+ 客户端

```kotlin
    val service = RpcClientBootstrap.getService<HelloWorld>("gzhu")
println(service.sayHello("fuck you"))
```

+ 服务端

```kotlin
// 服务
val remoteServiceInfo = RemoteServiceInfo(
    serviceName = HelloWorld::class.java.name,
    serviceHost = "localhost",
    servicePort = 8080,
    serviceVersion = "gzhu"
)
// 注册服务
RpcServerBootstrap.setup(
    listOf(
        remoteServiceInfo to HelloWorldImpl::class.java
    )
)
// 启动tcp服务
RpcServerBootstrap.start()
```

+ 配置文件`vrpc.yml`或`vrpc.properties`所在目录
```text
└─resources     
    ├─vrpc.yml   
    └─vrpc.properties  
```
+  配置文件`vrpc.yml`或`vrpc.properties`配置格式
```yml
#服务端
registry:
  type: zookeeper  #指定注册中心的类型
  zookeeper:       #注册中心地址
    address: localhost:2181

serializer:   
  type: json  #指定序列化器的类型

server:
  port: 8888  #TCP服务端口
  host: localhost #TCP服务地址

#客户端
serializer:
  type: json

server:
  port: 8888
  host: localhost

```
## 支持SPI
+ 配置目录
```text
└─META-INF 
    └─vrpc         
        ├─custom   //用户自定义，优先级最高
        └─default  //vrpc框架默认
```
+ 配置格式
`$类型=$实现类`,`vrpc.yml`或`vrpc.properties`的类型根据本配置得出,
```text
json=com.bitsim.vrpc.serializer.JsonSerializer
```
已支持的接口有
`com.bitsim.vrpc.registry.Registry`、
`com.bitsim.vrpc.serializer.Serializer`
