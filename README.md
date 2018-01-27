# GRpc实战及原理分析

## 消息及服务定义

1. message定义

        message InputMessage {
            double numA = 1;
            double numB = 2;
        }
        
        message OutputMessage {
            double result = 1;
        }
        
2. 数据类型

        proto type      java type       default
        double          double          0
        float           float           0
        int32           int             0
        int64           long            0
        bool            bool            false
        string          String          ""
        bytes           ByteString      

3. service定义

        service ComputeService {
            //Simple RPC。客户端发送一个并等待服务端的响应
            rpc add (InputMessage) returns (OutputMessage) {
        
            }
        
            //Client-side streaming RPC。客户端写消息序列到流并把他们通过流发送到服务端，发送完成后等待服务器端返回结果
            rpc subtract (stream InputMessage) returns (OutputMessage) {
        
            }
        
            //Server-side streaming RPC。客户端发送一个请求，服务端返回一个流给客户端，客户端从流中读取消息序列直到读取完
            rpc multiply (InputMessage) returns (stream OutputMessage) {
        
            }
        
            //Bidirectional streaming RPC。客户端和服务端都可以通过流来发送消息序列，客户端和服务端读写的顺序是任意的
            rpc divide (stream InputMessage) returns (stream OutputMessage) {
        
            }
        }

4. options

        option java_multiple_files = true; //是否为每个message生成单独的java文件
        option java_package = "yz.grpc.proto.service"; //生成文件的包名
        option java_outer_classname = "ComputeServiceProto";
        
5. 其他
    
    其他消息类型，如map类型、嵌套、引用其他proto文件定义，见[官方教程](https://developers.google.com/protocol-buffers/docs/proto3)。
    
## API

### 服务端API

1. `io.grpc.Server`

    监听和分发客户端请求。实现类：`io.grpc.internal.ServerImpl`

2. `io.grpc.netty.NettyServerBuilder`

    提供一系列静态方法用于构建`io.grpc.Server`实例
    
3. `io.grpc.BindableService`
    
    绑定service实例到server的方法。service实例继承`xxxGrpc.xxxImplBase`（proto生成的类），而`xxxGrpc.xxxImpl`Base实现了`io.grpc.BindableService`接口。调用ServerBuilder的`public abstract T addService(BindableService bindableService);`方法即可将service绑定到server对外提供服务。
    
4. `io.grpc.ServerInterceptor`
    
    服务端拦截器，在处理请求之前或之后做一些工作，如认证、日志、将请求转发到其他server

### 客户端API

1. `io.grpc.stub.AbstractStub`
    
    用于调用服务端提供的服务方法，由proto根据service定义生成其实现类
    
2. `io.grpc.netty.NettyChannelBuilder`
    
    提供一系列静态方法用于构建`io.grpc.ManagedChannel`实例

3. `io.grpc.ClientInterceptor`

    客户端拦截器，在客户端发送请求或接收响应时做一些工作，如认证、日志、request/response重写

## 原理分析

### 服务端

1. server端在使用`io.grpc.netty.NettyServerBuilder`构建`io.grpc.Server`实例时，通过调用`addService(BindableService bindableService);`方法将`service`实例解析为`io.grpc.ServerServiceDefinition`对象，最终以`ServerServiceDefinition.getServiceDescriptor().getName()`为key，以`io.grpc.ServerServiceDefinition`实例为value存储在`io.grpc.internal.InternalHandlerRegistry.Builder`属性内的`java.util.LinkedHashMap`实例中。

2. 接下来调用`io.grpc.netty.NettyServerBuilder`的`build`方法，该方法调用构造器`ServerImpl(AbstractServerImplBuilder<?> builder,InternalServer transportServer,Context rootContext)`返回`io.grpc.Server`实例。该构造函数调用`builder`参数的`build`方法将`java.util.LinkedHashMap`中方key、value封装为`io.grpc.internal.InternalHandlerRegistry`实例并赋值给`io.grpc.internal.ServerImpl`的`io.grpc.internal.InternalHandlerRegistry`属性。参数`transportServer`由`io.grpc.netty.NettyServerBuilder`的`io.grpc.netty.NettyServerBuilder.buildTransportServer`方法构建，实际上它是一个`io.grpc.netty.NettyServer`的实例。

3. 接下来看`io.grpc.internal.ServerImpl`的`start`方法，其内部调用了`io.grpc.netty.NettyServer`的`start`方法，这里才开始netty server的配置及启动。这里中点关注`io.netty.bootstrap.ServerBootstrap.childHandler(io.netty.channel.ChannelHandler)`方法，在`io.grpc.netty.NettyServerTransport`的`start`方法内对`io.grpc.netty.NettyServerHandler`进行实例化并把该实例添加到netty的channel链中。

### 客户端

客户端实例化ManagedChannel并实例化对stub，像调用本地方法一样调用远程方法就行了。

那么，服务端是如何直到客户端调用的哪个方法的呢？接着看。

###  `io.grpc.netty.NettyClientStream.Sink.writeHeaders`与`io.grpc.netty.NettyServerHandler.onHeadersRead`

1. 客户端writeHeaders

    从stub的远程方法调用入口跟进去，会发现，实际调用的是`io.grpc.stub.ClientCalls`的几个公有静态方法。在调用这些方法时，stub首先会把方法相关的返回值类型、入参类型、方法全名(`service全名+／+方法名`)封装为`io.grpc.MethodDescriptor`实例。然后会构造`io.grpc.internal.ClientCallImpl`实例并调用其`start`方法，在这个方法内构造了`io.grpc.netty.NettyClientStream`实例并调用其`start`方法，这里就到了writeHeaders的地方：以`:path`为key、以`/+方法全名`为value写入`io.netty.handler.codec.http2.Http2Headers`实例中。

2. 服务端onHeadersRead

    服务端在读取完客户端请求头后会调用`io.grpc.netty.NettyServerHandler`的`onHeadersRead`方法。该方法从请求头中取出path值，然后调用`io.grpc.internal.InternalHandlerRegistry`(`io.grpc.internal.ServerImpl`实例的一个属性，服务端启动时已初始化并把service信息放入其中)的`lookupMethod`方法更具path值找到对应的处理方法。

### 整体流程

1. 服务端将service实例添加到server中，并将其构造为`io.grpc.internal.InternalHandlerRegistry`实例。
2. 客户端请求时将所要调用的方法信息以path、value的方式放在请求头中。
3. 服务端在接到请求头后取出path对应的值并到`io.grpc.internal.InternalHandlerRegistry`实例中找到对应的`io.grpc.ServerCallHandler`来完成其请求的处理。

## GRpc官方文档

[官方教程](https://grpc.io/docs/tutorials/basic/java.html)