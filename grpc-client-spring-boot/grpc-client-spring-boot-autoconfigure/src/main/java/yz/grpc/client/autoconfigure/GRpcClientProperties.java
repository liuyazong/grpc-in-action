package yz.grpc.client.autoconfigure;

import io.grpc.ClientInterceptor;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = GRpcClientProperties.GRPC_CLIENT)
public class GRpcClientProperties {

    public static final String GRPC_CLIENT = "grpc.client";

    private String hostname = "localhost";
    private Integer port = 2018;

    private Ssl ssl;

    @NestedConfigurationProperty
    private Group worker = new Group(1, "grpc-worker-group");

    private Boolean directExecutor = Boolean.TRUE;
    private Class<? extends ClientInterceptor>[] interceptors;

    @Data
    public static class Ssl {
        private boolean enabled = false;
        private String keyCertChain;
        private String key;
        private String trustManager;
        private ClientAuth clientAuth;
        private SslProvider sslProvider;
    }

    public static class Group {
        private Integer nThreads;
        private String poolName;

        public Group() {
        }

        public Group(Integer nThreads, String poolName) {
            this.nThreads = nThreads;
            this.poolName = poolName;
        }

        public Integer getnThreads() {
            return nThreads;
        }

        public void setnThreads(Integer nThreads) {
            this.nThreads = nThreads;
        }

        public String getPoolName() {
            return poolName;
        }

        public void setPoolName(String poolName) {
            this.poolName = poolName;
        }

        @Override
        public String toString() {
            return "Group{" +
                    "nThreads=" + nThreads +
                    ", poolName='" + poolName + '\'' +
                    '}';
        }
    }
}
