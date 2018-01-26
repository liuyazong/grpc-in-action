package yz.grpc.client;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.stub.AbstractStub;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class StubFactory {

    private static StubFactory factory = new StubFactory();

    static {
        factory.stubContainer = new ConcurrentHashMap<>();
    }

    private GenericKeyedObjectPool<ObjectKey, ManagedChannel> channelPool;
    private ConcurrentHashMap<ObjectKey, AbstractStub<? extends AbstractStub>> stubContainer;

    private StubFactory() {
        GenericKeyedObjectPoolConfig poolConfig = new GenericKeyedObjectPoolConfig();
        poolConfig.setMinIdlePerKey(3);
        poolConfig.setMaxIdlePerKey(3);
        poolConfig.setMaxTotalPerKey(8);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        this.channelPool = new GenericKeyedObjectPool<>(new ChannelFactory(), poolConfig);
    }

    public static StubFactory instance() {
        return factory;
    }

    public <T extends AbstractStub<T>> T stub(Class<T> tClass) {
        ObjectKey objectKey = new ObjectKey(discoveryAddress(), tClass);
        AbstractStub<? extends AbstractStub> abstractStub = stubContainer.computeIfAbsent(objectKey, key0 -> {
            try {
                @SuppressWarnings("unchecked")
                Constructor<T> constructor = (Constructor<T>) key0.getStubClass().getDeclaredConstructor(Channel.class);
                constructor.setAccessible(true);
                T t = constructor.newInstance(this.channelPool.borrowObject(key0));
                log.debug("create stub: {}", t);
                return t;
            } catch (Exception e) {
                log.error(String.format("create stub error: %s", key0), e);
            }
            return null;
        });

        @SuppressWarnings("unchecked")
        T t = (T) abstractStub;
        //TODO 如果 ((ManagedChannel)t.getChannel()).isShutdown() || ((ManagedChannel)t.getChannel()).isTerminated() ??
        return t;
    }

    private InetSocketAddress discoveryAddress() {
        return new InetSocketAddress("127.0.0.1", 2018);
    }
}
