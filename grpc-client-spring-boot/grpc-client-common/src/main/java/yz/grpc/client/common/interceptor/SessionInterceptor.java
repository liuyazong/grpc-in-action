package yz.grpc.client.common.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class SessionInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(Metadata.Key.of("session-id", Metadata.ASCII_STRING_MARSHALLER), UUID.randomUUID().toString());
                log.debug("sendHeaders: {}", headers);
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                    @Override
                    public void onHeaders(Metadata headers) {
                        log.debug("onHeaders: {}", headers);
                        super.onHeaders(headers);
                    }
                }, headers);
            }

        };
    }
}
