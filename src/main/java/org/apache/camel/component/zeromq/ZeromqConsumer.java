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

import java.util.concurrent.ExecutorService;

import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.util.AsyncProcessorConverterHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jinesh M.K
 * 
 */
public class ZeromqConsumer extends DefaultConsumer {

	static final Logger		logger	= LoggerFactory.getLogger(ZeromqConsumer.class);

	private final Processor		processor;
	private final ZeromqEndpoint	endpoint;
	private final ContextFactory	contextFactory;
	private final SocketFactory	socketFactory;
	private ExecutorService		executor;
	private Listener			listener;

	public ZeromqConsumer(ZeromqEndpoint endpoint, Processor processor, ContextFactory contextFactory, SocketFactory socketFactory) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		this.contextFactory = contextFactory;
		this.socketFactory = socketFactory;
		this.processor = AsyncProcessorConverterHelper.convert(processor);
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();
		executor = endpoint.getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, endpoint.getEndpointUri(), 1);
		listener = new Listener(endpoint, processor, socketFactory, contextFactory);
		executor.submit(listener);
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		if (listener != null) {
			listener.stop();
		}
		if (executor != null) {
			logger.debug("Shutdown of executor");
			if (!executor.isShutdown()) {
				executor.shutdownNow();
			}
			logger.debug("Executor is now shutdown");
			executor = null;
		}
	}

	public ContextFactory getContextFactory() {
		return contextFactory;
	}

	public SocketFactory getSocketFactory() {
		return socketFactory;
	}

	@Override
	public void resume() throws Exception {
		super.resume();
		doStart();
	}

	@Override
	public void suspend() throws Exception {
		super.suspend();
		// currently do not support resume and suspend of listener, right
		// now this delegates to just stopping and
		// starting the consumer.
		doStop();
	}
}
