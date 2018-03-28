package yz;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import yz.grpc.proto.service.compute.ComputeServiceGrpc;
import yz.grpc.proto.service.compute.InputMessage;
import yz.grpc.proto.service.compute.OutputMessage;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ClientDemoApp.class)
@Slf4j
public class ClientDemoAppTest {
    @Autowired
    private ComputeServiceGrpc.ComputeServiceStub computeServiceStub;
    @Autowired
    private ComputeServiceGrpc.ComputeServiceBlockingStub computeServiceBlockingStub;
    @Autowired
    private ComputeServiceGrpc.ComputeServiceFutureStub computeServiceFutureStub;


    @Test
    public void contextLoads() throws InterruptedException {

    }


    @Test
    public void simple() {
        OutputMessage outputMessage = computeServiceBlockingStub.add(InputMessage
                .newBuilder()
                .setNumA(new Random().nextInt(100))
                .setNumB(new Random().nextInt(100))
                .setId(UUID.randomUUID().toString())
                .build());

        log.debug("simple: \n{}", outputMessage);

        outputMessage = computeServiceBlockingStub.add(InputMessage
                .newBuilder()
                .setNumA(new Random().nextInt(111))
                .setNumB(new Random().nextInt(111))
                .setId(UUID.randomUUID().toString())
                .build());

        log.debug("simple: \n{}", outputMessage);
    }

    @Test
    public void clientStream() throws InterruptedException, ExecutionException {
        mStreamObserver<OutputMessage> streamObserver = new mStreamObserver<>();
        StreamObserver<InputMessage> subtract = computeServiceStub.subtract(streamObserver);
        subtract.onNext(InputMessage
                .newBuilder()
                .setNumA(new Random().nextInt(100))
                .setNumB(new Random().nextInt(100))
                .setId(UUID.randomUUID().toString())
                .build());
        subtract.onCompleted();
        OutputMessage outputMessage = streamObserver.get();
        log.debug("client stream: \n{}", outputMessage);
    }

    @Test
    public void serverStream() throws InterruptedException {
        mStreamObserver<OutputMessage> responseObserver = new mStreamObserver<>();
        computeServiceStub.multiply(InputMessage.
                        newBuilder()
                        .setNumA(new Random().nextInt(100))
                        .setNumB(new Random().nextInt(100))
                        .setId(UUID.randomUUID().toString())
                        .build(),
                responseObserver);
        OutputMessage outputMessage = responseObserver.get();
        log.debug("server stream: \n{}", outputMessage);
    }

    @Test
    public void bidiStream() throws InterruptedException {
        mStreamObserver<OutputMessage> responseObserver = new mStreamObserver<>();
        StreamObserver<InputMessage> divide = computeServiceStub.divide(responseObserver);

        divide.onNext(InputMessage.newBuilder().setNumA(new Random().nextInt(100)).setNumB(new Random().nextInt(100)).setId(UUID.randomUUID().toString()).build());
        divide.onNext(InputMessage.newBuilder().setNumA(new Random().nextInt(100)).setNumB(new Random().nextInt(100)).setId(UUID.randomUUID().toString()).build());
        divide.onCompleted();
        OutputMessage outputMessage = responseObserver.get();
        log.debug("bidi stream: \n{}", outputMessage);
        outputMessage = responseObserver.get();
        log.debug("bidi stream: \n{}", outputMessage);
    }

    @Test
    public void futureStream() throws ExecutionException, InterruptedException {
        OutputMessage outputMessage = computeServiceFutureStub.add(InputMessage.newBuilder().setNumA(new Random().nextInt(100)).setNumB(new Random().nextInt(100)).setId(UUID.randomUUID().toString()).build()).get();
        log.debug("future stream: \n{}", outputMessage);
    }
}

class mStreamObserver<V> implements StreamObserver<V>, Future<V> {

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private V v;

    @Override
    public void onNext(V value) {
        this.v = value;
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onCompleted() {
        countDownLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.v != null;
    }

    @Override
    public V get() throws InterruptedException {
        countDownLatch.await();
        return this.v;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException {
        countDownLatch.await(timeout, unit);
        return this.v;
    }
}