package yz.grpc.server.autoconfigure;

import io.grpc.BindableService;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.lang.reflect.Constructor;

public class ServiceFactoryBean<T extends BindableService> extends AbstractFactoryBean<T> {

    private String beanClassName;
    private Class<T> beanType;


    public String getBeanClassName() {
        return beanClassName;
    }


    @SuppressWarnings("unchecked")
    public void setBeanClassName(String beanClassName) throws ClassNotFoundException {
        this.beanClassName = beanClassName;
        this.beanType = (Class<T>) Class.forName(beanClassName);
    }

    @Override
    public Class<T> getObjectType() {
        return beanType;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T createInstance() throws Exception {
        Constructor constructor = beanType.getDeclaredConstructor();
        constructor.setAccessible(true);
        T t = (T) constructor.newInstance();
        return t;
    }
}
