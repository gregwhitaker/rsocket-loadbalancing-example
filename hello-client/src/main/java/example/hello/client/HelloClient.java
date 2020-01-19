package example.hello.client;

import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.client.LoadBalancedRSocketMono;
import io.rsocket.client.filter.RSocketSupplier;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Client that load balances requests for hello messages across multiple instances
 * of the hello-service.
 */
public class HelloClient {
    private static final Logger LOG = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String... args) throws Exception {
        final String name = recipientName(args);
        final Collection<ServiceDefinition> services = serviceDefinitions(args);

        // Populate RSocketSuppliers with the services to load balance across
        Collection<RSocketSupplier> rSocketSuppliers = new HashSet<>();
        services.forEach(serviceDefinition -> {
            rSocketSuppliers.add(new RSocketSupplier(() -> {
                return RSocketFactory.connect()
                        .transport(TcpClientTransport.create(serviceDefinition.host, serviceDefinition.port))
                        .start()
                        .doOnSubscribe(subscription -> LOG.info("Connected to hello-service at '{}:{}'", serviceDefinition.host, serviceDefinition.port));
            }));
        });

        // Create a load balancer
        LoadBalancedRSocketMono loadBalancer = LoadBalancedRSocketMono
                .create(Flux.just(rSocketSuppliers));

        CountDownLatch latch = new CountDownLatch(1000);

        // Sending 10 requests
        Flux.range(1, 1000)
                .delayElements(Duration.ofSeconds(1))
                .subscribe(cnt -> {
                    loadBalancer.flatMap(rSocket -> {
                        LOG.info("Sending Request: {}", cnt);
                        return rSocket.requestResponse(DefaultPayload.create(name));
                    })
                    .retryBackoff(3, Duration.ofSeconds(10))
                    .subscribe(payload -> {
                        LOG.info("Response: {}", payload.getDataUtf8());
                        latch.countDown();
                    });
                });

        latch.await();
    }

    /**
     * Gets the hello message recipient's name from the command line arguments.
     *
     * @param args command line argument
     * @return recipient name
     */
    public static String recipientName(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Command line arguments should be of length 2.");
        }

        return args[0];
    }

    /**
     * Gets the services to add to the load balancer from the command line arguments.
     *
     * Services are specified in the format: {hostname}:{port},{hostname}:{port}, etc.
     *
     * @param args command line arguments
     * @return collection of {@link ServiceDefinition}s to add to load balancer
     */
    public static Collection<ServiceDefinition> serviceDefinitions(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Command line arguments should be of length 2.");
        }

        Set<ServiceDefinition> serviceDefinitions = new HashSet<>();
        Arrays.asList(args[1].split(",")).forEach(s -> {
            String[] hostPortPair = s.trim().split(":");

            if (hostPortPair.length != 2) {
                throw new IllegalArgumentException("Invalid host/port pair specified: " + s);
            }

            serviceDefinitions.add(new ServiceDefinition(hostPortPair[0], Integer.parseInt(hostPortPair[1])));
        });

        return serviceDefinitions;
    }

    /**
     * Holds the host and port information for services to add to the load balancer.
     */
    static class ServiceDefinition {
        private final String host;
        private final int port;

        public ServiceDefinition(final String host, final int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
