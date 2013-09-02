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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

/**
 * @author Jinesh M.K
 * 
 */
public class ZeromqSocketFactory implements SocketFactory {

	private final long		highWaterMark;
	private final long		linger;

	private static final Logger	logger	= LoggerFactory.getLogger(ZeromqSocketFactory.class);

	public ZeromqSocketFactory(long highWaterMark, long linger) {
		this.highWaterMark = highWaterMark;
		this.linger = linger;
	}

	void applySocketOptions(Socket socket) {
		if (highWaterMark >= 0){
			
			socket.setHWM(highWaterMark);
			
		}
		if (linger >= 0)
			socket.setLinger(linger);
	}

	@Override
	public Socket createConsumerSocket(Context context, ZeromqSocketType socketType) {
		logger.debug("Creating consumer socket [{}]", socketType);
		Socket socket;
		switch (socketType) {
		default:
			throw new ZeromqException("Unsupported socket type for consumer: " + this);
		case ROUTER:
			socket = context.socket(ZMQ.ROUTER);
			break;
		case SUBSCRIBE:
			socket = context.socket(ZMQ.SUB);
			break;
		case PULL:
			socket = context.socket(ZMQ.PULL);
			break;
		}
		applySocketOptions(socket);
		return socket;
	}

	@Override
	public Socket createProducerSocket(Context context, ZeromqSocketType socketType) {
		logger.debug("Creating producer socket [{}]", socketType);
		Socket socket;
		switch (socketType) {
		case DEALER:
			socket = context.socket(ZMQ.DEALER);
			break;
		case PUBLISH:
			socket = context.socket(ZMQ.PUB);
			break;
		case PUSH:
			socket = context.socket(ZMQ.PUSH);
			break;
		default:
			throw new ZeromqException("Unsupported socket type for producer: " + socketType);
		}
		applySocketOptions(socket);
		return socket;
	}

}
