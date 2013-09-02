package org.apache.camel.component.zeromq;

/**
 * @author Jinesh M.K
 * 
 */
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.builder.RouteBuilder;


import org.junit.Test;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;

import org.zeromq.ZMQ.Socket;

public class ZeromqProducerTest extends ZeromqBaseTest {

    @Test
    public void testProduce() throws Exception {
    	Context context1 = (new ZeromqContextFactory()).createContext(1);
		final Socket subscriber = context1.socket(ZMQ.SUB);
		subscriber.connect("tcp://localhost:5555");
         
        final CountDownLatch latch = new CountDownLatch(numberOfMessages);
       subscriber.subscribe(TEST_TOPIC.getBytes());
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for (int i = 0; i < numberOfMessages; i++) {
                    try {
                    	subscriber.recv(0);
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        thread.start();

        Producer producer = context.getEndpoint("direct:foo").createProducer();
        for (int i = 0; i < numberOfMessages; i++) {
            Exchange exchange = producer.createExchange();
            exchange.getIn().setBody("test message " + i);
            producer.process(exchange);
        }
        latch.await(20, TimeUnit.SECONDS);
        assertTrue("Messages not consumed = " + latch.getCount(), latch.getCount() == 0);
        subscriber.close();
    }

    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:foo").to("zeromq:tcp://0.0.0.0:5555?socketType=PUBLISH&topics=" + TEST_TOPIC);
            }
        };
    }
}