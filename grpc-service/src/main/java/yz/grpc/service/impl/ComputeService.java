package yz.grpc.service.impl;

import io.grpc.stub.StreamObserver;
import yz.grpc.proto.service.compute.ComputeServiceGrpc;
import yz.grpc.proto.service.compute.InputMessage;
import yz.grpc.proto.service.compute.OutputMessage;

import java.math.BigDecimal;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class ComputeService extends ComputeServiceGrpc.ComputeServiceImplBase {
    private int scale = 8;
    private int roundingMode = ROUND_HALF_UP;

    @Override
    public void add(InputMessage request, StreamObserver<OutputMessage> responseObserver) {
        responseObserver.onNext(
                OutputMessage
                        .newBuilder()
                        .setResult(
                                BigDecimal.valueOf(request.getNumA())
                                        .add(BigDecimal.valueOf(request.getNumB()))
                                        .setScale(scale, roundingMode).doubleValue()
                        ).setId(request.getId())
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<InputMessage> subtract(StreamObserver<OutputMessage> responseObserver) {
        return new StreamObserver<InputMessage>() {

            @Override
            public void onNext(InputMessage request) {
                responseObserver.onNext(OutputMessage
                        .newBuilder()
                        .setResult(
                                BigDecimal.valueOf(request.getNumA())
                                        .subtract(BigDecimal.valueOf(request.getNumB()))
                                        .setScale(scale, roundingMode).doubleValue()
                        ).setId(request.getId())
                        .build());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void multiply(InputMessage request, StreamObserver<OutputMessage> responseObserver) {
        responseObserver.onNext(
                OutputMessage
                        .newBuilder()
                        .setResult(
                                BigDecimal.valueOf(request.getNumA())
                                        .multiply(BigDecimal.valueOf(request.getNumB()))
                                        .setScale(scale, roundingMode).doubleValue()
                        ).setId(request.getId())
                        .build()
        );
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<InputMessage> divide(StreamObserver<OutputMessage> responseObserver) {
        return new StreamObserver<InputMessage>() {
            @Override
            public void onNext(InputMessage request) {
                responseObserver.onNext(
                        OutputMessage
                                .newBuilder()
                                .setResult(
                                        BigDecimal.valueOf(request.getNumA())
                                                .divide(BigDecimal.valueOf(request.getNumB()), scale, roundingMode)
                                                .doubleValue()
                                ).setId(request.getId())
                                .build()
                );
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
