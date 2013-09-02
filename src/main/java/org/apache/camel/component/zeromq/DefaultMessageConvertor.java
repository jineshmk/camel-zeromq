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

/**
 * @author Jinesh M.K
 * 
 */
public class DefaultMessageConvertor implements MessageConverter {

	@Override
	public byte[] convert(Exchange arg0) {
		Object msg = arg0.getIn().getBody();
		if (msg instanceof String) {
			return ((String) msg).getBytes();
		} else if (msg instanceof byte[]) {
			return (byte[]) msg;
		} else {
			return msg.toString().getBytes();
		}
	}
}
