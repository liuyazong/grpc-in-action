package yz.grpc.client.autoconfigure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {StubBeanDefinitionRegistrar.class})
public class StubBeanAutoConfiguration {

}
