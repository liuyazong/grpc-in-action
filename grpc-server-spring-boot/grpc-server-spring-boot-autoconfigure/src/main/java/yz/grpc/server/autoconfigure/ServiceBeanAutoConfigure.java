package yz.grpc.server.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {ServiceBeanDefinitionRegistrar.class})
public class ServiceBeanAutoConfigure {

}
