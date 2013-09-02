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

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @author Jinesh M.K
 * 
 */
public class ZeromqProducer extends DefaultProducer {

	private static final Logger		logger		= LoggerFactory.getLogger(ZeromqProducer.class);

	private final ZeromqEndpoint		endpoint;
	private Socket				socket;
	private Context				context;
	private final MessageConverter	messageConvertor;
	private final SocketFactory		socketFactory;
	private final ContextFactory		contextFactory;
	private int					shutdownWait	= 5000;
	private String[]				topics;

	public ZeromqProducer(ZeromqEndpoint endpoint, SocketFactory socketFactory, ContextFactory contextFactory)
			throws InstantiationException, IllegalAccessException {
		super(endpoint);
		this.endpoint = endpoint;
		this.socketFactory = socketFactory;
		this.contextFactory = contextFactory;
		this.messageConvertor = (MessageConverter) endpoint.getMessageConvertor().newInstance();
	}

	public ContextFactory getContextFactory() {
		return contextFactory;
	}

	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void process(Exchange arg0) throws Exception {
		byte[] body = messageConvertor.convert(arg0);
		if (topics == null) {
			socket.send(body, 0);
		} else {
			for (String topic : topics) {
				byte[] t = topic.getBytes();
				byte[] prefixed = new byte[t.length + body.length];
				System.arraycopy(t, 0, prefixed, 0, t.length);
				System.arraycopy(body, 0, prefixed, t.length, body.length);
				socket.send(prefixed, 0);
			}
		}
		arg0.getIn().setHeader(ZeromqConstants.HEADER_TIMESTAMP, System.currentTimeMillis());
		arg0.getIn().setHeader(ZeromqConstants.HEADER_SOURCE, endpoint.getSocketAddress());
		arg0.getIn().setHeader(ZeromqConstants.HEADER_SOCKET_TYPE, endpoint.getSocketType());
	}

	public void setShutdownWait(int shutdownWait) {
		this.shutdownWait = shutdownWait;
	}

	@Override
	public void start() throws Exception {

		this.context = contextFactory.createContext(1);
		this.socket = socketFactory.createProducerSocket(context, endpoint.getSocketType());
		this.topics = endpoint.getTopics() == null ? null : endpoint.getTopics().split(",");

		String addr = endpoint.getSocketAddress();
		logger.info("Binding client to [{}]", addr);
		socket.bind(addr);
		logger.info("Client bound OK");
	}

	@Override
	public void stop() throws Exception {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					socket.close();
				} catch (Exception e) {
				}
			}
		});
		t.start();
		logger.debug("Waiting {}ms for producer socket to close", shutdownWait);
		t.join(shutdownWait);
		try {
			context.term();
		} catch (Exception e) {
		}
	}
}
