package yz;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
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


    @Before
    public void contextLoads() {

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
    public void clientStream() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<InputMessage> subtract = computeServiceStub.subtract(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.debug("client stream: \n{}", value);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                log.error("onError ", t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.debug("onCompleted");
                countDownLatch.countDown();
            }
        });
        subtract.onNext(InputMessage
                .newBuilder()
                .setNumA(new Random().nextInt(100))
                .setNumB(new Random().nextInt(100))
                .setId(UUID.randomUUID().toString())
                .build());
        subtract.onCompleted();
        countDownLatch.await();
    }

    @Test
    public void serverStream() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        computeServiceStub.multiply(InputMessage.
                        newBuilder()
                        .setNumA(new Random().nextInt(100))
                        .setNumB(new Random().nextInt(100))
                        .setId(UUID.randomUUID().toString())
                        .build(),
                new StreamObserver<OutputMessage>() {
                    @Override
                    public void onNext(OutputMessage value) {
                        log.debug("server stream: \n{}", value);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("onError ", t);
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.debug("onCompleted");
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();
    }

    @Test
    public void bidiStream() throws InterruptedException {
        int count = 20;
        CountDownLatch countDownLatch = new CountDownLatch(count);

        StreamObserver<InputMessage> divide = computeServiceStub.divide(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.debug("bidi stream: \n{}", value);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                log.error("onError ", t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.debug("onCompleted");
                countDownLatch.countDown();
            }
        });
        for (int i = 0; i < count; i++) {
            divide.onNext(InputMessage.newBuilder().setNumA(new Random().nextInt(100)).setNumB(new Random().nextInt(100)).setId(UUID.randomUUID().toString()).build());
        }
        divide.onCompleted();
        countDownLatch.await();
    }

    @Test
    public void ex() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        StreamObserver<InputMessage> divide = computeServiceStub.divide(new StreamObserver<OutputMessage>() {
            @Override
            public void onNext(OutputMessage value) {
                log.debug("bidi stream: \n{}", value);
                countDownLatch.countDown();
            }

            @Override
            public void onError(Throwable t) {
                log.error("onError ", t);
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.debug("onCompleted");
                countDownLatch.countDown();
            }
        });
        divide.onNext(InputMessage.newBuilder().setNumA(1).setNumB(0).setId(UUID.randomUUID().toString()).build());
        divide.onCompleted();
        countDownLatch.await();
    }

    @Test
    public void futureStream() throws ExecutionException, InterruptedException {
        OutputMessage outputMessage = computeServiceFutureStub.add(InputMessage.newBuilder().setNumA(new Random().nextInt(100)).setNumB(new Random().nextInt(100)).setId(UUID.randomUUID().toString()).build()).get();
        log.debug("future stream: \n{}", outputMessage);
    }
}
