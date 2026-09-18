package org.example.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BilingServiceGRPCClient {
    private final BillingServiceGrpc.BillingServiceBlockingStub blockStub;

    public BilingServiceGRPCClient(
            @Value("${billing.service.address:localhost}") String serverAddress,
            @org.springframework.beans.factory.annotation.Value("${billing.service.grpc.port:9001}") int ServerPort
    ){
        log.info("Connecting to BillingService GRPC service at {}:{}",serverAddress,ServerPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,ServerPort).usePlaintext().build();

        blockStub = BillingServiceGrpc.newBlockingStub(channel);
    }

    public BillingResponse createBillingAccount(String patientId,String name, String email){
        BillingRequest billReq = BillingRequest.newBuilder().setPatientId(patientId)
                .setName(name).setEmail(email).build();
        BillingResponse billRes = blockStub .createBillingAccount(billReq);
        log.info("Received response from billing service via GRPC: {}",billRes);
        return billRes;
    }
}
