package yz.grpc.server.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

@Slf4j
public class ServiceBeanDefinitionRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware
        /*,BeanFactoryPostProcessor, ApplicationContextAware */ {

    private BeanFactory beanFactory;
    private ResourceLoader resourceLoader;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ServiceScanner scanner = new ServiceScanner(registry);
        scanner.setResourceLoader(resourceLoader);

        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        String[] pkgs = new String[packages.size()];
        int i = 0;
        for (String pkg : packages) {
            log.debug("using auto-configuration base package '{}'", pkg);
            pkgs[i] = pkg;
            i++;
        }
        scanner.doScan(pkgs);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
