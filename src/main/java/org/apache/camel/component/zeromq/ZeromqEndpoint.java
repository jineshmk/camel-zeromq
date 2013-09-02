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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;

/**
 * @author Jinesh M.K
 * 
 */
public class ZeromqEndpoint extends DefaultEndpoint {

	private static final String	URI_ERROR		= "Invalid URI. Format must be of the form zeromq:(tcp|icp)://hostname:port[?options...]";

	private final String		protocol;

	private final String		hostname;

	private boolean			messageIdEnabled;

	private final int			port;

	private ZeromqSocketType	socketType;

	private long			highWaterMark	= -1;

	private long			linger		= -1;

	private String			topics;

	private boolean			asyncConsumer	= true;

	private Class			messageConvertor	= DefaultMessageConvertor.class;

	private SocketFactory		socketFactory;

	private ContextFactory		contextFactory;

	public ZeromqEndpoint(String endpointUri, String remaining, ZeromqComponent component) throws URISyntaxException {
		super(endpointUri, component);

		URI uri = new URI(remaining);

		protocol = uri.getScheme();
		if (protocol == null)
			throw new ZeromqException(URI_ERROR);

		if (!protocol.equalsIgnoreCase("TCP") && !protocol.equalsIgnoreCase("IPC"))
			throw new ZeromqException(URI_ERROR);

		hostname = uri.getHost();
		if (hostname == null)
			throw new ZeromqException(URI_ERROR);

		port = uri.getPort();
		if (port < 0)
			throw new ZeromqException(URI_ERROR);

		this.socketFactory = new ZeromqSocketFactory(highWaterMark, linger);
		this.contextFactory = new ZeromqContextFactory();
	}

	@Override
	public ZeromqConsumer createConsumer(Processor processor) throws Exception {
		if (socketType == null)
			throw new ZeromqException("Must specify socket type as a parameter, eg socketType=SUBSCRIBE");
		return new ZeromqConsumer(this, processor, contextFactory, socketFactory);
	}

	@Override
	public ZeromqProducer createProducer() throws Exception {
		if (socketType == null)
			throw new ZeromqException("Must specify socket type as a parameter, eg socketType=PUBLISH");
		return new ZeromqProducer(this, socketFactory, contextFactory);
	}

	Exchange createZeromqExchange(byte[] body) {
		Exchange exchange = new DefaultExchange(getCamelContext(), getExchangePattern());

		Message message = new DefaultMessage();
		message.setHeader(ZeromqConstants.HEADER_SOURCE, getSocketAddress());
		message.setHeader(ZeromqConstants.HEADER_SOCKET_TYPE, socketType);
		message.setHeader(ZeromqConstants.HEADER_TIMESTAMP, System.currentTimeMillis());
		if (isMessageIdEnabled())
			message.setHeader(ZeromqConstants.HEADER_MSG_ID, UUID.randomUUID().toString());

		message.setBody(body);
		exchange.setIn(message);

		return exchange;
	}

	public long getHighWaterMark() {
		return highWaterMark;
	}

	public String getHostname() {
		return hostname;
	}

	public long getLinger() {
		return linger;
	}

	public Class getMessageConvertor() {
		return messageConvertor;
	}

	public int getPort() {
		return port;
	}

	public String getProtocol() {
		return protocol;
	}

	String getSocketAddress() {
		String addr = getProtocol() + "://" + getHostname() + ":" + getPort();
		return addr;
	}

	public ZeromqSocketType getSocketType() {
		return socketType;
	}

	public String getTopics() {
		return topics;
	}

	public boolean isAsyncConsumer() {
		return asyncConsumer;
	}

	public boolean isMessageIdEnabled() {
		return messageIdEnabled;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public void setAsyncConsumer(boolean asyncConsumer) {
		this.asyncConsumer = asyncConsumer;
	}

	public void setContextFactory(ContextFactory contextFactory) {
		this.contextFactory = contextFactory;
	}

	public void setHighWaterMark(long highWaterMark) {
		this.highWaterMark = highWaterMark;
	}

	public void setLinger(long linger) {
		this.linger = linger;
	}

	public void setMessageConvertor(Class messageConvertor) {
		this.messageConvertor = messageConvertor;
	}

	public void setMessageIdEnabled(boolean messageIdEnabled) {
		this.messageIdEnabled = messageIdEnabled;
	}

	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	public void setSocketType(ZeromqSocketType socketType) {
		this.socketType = socketType;
	}

	public void setTopics(String topics) {
		this.topics = topics;
	}

}
