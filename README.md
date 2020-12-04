# rsocket-loadbalancing-example
![Build](https://github.com/gregwhitaker/rsocket-loadbalancing-example/workflows/Build/badge.svg)

An example of load-balancing requests across multiple [RSocket](http://rsocket.io) services.

In this example the `hello-client` load balances requests for `10` hello messages across two instances of the `hello-service`.

## Building the Example
Run the following command to build the example:

    ./gradlew clean build

## Running the Example:
Follow the steps below to run the example:

1. Run the following command to start an instance of the `hello-service` on port `7000`:

        ./gradlew :hello-service:run --args="hello-service1 7000"
        
   If successful, you will see the following in the terminal:
   
       > Task :hello-service:run
       [main] INFO example.hello.service.HelloService - RSocket server 'hello-service1' started on port: 7000

2. In a new terminal, run the following command to start a second instance of the `hello-service` on port `7001`:

        ./gradlew :hello-service:run --args="hello-service2 7001"

   If successful, you will see the following in the terminal:
   
        > Task :hello-service:run
        [main] INFO example.hello.service.HelloService - RSocket server 'hello-service2' started on port: 7001
        
3. Next, in a new terminal run the following command to start the `hello-client` and request `10` hello messages for `Bob` load balanced across the two service instances:

       ./gradlew :hello-client:run --args="Bob localhost:7000,localhost:7001"
       
   If successful, you will see that the requests were load balanced across both hello-service instances in the terminal:
   
       > Task :hello-client:run
       [main] INFO example.hello.client.HelloClient - Connected to hello-service at 'localhost:7000'
       [main] INFO example.hello.client.HelloClient - Connected to hello-service at 'localhost:7001'
       [parallel-3] INFO example.hello.client.HelloClient - Sending Request 1
       [reactor-tcp-nio-1] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service1
       [parallel-4] INFO example.hello.client.HelloClient - Sending Request 2
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service2
       [parallel-5] INFO example.hello.client.HelloClient - Sending Request 3
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service2
       [parallel-6] INFO example.hello.client.HelloClient - Sending Request 4
       [reactor-tcp-nio-1] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service1
       [parallel-7] INFO example.hello.client.HelloClient - Sending Request 5
       [reactor-tcp-nio-1] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service1
       [parallel-8] INFO example.hello.client.HelloClient - Sending Request 6
       [reactor-tcp-nio-1] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service1
       [parallel-1] INFO example.hello.client.HelloClient - Sending Request 7
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service2
       [parallel-2] INFO example.hello.client.HelloClient - Sending Request 8
       [reactor-tcp-nio-1] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service1
       [parallel-3] INFO example.hello.client.HelloClient - Sending Request 9
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service2
       [parallel-4] INFO example.hello.client.HelloClient - Sending Request 10
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Response: Hello, Bob! from hello-service2
       [reactor-tcp-nio-2] INFO example.hello.client.HelloClient - Done
   
   Notice that the requests are not evenly distributed amongst the service instances. This is because the load balancer uses intelligent
   routing to direct traffic to the least latent instance.
   
## License
MIT License

Copyright (c) 2020 Greg Whitaker

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
