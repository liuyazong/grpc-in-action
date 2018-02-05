package yz.grpc.server.autoconfigure;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptor;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                            ApplicationContext applicationContext) {
        log.debug("server properties: {}", properties);
        NettyServerBuilder builder = NettyServerBuilder
                .forAddress(new InetSocketAddress(properties.getHostname(), properties.getPort()))
                .bossEventLoopGroup(new NioEventLoopGroup(properties.getBoss().getnThreads(), new DefaultThreadFactory(properties.getBoss().getPoolName())))
                .workerEventLoopGroup(new NioEventLoopGroup(properties.getWorker().getnThreads(), new DefaultThreadFactory(properties.getWorker().getPoolName())));

        if (properties.getDirectExecutor()) {
            builder = builder.directExecutor();
        }

        String[] interceptors = properties.getInterceptors();
        if (null != interceptors && interceptors.length > 0) {
            for (String interceptor : interceptors)
                try {
                    @SuppressWarnings("unchecked")
                    Class<? extends ServerInterceptor> aClass = (Class<? extends ServerInterceptor>) Class.forName(interceptor);
                    Constructor<? extends ServerInterceptor> constructor = aClass.getDeclaredConstructor();
                    ServerInterceptor serverInterceptor = constructor.newInstance();
                    builder = builder.intercept(serverInterceptor);
                    log.debug("add interceptor: {}", serverInterceptor);
                } catch (Exception e) {
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
