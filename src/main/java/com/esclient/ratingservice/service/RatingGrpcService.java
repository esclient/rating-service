package com.esclient.ratingservice.service;

import com.esclient.ratingservice.mapper.RatingMapper;
import com.esclient.ratingservice.model.Rating;
import com.esclient.ratingservice.repository.RatingRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
@RequiredArgsConstructor
public class RatingGrpcService extends com.esclient.ratingservice.proto.RatingServiceGrpc.RatingServiceImplBase {

    private final RatingRepository repository;
    private final RatingMapper mapper;

    @Override
    public void addRate(com.esclient.ratingservice.proto.RateModRequest request, StreamObserver<com.esclient.ratingservice.proto.RateModResponse> responseObserver) {
        Rating newRate = mapper.toRating(request);
        Rating savedRate = repository.save(newRate);

        com.esclient.ratingservice.proto.RateModResponse response = mapper.toResponse(savedRate);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
