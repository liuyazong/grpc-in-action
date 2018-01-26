package yz;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import yz.grpc.server.LoggingInterceptor;
import yz.grpc.service.impl.ComputeService;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException, InterruptedException {
        log.info("server starting...");

        NettyServerBuilder builder = NettyServerBuilder
                .forAddress(new InetSocketAddress("127.0.0.1", 2018))

                /*默认(NioServerSocketChannel.class*/
                //.channelType(NioServerSocketChannel.class)

                /*当不设置这两个值时，Grpc会调用io.grpc.netty.NettyServer.allocateSharedGroups()方法来设置其默认值。
                在io.grpc.netty.Utils.DefaultEventLoopGroupResource.create()方法种有这样一句注释：
                 Use Netty's DefaultThreadFactory in order to get the benefit of FastThreadLocal。
                默认情况下，boss group的nThread = 1，worker group的nThreads = cpu核心数<<1。并且两者都将DefaultThreadFactory的daemon属性为true，即其所产生的线程都是daemon线程。
                使用默认设置时，伴随启动类线程执行完毕，netty内线程也会退出，从而致使server并不能对外提供服务。
                因此需要用户对服务端启动类进行阻塞以防止上述情况的发生。只要保证jvm不退出，此问题可忽略，如程序在tomcat等容器内运行。*/
                //.bossEventLoopGroup(new NioEventLoopGroup(1))
                //.workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() << 1))

                /*如果channelType为NioServerSocketChannel.class或者其子类的class，自动设置SO_KEEPALIVE=true、SO_BACKLOG=128自动配置，且不可更改*/
                //.withChildOption(ChannelOption.SO_KEEPALIVE, true)
                //.withChildOption(ChannelOption.SO_BACKLOG, 256)

                .directExecutor()
                .withChildOption(ChannelOption.TCP_NODELAY, true);

        //将service注册到server以对外提供该服务
        builder = builder.addService(new ComputeService());

        //服务端interceptor，简单打印请求、响应日志
        builder = builder.intercept(new LoggingInterceptor());

        Server server = builder
                .build()
                .start();

        log.info("server started... {}", server.getPort());
        server.getServices().forEach(service -> {
            log.debug("service: {}", service.getServiceDescriptor().getName());
            service.getMethods().forEach(method -> log.debug("method: {}", method.getMethodDescriptor().getFullMethodName()));
        });

        Thread.currentThread().join();
    }
}
