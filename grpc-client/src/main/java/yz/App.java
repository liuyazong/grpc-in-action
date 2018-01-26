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

        {
            ComputeServiceGrpc.ComputeServiceBlockingStub computeServiceBlockingStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceBlockingStub.class);//ComputeServiceGrpc.newBlockingStub(managedChannel);
            OutputMessage value = computeServiceBlockingStub.add(inputMessage);//对返回结果value做处理
        }

        {
            ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
            computeServiceStub.add(inputMessage, new StreamObserver<OutputMessage>() {
                @Override
                public void onNext(OutputMessage value) {
                    //对返回结果value做处理
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onCompleted() {
                }
            });


            StreamObserver<InputMessage> subtract_error_ = computeServiceStub.subtract(new StreamObserver<OutputMessage>() {
                @Override
                public void onNext(OutputMessage value) {
                    //对返回结果value做处理
                }

                @Override
                public void onError(Throwable t) {
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
                    //对返回结果value做处理
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onCompleted() {
                }
            });


            StreamObserver<InputMessage> divide_error_ = computeServiceStub.divide(new StreamObserver<OutputMessage>() {
                @Override
                public void onNext(OutputMessage value) {
                    //对返回结果value做处理
                }

                @Override
                public void onError(Throwable t) {
                }

                @Override
                public void onCompleted() {
                }
            });
            divide_error_.onNext(inputMessage);
            divide_error_.onCompleted();
        }

        {
            ComputeServiceGrpc.ComputeServiceFutureStub computeServiceFutureStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceFutureStub.class);// ComputeServiceGrpc.newFutureStub(managedChannel);
            OutputMessage value = computeServiceFutureStub.add(inputMessage).get();//对返回结果value做处理
        }

        Thread.currentThread().join();
    }
}
