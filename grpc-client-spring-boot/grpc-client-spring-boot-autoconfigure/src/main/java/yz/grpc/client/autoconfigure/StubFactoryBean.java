package yz.grpc.client.autoconfigure;

import io.grpc.Channel;
import io.grpc.stub.AbstractStub;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;

public class StubFactoryBean<T extends AbstractStub> extends AbstractFactoryBean<T> {

    private String beanClassName;
    private Class<T> beanType;
    //private Channel channel;
    private ApplicationContext applicationContext;

    public String getBeanClassName() {
        return beanClassName;
    }


    @SuppressWarnings("unchecked")
    public void setBeanClassName(String beanClassName) throws ClassNotFoundException {
        this.beanClassName = beanClassName;
        this.beanType = ((Class<T>) Class.forName(beanClassName));
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    /*public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }*/


    @Override
    public Class<T> getObjectType() {
        return beanType;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T createInstance() throws Exception {
        Constructor constructor = beanType.getDeclaredConstructor(Channel.class);
        constructor.setAccessible(true);
        T t = (T) constructor.newInstance(applicationContext.getBean(Channel.class));
        return t;
    }
}
