package yz.grpc.client.autoconfigure;

import io.grpc.Channel;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

@Slf4j
@Configuration
@ConditionalOnClass(value = {NettyChannelBuilder.class, ManagedChannel.class})
@EnableConfigurationProperties(value = {GRpcClientProperties.class})
public class GRpcClientAutoConfiguration {

    @Bean
    public Channel channel(GRpcClientProperties properties) throws SSLException {
        log.debug("client properties: {}", properties);
        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress(new InetSocketAddress(properties.getHostname(), properties.getPort()))
                .channelType(NioSocketChannel.class)
                .eventLoopGroup(new NioEventLoopGroup(properties.getWorker().getnThreads(), new DefaultThreadFactory(properties.getWorker().getPoolName())))
                .withOption(ChannelOption.SO_KEEPALIVE, true)
                .withOption(ChannelOption.TCP_NODELAY, true);

        GRpcClientProperties.Ssl ssl = properties.getSsl();
        if (null != ssl && ssl.isEnabled()) {
            SslContext sslContext = GrpcSslContexts
                    .configure(
                            SslContextBuilder
                                    .forClient()
                                    .sslProvider(ssl.getSslProvider())
                                    .clientAuth(ssl.getClientAuth())
                                    .trustManager(GRpcClientAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getTrustManager()))
                                    .keyManager(GRpcClientAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getKeyCertChain()),
                                            GRpcClientAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getKey()))
                    ).build();
            builder.sslContext(sslContext);
        } else {
            builder.negotiationType(NegotiationType.PLAINTEXT);
        }

        if (properties.getDirectExecutor()) {
            builder = builder.directExecutor();
        }

        Class<? extends ClientInterceptor>[] interceptors = properties.getInterceptors();
        if (null != interceptors && interceptors.length > 0) {
            for (Class<? extends ClientInterceptor> interceptor : interceptors)
                try {
                    Constructor<? extends ClientInterceptor> constructor = interceptor.getDeclaredConstructor();
                    ClientInterceptor clientInterceptor = constructor.newInstance();
                    builder = builder.intercept(clientInterceptor);
                    log.debug("add interceptor: {}", clientInterceptor);
                } catch (Exception e) {
                    log.error("error", e);
                }
        }
        ManagedChannel managedChannel = builder.build();
        return managedChannel;
    }

}
