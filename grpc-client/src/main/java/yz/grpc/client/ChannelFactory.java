package yz.grpc.client;

import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
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
                .channelType(NioSocketChannel.class)
                .eventLoopGroup(new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() << 1))
                .withOption(ChannelOption.SO_KEEPALIVE, true)
                .withOption(ChannelOption.TCP_NODELAY, true)
                .negotiationType(NegotiationType.PLAINTEXT)
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
