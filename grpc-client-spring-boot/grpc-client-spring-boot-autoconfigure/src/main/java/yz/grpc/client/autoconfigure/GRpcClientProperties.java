package yz.grpc.client.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Data
@ConfigurationProperties(prefix = GRpcClientProperties.GRPC_CLIENT)
public class GRpcClientProperties {

    public static final String GRPC_CLIENT = "grpc.client";

    private String hostname = "localhost";
    private Integer port = 2018;

    @NestedConfigurationProperty
    private Group worker = new Group(1, "grpc-worker-group");

    private Boolean directExecutor = Boolean.TRUE;
    private String[] interceptors;

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
