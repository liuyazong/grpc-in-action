package yz.grpc.client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.AbstractStub;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class StubFactory {
    private static final Map<Class<? extends AbstractStub>, AbstractStub<? extends AbstractStub>> STUB_CONTAINER = new HashMap<>();

    private static final ManagedChannel MANAGED_CHANNEL = NettyChannelBuilder
            .forAddress(new InetSocketAddress("127.0.0.1", 2018))
            .channelType(NioSocketChannel.class)
            .withOption(ChannelOption.SO_KEEPALIVE, true)
            .withOption(ChannelOption.TCP_NODELAY, true)
            .negotiationType(NegotiationType.PLAINTEXT)
            .intercept(new LoggingInterceptor())
            .build();

    @SuppressWarnings("unchecked")
    public static <T extends AbstractStub<T>> T stub(Class<T> tClass) {
        return (T) STUB_CONTAINER.computeIfAbsent(tClass, aClass -> {
            try {
                Constructor<T> constructor = tClass.getDeclaredConstructor(Channel.class);
                constructor.setAccessible(true);
                T t = constructor.newInstance(MANAGED_CHANNEL);
                return t;
            } catch (Exception e) {
            }
            return null;
        });
    }
}
