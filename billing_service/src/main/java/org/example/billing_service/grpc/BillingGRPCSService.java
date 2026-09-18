package org.example.billing_service.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGRPCSService extends BillingServiceGrpc.BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGRPCSService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest billReq, StreamObserver<billing.BillingResponse> resObserver){
        log.info("createBillingAccount request received {}", billReq.toString());

        //Business Logic

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();

        resObserver.onNext(response);
        resObserver.onCompleted();
    }
}
