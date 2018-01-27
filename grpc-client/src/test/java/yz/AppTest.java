package yz;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import yz.grpc.client.StubFactory;
import yz.grpc.proto.service.compute.ComputeServiceGrpc;
import yz.grpc.proto.service.compute.InputMessage;
import yz.grpc.proto.service.compute.OutputMessage;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

@Slf4j
@Test
public class AppTest {

    private InputMessage inputMessage = InputMessage.newBuilder().setNumA(2).setNumB(3).build();

    public void blockCall() {
        ComputeServiceGrpc.ComputeServiceBlockingStub computeServiceBlockingStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceBlockingStub.class);//ComputeServiceGrpc.newBlockingStub(managedChannel);
        OutputMessage value = computeServiceBlockingStub.add(inputMessage);
        //对返回结果value做处理
        log.debug("blocking call --->>> add: {}", value);
    }

    public void asyncUnaryCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
        computeServiceStub.add(inputMessage, new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                //对返回结果value做处理
                log.debug("async unary call --->>> add: {}", value);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
        latch.await();
    }

    public void asyncClientStreamingCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
        StreamObserver<InputMessage> subtract = computeServiceStub.subtract(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                //对返回结果value做处理
                log.debug("async client streaming call --->>> subtract: {}", value);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
        subtract.onNext(inputMessage);
        subtract.onCompleted();
        latch.await();
    }

    public void asyncServerStreamingCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
        computeServiceStub.multiply(inputMessage, new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                //对返回结果value做处理
                log.debug("async server streaming call --->>> multiply: {}", value);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
        latch.await();
    }

    public void asyncBidiStreamingCall() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        ComputeServiceGrpc.ComputeServiceStub computeServiceStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceStub.class);//ComputeServiceGrpc.newStub(managedChannel);
        StreamObserver<InputMessage> divide = computeServiceStub.divide(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                //对返回结果value做处理
                log.debug("async server streaming call --->>> divide: {}", value);
                latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        });
        divide.onNext(inputMessage);
        divide.onCompleted();
        latch.await();
    }

    public void futureUnaryCall() throws ExecutionException, InterruptedException {
        ComputeServiceGrpc.ComputeServiceFutureStub computeServiceFutureStub = StubFactory.instance().stub(ComputeServiceGrpc.ComputeServiceFutureStub.class);// ComputeServiceGrpc.newFutureStub(managedChannel);
        ListenableFuture<OutputMessage> future = computeServiceFutureStub.add(inputMessage);
        log.debug("future unary call --->>> future: {}", future);
        OutputMessage value = future.get();
        //对返回结果value做处理
        log.debug("future unary call --->>> add: {}", value);
    }
}