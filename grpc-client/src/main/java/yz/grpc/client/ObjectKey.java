package yz.grpc.client;

import io.grpc.stub.AbstractStub;

import java.net.SocketAddress;

public class ObjectKey {

    private SocketAddress address;
    private Class<? extends AbstractStub<?>> stubClass;

    public ObjectKey(SocketAddress address, Class<? extends AbstractStub<?>> stubClass) {
        this.address = address;
        this.stubClass = stubClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ObjectKey that = (ObjectKey) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
                .append(address, that.address)
                .append(stubClass, that.stubClass)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
                .append(address)
                .append(stubClass)
                .toHashCode();
    }

    public SocketAddress getAddress() {
        return address;
    }

    public Class<? extends AbstractStub> getStubClass() {
        return stubClass;
    }

    @Override
    public String toString() {
        return "ObjectKey{" +
                "address=" + address +
                ", stubClass=" + stubClass +
                '}';
    }
}
