package org.apache.camel.component.zeromq;
import java.util.concurrent.TimeUnit;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQ.Context;

public class ZeromqConsumerTest extends ZeromqBaseTest {

    @Test
    public void testConsume() throws Exception {
    	Context context = (new ZeromqContextFactory()).createContext(1);
		Socket publisher = context.socket(ZMQ.PUB);
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(numberOfMessages);
        publisher.bind("tcp://*:5555");
        for (int i = 0; i < numberOfMessages; i++) {
            String payload = "Message " + i;
            publisher.send(TEST_TOPIC.getBytes(),ZMQ.SNDMORE);
            publisher.send(payload.getBytes());
        }

        mock.await(10, TimeUnit.SECONDS);
        mock.assertIsSatisfied();
   
    }

    protected RouteBuilder createRouteBuilder() {

        return new RouteBuilder() {
            public void configure() {
                from("zeromq:tcp://localhost:5555?socketType=SUBSCRIBE&topics=" + TEST_TOPIC)
                        .transform(body().convertToString())
                        .to("mock:result");
            }
        };
    }
}