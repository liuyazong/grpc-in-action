package yz.grpc.server.autoconfigure;

import io.grpc.BindableService;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Set;

public class ServiceScanner extends ClassPathBeanDefinitionScanner {

    public ServiceScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
        this.addIncludeFilter(new AssignableTypeFilter(BindableService.class));
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
            definition.setBeanClass(ServiceFactoryBean.class);
        }
    }

}
