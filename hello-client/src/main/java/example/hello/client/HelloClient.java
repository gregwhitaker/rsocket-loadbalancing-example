package example.hello.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Client that load balances requests for hello messages across multiple instances
 * of the hello-service.
 */
public class HelloClient {
    private static final Logger LOG = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String... args) {
        final String name = recipientName(args);
        final Collection<ServiceDefinition> services = serviceDefinitions(args);

        
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
     *
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
