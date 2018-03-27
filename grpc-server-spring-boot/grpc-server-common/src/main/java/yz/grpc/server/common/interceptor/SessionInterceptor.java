package yz.grpc.server.common.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        log.debug("onHeaders: {}", headers);
        String sessionId = headers.get(Metadata.Key.of("session-id", Metadata.ASCII_STRING_MARSHALLER));
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void sendHeaders(Metadata headers) {
                headers.put(Metadata.Key.of("session-id", Metadata.ASCII_STRING_MARSHALLER), sessionId);
                log.debug("sendHeaders: {}", headers);
                super.sendHeaders(headers);
            }
        }, headers)) {

        };

        /*
        if (null == sessionId) {
            call.close(Status.UNAUTHENTICATED, headers);
            return next.startCall(call,headers);
        }else {
            Attributes attributes = call.getAttributes();
            Set<Attributes.Key<?>> keys = attributes.keys();
            InetSocketAddress address = null;
            for (Attributes.Key<?> key : keys) {
                if (key.toString().equals("remote-addr")) {
                    address = attributes.get((Attributes.Key<InetSocketAddress>) key);
                    break;
                }
            }
            log.debug("session:{},remote-addr:{}:{}", sessionId, address.getHostString(), address.getPort());
            return next.startCall(call, headers);
        }*/
    }
}
