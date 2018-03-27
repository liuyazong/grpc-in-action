package yz.grpc.server.autoconfigure;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

@Slf4j
@Configuration
@ConditionalOnClass(value = {NettyServerBuilder.class, Server.class})
@EnableConfigurationProperties(value = {GRpcServerProperties.class})
public class GRpcServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(value = {NettyServerBuilder.class})
    public NettyServerBuilder serverBuilder(GRpcServerProperties properties,
                                            ApplicationContext applicationContext) throws SSLException {
        log.debug("server properties: {}", properties);
        NettyServerBuilder builder = NettyServerBuilder
                .forAddress(new InetSocketAddress(properties.getHostname(), properties.getPort()))
                .channelType(NioServerSocketChannel.class)
                .bossEventLoopGroup(new NioEventLoopGroup(properties.getBoss().getnThreads(), new DefaultThreadFactory(properties.getBoss().getPoolName())))
                .workerEventLoopGroup(new NioEventLoopGroup(properties.getWorker().getnThreads(), new DefaultThreadFactory(properties.getWorker().getPoolName())));

        GRpcServerProperties.Ssl ssl = properties.getSsl();
        if (null != ssl && ssl.isEnabled()) {
            SslContext sslContext = GrpcSslContexts
                    .configure(
                            SslContextBuilder
                                    .forServer(
                                            GRpcServerAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getKeyCertChain()),
                                            GRpcServerAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getKey())
                                    )
                                    .trustManager(GRpcServerAutoConfiguration.class.getClassLoader().getResourceAsStream(ssl.getTrustManager()))
                                    .clientAuth(ssl.getClientAuth())
                                    .sslProvider(ssl.getSslProvider()))
                    .build();
            builder.sslContext(sslContext);
        } else {

        }

        if (properties.getDirectExecutor()) {
            builder = builder.directExecutor();
        }

        Class<? extends ServerInterceptor>[] interceptors = properties.getInterceptors();
        if (null != interceptors && interceptors.length > 0) {
            for (Class<? extends ServerInterceptor> interceptor : interceptors)
                try {
                    Constructor<? extends ServerInterceptor> constructor = interceptor.getDeclaredConstructor();
                    ServerInterceptor serverInterceptor = constructor.newInstance();
                    builder = builder.intercept(serverInterceptor);
                    log.debug("add interceptor: {}", serverInterceptor);
                } catch (Exception e) {
                    log.error("error",e);
                }
        }
        String[] beanNamesForType = applicationContext.getBeanNamesForType(BindableService.class);
        for (int i = 0; i < beanNamesForType.length; i++) {
            String beanName = beanNamesForType[i];
            BindableService bindableService = (BindableService) applicationContext.getBean(beanName);
            builder = builder.addService(bindableService);
            log.debug("bind service: {}", bindableService);
        }
        builder = builder.withChildOption(ChannelOption.TCP_NODELAY, true);
        return builder;

    }

    @Bean
    @ConditionalOnMissingBean(value = {Server.class})
    public Server server(ServerBuilder builder) throws IOException {
        Server start = builder.build().start();
        log.info("server started at {}", start.getPort());
        return start;
    }

}
