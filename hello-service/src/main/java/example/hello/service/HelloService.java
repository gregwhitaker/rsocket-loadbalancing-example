package example.hello.service;

import io.rsocket.AbstractRSocket;
import io.rsocket.ConnectionSetupPayload;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.SocketAcceptor;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

/**
 * Service that generates a hello message.
 */
public class HelloService {
    private static final Logger LOG = LoggerFactory.getLogger(HelloService.class);

    public static void main(String... args) throws Exception {
        final String serviceName = serviceName(args);
        final Integer port = port(args);

        RSocketFactory.receive()
                .frameDecoder(PayloadDecoder.DEFAULT)
                .acceptor(new SocketAcceptor() {
                    @Override
                    public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket sendingSocket) {
                        return Mono.just(new AbstractRSocket() {
                            @Override
                            public Mono<Payload> requestResponse(Payload payload) {
                                String name = payload.getDataUtf8();

                                if (name == null || name.isEmpty()) {
                                    name = "You";
                                }

                                return Mono.just(DefaultPayload.create(String.format("Hello, %s! from %s", name, serviceName)));
                            }
                        });
                    }
                })
                .transport(TcpServerTransport.create(port))
                .start()
                .block();

        LOG.info("RSocket server '{}' started on port: {}", serviceName, port);

        Thread.currentThread().join();
    }

    /**
     * Gets the service name from the command line arguments.
     *
     * @param args command line arguments
     * @return name of this service
     */
    private static String serviceName(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Missing command line argument(s).");
        }

        return args[0];
    }

    /**
     * Gets the port on which to run this service from the command line arguments.
     *
     * @param args command line arguments
     * @return service port
     */
    private static Integer port(String... args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Missing command line argument(s).");
        }

        return Integer.parseInt(args[1]);
    }
}
