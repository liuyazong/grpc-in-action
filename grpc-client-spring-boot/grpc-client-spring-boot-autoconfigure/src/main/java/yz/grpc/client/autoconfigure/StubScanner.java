package yz.grpc.client.autoconfigure;

import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Set;

public class StubScanner extends ClassPathBeanDefinitionScanner {

    private ApplicationContext applicationContext;

    public StubScanner(BeanDefinitionRegistry registry,
                       ApplicationContext applicationContext) {
        super(registry, false);
        this.applicationContext = applicationContext;
        this.addIncludeFilter(new AssignableTypeFilter(AbstractStub.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        this.addProperties(beanDefinitions);
        return beanDefinitions;
    }

    private void addProperties(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
            definition.getPropertyValues().add("beanClassName", definition.getBeanClassName());
            //definition.getPropertyValues().add("channel", applicationContext.getBean(Channel.class));
            definition.getPropertyValues().add("applicationContext", applicationContext);
            definition.setBeanClass(StubFactoryBean.class);
        }
    }

}
