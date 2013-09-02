package org.apache.camel.component.zeromq;
import org.apache.camel.Endpoint;
import org.junit.Test;

/**
 * @author Jinesh M.K
 * 
 */
public class ZeromqConfigurationTest extends ZeromqBaseTest{
	 @Test
	    public void testBasicConfiguration() throws Exception {
	        Endpoint endpoint = context.getEndpoint("zeromq:tcp://localhost:5555?socketType=PUBLISH&topics="+TEST_TOPIC);
	      
	        assertTrue("Endpoint not a ZeromqEndpoint: " + endpoint, endpoint instanceof ZeromqEndpoint);
	        ZeromqEndpoint zeromqEndpoint = (ZeromqEndpoint) endpoint;

	        assertEquals(zeromqEndpoint.getHostname(),"localhost");
	        assertEquals(zeromqEndpoint.getTopics(), TEST_TOPIC);
	        assertEquals(zeromqEndpoint.getSocketType(), ZeromqSocketType.PUBLISH);
	    }
}
