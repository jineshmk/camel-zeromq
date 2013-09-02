/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.zeromq;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.AsyncProcessorConverterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @author Jinesh M.K
 * 
 */
class Listener implements Runnable {

	private static final Logger	logger	= LoggerFactory.getLogger(Listener.class);

	private volatile boolean	running	= true;
	private Socket			socket;
	private Context			context;
	private final ZeromqEndpoint	endpoint;
	private final Processor		processor;
	private final SocketFactory	akkaSocketFactory;
	private final ContextFactory	akkaContextFactory;
	private AsyncCallback		callback	= new AsyncCallback() {

									@Override
									public void done(boolean doneSync) {
										// no-op
									}
								};

	public Listener(ZeromqEndpoint endpoint, Processor processor, SocketFactory akkaSocketFactory, ContextFactory akkaContextFactory) {
		this.endpoint = endpoint;
		this.akkaSocketFactory = akkaSocketFactory;
		this.akkaContextFactory = akkaContextFactory;
		if (endpoint.isAsyncConsumer())
			this.processor = AsyncProcessorConverterHelper.convert(processor);
		else
			this.processor = processor;
	}

	void connect() {
		context = akkaContextFactory.createContext(1);
		socket = akkaSocketFactory.createConsumerSocket(context, endpoint.getSocketType());

		String addr = endpoint.getSocketAddress();
		logger.info("Connecting to server [{}]", addr);
		socket.connect(addr);
		logger.info("Connected OK");

		if (endpoint.getSocketType() == ZeromqSocketType.SUBSCRIBE) {
			subscribe();
		}
	}

	@Override
	public void run() {
		connect();
		while (running) {
			byte[] msg = socket.recv(0);
			if (msg == null)
				continue;
			logger.trace("Received message [length=" + msg.length + "]");
			Exchange exchange = endpoint.createZeromqExchange(msg);
			logger.trace("Created exchange [exchange={}]", new Object[] { exchange });
			try {
				if (processor instanceof AsyncProcessor) {
					((AsyncProcessor) processor).process(exchange, callback);
				} else {
					processor.process(exchange);
				}
			} catch (Exception e) {
				logger.error("Exception processing exchange [{}]", e);
			}
		}

		try {
			logger.info("Closing socket");
			socket.close();
		} catch (Exception e) {
		}
		try {
			logger.info("Terminating context");
			context.term();
		} catch (Exception e) {
		}
	}

	public void setCallback(AsyncCallback callback) {
		this.callback = callback;
	}

	void stop() {
		logger.debug("Requesting shutdown of consumer thread");
		running = false;
		// we have to term the context to interrupt the recv call
		if (context != null)
			try {
				context.term();
			} catch (Exception e) {
			}
	}

	void subscribe() {
		if (endpoint.getTopics() == null) {
			// subscribe all by using
			// empty filter
			logger.debug("Subscribing to all messages (topics option was not specified)", endpoint.getTopics());
			socket.subscribe("".getBytes());
		} else {
			logger.debug("Subscribing to topics: {}", endpoint.getTopics());
			for (String topic : endpoint.getTopics().split(",")) {
				socket.subscribe(topic.getBytes());
			}
		}
	}
}