package org.apache.camel.component.zeromq;


import org.apache.camel.test.junit4.CamelTestSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jinesh M.K
 * 
 */
public abstract class ZeromqBaseTest extends CamelTestSupport {
    protected static final Logger LOG = LoggerFactory.getLogger(ZeromqBaseTest.class);
    protected static final String TEST_TOPIC = "ComponentTestTopic";
    protected int numberOfMessages = 2;

    public void setUp() throws Exception {
    	super.setUp();
    }


    public void tearDown() throws Exception {
    	super.tearDown();
    }
}