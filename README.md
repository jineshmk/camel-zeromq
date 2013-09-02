camel-zeromq with jeromq support
================================

*Available as of Camel 2.11*

The *zeromq:* component allows you to consumer or produce messages using [ZeroMQ|http://zeromq.org/]. 
Assertion failure is one of the main issue in native libzmq implementation. 
Therefore here we are using jeromq instead of libzmq. Jeromq is the pure Java implementation of libzmq.[https://github.com/zeromq/jeromq].	

Here I am using latest jeromq version(not stable). You can also switch to pervious version by adding appropriate version in pom.xml


Maven users will need to add the following dependency to their {{pom.xml}} for this component:

{code:xml}
<dependency>
    <groupId>org.apache.camel</groupId>
    <artifactId>camel-zeromq</artifactId>
    <version>x.x.x</version>
    <!-- use the same version as your Camel core version -->
</dependency>
{code}

h3. URI format

{code}
zeromq:(tcp|ipc)://hostname:port[?options...]
{code}

Where *hostname* is the hostname or ip where the server will run. 

h3. Options
{div:class=confluenceTableSmall}
|| Property || Default || Description ||
| {{socketType}} | {{none}} | Choose from PULL, SUBSCRIBE for consumers, or PUSH, PUBLISH for producers |
| {{asyncConsumer}} | {{true}} | Sets the consumer to be async. |
| {{topics}} | {{null}} | Applicable for subscribers and publishers only. Determines which topics to subscribe or publish to. If not specified then the subscriber will subscribe to all messages. If not specified by a publisher then no topic will be prefixed to the message. Specify multiple topics by a comma separated list |
| {{messageIdEnabled}} | {{false}} | If enabled then camel-zeromq will add a unique UUID to each message as it is received |
| {{messageConvertor}} | {{DefaultMessageConvertor}} | The message convertor that is used to turn an exchange into a byte array for dispatch through zeromq. See later section. |
| {{highWaterMark}} | {{-1}} | By default, zeromq will keep messages in an in-memory buffer while waiting for clients to receive. The high water mark is the max number of messages to hold before throwing an exception. -1 sets to the zeromq default, 0 disables, and any positive value sets to that value. |
| {{linger}} | {{-1}} | By default, zeromq will wait for sending messages to be received before shutting down. Linger is the number of seconds to wait before force closing the socket. -1 sets to the zeromq default, 0 disables, and any positive value sets to that value. |
{div}

h3. Headers

The follwing headers are set on exchanges during message transport.

|| Header Name || Endpoint Type || Description ||
| ZeroMQMessageId | Consumer | A type 4 UUID that is added to received messages |
| ZeroMQTimestamp | Both | The timestamp when the message was received |
| ZeroMQSource | Both | The socket address where the message was received from or pushed to |
| ZeroMQSocketType | Both | The type of zeromq that the message was received from or pushed to |

h3. Message Conversion

ZeroMQ sends its messages as a byte stream and so incoming messages have the body set as a byte[] array. When sending to a producer, the messageConvertor is invoked, and passed the Exchange. The DefaultMessageConvertor will return the body as is, if it is a byte array, otherwise it will call toString().getBytes(). You can provide custom conversion strategies by specifying a class to the messageConvertor option on the endpoint.

h3. Samples

To receive all messages from a publisher, and then print those to a logger:

{code}
from("zeromq:tcp://127.0.0.1:5555").process(someLoggingProcessor);
{code}

To broadcast a message every second on three topics:

{code}
        from("timer://foo?fixedRate=true&period=10").process(new Processor() {
        
        List<String> asList = Arrays.asList("coldplay", "keane", "jethro tull", "jack bruce", "elton john", "kate bush");

			@Override
			public void process(Exchange exchange) throws Exception {
				Collections.shuffle(asList);
				exchange.getIn().setBody(asList.get(0));
			}
		}).to("zeromq:tcp://127.0.0.1:5556?socketType=PUBLISH&topics=bands,musicians,singers");
{code}
