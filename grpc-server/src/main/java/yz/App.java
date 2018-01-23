package yz;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.util.TransmitStatusRuntimeExceptionInterceptor;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import yz.grpc.server.LoggingInterceptor;
import yz.grpc.service.impl.ComputeService;

import java.io.IOException;
import java.net.InetSocketAddress;

@Slf4j
public class App {
    public static void main(String[] args) throws IOException {
        log.info("server starting...");

        NettyServerBuilder builder = NettyServerBuilder.forAddress(new InetSocketAddress("127.0.0.1", 2018))
                .channelType(NioServerSocketChannel.class)
                .bossEventLoopGroup(new NioEventLoopGroup(1))
                .workerEventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()))
                .withChildOption(ChannelOption.SO_KEEPALIVE, true)
                .withChildOption(ChannelOption.SO_BACKLOG, 1024)
                .withChildOption(ChannelOption.TCP_NODELAY, true);

        builder = builder.addService(new ComputeService());
        builder = builder.intercept(TransmitStatusRuntimeExceptionInterceptor.instance())
                .intercept(new LoggingInterceptor());

        Server server = builder
                .build()
                .start();

        log.info("server started... {}", server.getPort());
        server.getServices().forEach(service -> {
            log.debug("service: {}", service.getServiceDescriptor().getName());
            service.getMethods().forEach(method -> {
                log.debug("method: {}", method.getMethodDescriptor().getFullMethodName());
            });
        });
    }
}
