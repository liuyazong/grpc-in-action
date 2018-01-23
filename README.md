
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
    
    其他信息，如map类型、嵌套、引用其他proto文件定义，见[官方教程](https://developers.google.com/protocol-buffers/docs/proto3)。
    
## API

### 服务端

1. `io.grpc.Server`

    监听和分发客户端请求。实现类：`io.grpc.internal.ServerImpl`

2. `io.grpc.netty.NettyServerBuilder`

    提供一系列静态方法用于构建`io.grpc.Server`实例
    
3. `io.grpc.BindableService`
    
    绑定service实例到server的方法。service实例继承`xxxGrpc.xxxImplBase`（proto生成的类），而`xxxGrpc.xxxImpl`Base实现了`io.grpc.BindableService`接口。
    调用ServerBuilder的`public abstract T addService(BindableService bindableService);`方法即可将service绑定到server对外提供服务。
    
4. `io.grpc.ServerInterceptor`
    
    服务端拦截器，在处理请求之前或之后做一些工作，如认证、日志、将请求转发到其他server

5. `io.grpc.ClientInterceptor`

    客户端拦截器，在客户端发送请求或接收响应时做一些工作，如认证、日志、request/response重写

### 客户端

1. `io.grpc.stub.AbstractStub`
    
    用于调用服务端提提供的方法，由proto根据service定义生成其实现类
    
2. `io.grpc.netty.NettyChannelBuilder`
    
    提供一系列静态方法用于构建`io.grpc.ManagedChannel`实例
    
## Grpc官方文档

[官方教程](https://grpc.io/docs/tutorials/basic/java.html)