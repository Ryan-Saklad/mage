package mage.server.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import mage.server.managers.ManagerFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Manages the gRPC server lifecycle.
 */
public class GrpcServerManager {

    private static final Logger logger = Logger.getLogger(GrpcServerManager.class);

    private final int port;
    private final MageServiceGrpcImpl serviceImpl;
    private Server server;

    public GrpcServerManager(int port, ManagerFactory managerFactory, String adminPassword, boolean testMode, boolean detailsMode) {
        this.port = port;
        this.serviceImpl = new MageServiceGrpcImpl(managerFactory, adminPassword, testMode, detailsMode);
    }

    public void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(serviceImpl)
                .build()
                .start();

        logger.info("gRPC server started on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down gRPC server...");
            try {
                GrpcServerManager.this.stop();
            } catch (InterruptedException e) {
                logger.error("Error during gRPC server shutdown", e);
            }
            logger.info("gRPC server shut down.");
        }));
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public MageServiceGrpcImpl getServiceImpl() {
        return serviceImpl;
    }
}
