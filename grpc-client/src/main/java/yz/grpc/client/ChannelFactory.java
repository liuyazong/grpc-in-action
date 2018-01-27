package yz.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Slf4j
public class ChannelFactory extends BaseKeyedPooledObjectFactory<ObjectKey, ManagedChannel> {
    @Override
    public ManagedChannel create(ObjectKey key) throws Exception {
        ManagedChannel managedChannel = NettyChannelBuilder
                .forAddress(key.getAddress())

                /*默认为NioSocketChannel.class*/
                //.channelType(NioSocketChannel.class)

                /*默认nThreads=1， 并且将DefaultThreadFactory的daemon属性为true，即其所产生的线程都是daemon线程。
                使用默认设置时，伴随启动类线程退出netty请求线程也退出，会导致用户请求还未获得响应甚至请求还未发送到服端时。
                因此需要用户对客户端启动类进行阻塞以防止上述情况的发生。只要保证jvm不退出，此问题可忽略，如程序在tomcat等容器内运行。*/
                /*创建自定义的NioEventLoopGroup*/
                .eventLoopGroup(new NioEventLoopGroup(1, new DefaultThreadFactory("grpc-worker-group")))

                .directExecutor()
                .withOption(ChannelOption.SO_KEEPALIVE, true)
                .withOption(ChannelOption.TCP_NODELAY, true)
                .negotiationType(NegotiationType.PLAINTEXT)

                //客户端interceptor，简单打印请求、响应日志
                .intercept(new LoggingInterceptor())
                .build();
        log.debug("create channel key: {}, channel: {}", key, managedChannel);
        return managedChannel;
    }

    @Override
    public PooledObject<ManagedChannel> wrap(ManagedChannel value) {
        return new DefaultPooledObject<>(value);
    }

    @Override
    public boolean validateObject(ObjectKey key, PooledObject<ManagedChannel> p) {
        ManagedChannel object = p.getObject();
        if (object.isShutdown() || object.isTerminated()) {
            return false;
        }
        return super.validateObject(key, p);
    }
}
