package yz.grpc.client.autoconfigure;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

@Slf4j
@Configuration
@ConditionalOnClass(value = {NettyChannelBuilder.class, ManagedChannel.class})
@EnableConfigurationProperties(value = {GRpcClientProperties.class})
public class GRpcClientAutoConfiguration {

    @Bean
    public Channel channel(GRpcClientProperties properties) {
        log.debug("client properties: {}", properties);
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(new InetSocketAddress(properties.getHostname(), properties.getPort()))
                .eventLoopGroup(new NioEventLoopGroup(properties.getWorker().getnThreads(), new DefaultThreadFactory(properties.getWorker().getPoolName())))
                .withOption(ChannelOption.SO_KEEPALIVE, true)
                .withOption(ChannelOption.TCP_NODELAY, true)
                .negotiationType(NegotiationType.PLAINTEXT);

        if (properties.getDirectExecutor()) {
            builder = builder.directExecutor();
        }

        String[] interceptors = properties.getInterceptors();
        if (null != interceptors && interceptors.length > 0) {
            for (String interceptor : interceptors)
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ClientInterceptor> aClass = (Class<? extends ClientInterceptor>) Class.forName(interceptor);
                    Constructor<? extends ClientInterceptor> constructor = aClass.getDeclaredConstructor();
                    ClientInterceptor clientInterceptor = constructor.newInstance();
                    builder = builder.intercept(clientInterceptor);
                    log.debug("add interceptor: {}", clientInterceptor);
                } catch (Exception e) {
                }
        }
        ManagedChannel managedChannel = builder.build();
        return managedChannel;
    }

}
