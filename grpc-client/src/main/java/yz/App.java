package yz;


import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import yz.grpc.client.StubFactory;
import yz.grpc.proto.service.compute.ComputeServiceGrpc;
import yz.grpc.proto.service.compute.InputMessage;
import yz.grpc.proto.service.compute.OutputMessage;


@Slf4j
public class App {
    public static void main(String[] args) throws Exception {
        InputMessage inputMessage = InputMessage.newBuilder().setNumA(2).setNumB(3).build();

        ComputeServiceGrpc.ComputeServiceBlockingStub computeServiceBlockingStub = StubFactory.stub(ComputeServiceGrpc.ComputeServiceBlockingStub.class);//ComputeServiceGrpc.newBlockingStub(managedChannel);
        OutputMessage add = computeServiceBlockingStub.add(inputMessage);
        log.info("blocking add: {}", add);

        //
        ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
        computeServiceStub.add(inputMessage, new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.info("add: {}", value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("add error ", t);
            }

            @Override
            public void onCompleted() {
            }
        });

        StreamObserver<InputMessage> subtract_error_ = computeServiceStub.subtract(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.info("subtract: {}", value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("subtract error ", t);
            }

            @Override
            public void onCompleted() {
            }
        });
        subtract_error_.onNext(inputMessage);
        subtract_error_.onCompleted();


        computeServiceStub.multiply(inputMessage, new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.info("multiply: {}", value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("multiply error ", t);
            }

            @Override
            public void onCompleted() {
            }
        });

        StreamObserver<InputMessage> divide_error_ = computeServiceStub.divide(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.info("divide: {}", value);
            }

            @Override
            public void onError(Throwable t) {
                log.error("divide error ", t);
            }

            @Override
            public void onCompleted() {
            }
        });
        divide_error_.onNext(inputMessage);
        divide_error_.onCompleted();


        ComputeServiceGrpc.ComputeServiceFutureStub computeServiceFutureStub = StubFactory.stub(ComputeServiceGrpc.ComputeServiceFutureStub.class);// ComputeServiceGrpc.newFutureStub(managedChannel);
        OutputMessage outputMessage = computeServiceFutureStub.add(inputMessage).get();
        log.info("future add: {}", outputMessage);

    }
}
