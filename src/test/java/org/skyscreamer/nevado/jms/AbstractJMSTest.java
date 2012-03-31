package org.skyscreamer.nevado.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import javax.jms.Queue;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Carter Page
 * Date: 3/22/12
 * Time: 3:23 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@ContextConfiguration(locations = { "classpath:/testContext.xml" })
public abstract class AbstractJMSTest {
    private static final String TEST_QUEUE_NAME = "testQueue";

    private final Log _log = LogFactory.getLog(AbstractJMSTest.class);

    private String _awsAccessKey;
    private String _awsSecretKey;

    @Autowired private ConnectionFactory connectionFactory;
    private Session _session;
    private Queue _testQueue = new NevadoQueue(TEST_QUEUE_NAME);

    @Before
    public void setUp() throws JMSException, IOException {
        initializeAWSCredentials();
        _session = connectionFactory.createConnection(_awsAccessKey, _awsSecretKey).createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    protected void clearTestQueue() throws JMSException {
        // Clear out the test queue
        int msgCount = 0;
        MessageConsumer consumer = _session.createConsumer(new NevadoQueue(TEST_QUEUE_NAME));
        Message message;
        while((message = consumer.receiveNoWait()) != null) {
            ++msgCount;
            message.acknowledge();
        }
        _log.info("Cleared out " + msgCount + " messages");
    }

    protected Message sendAndReceive(Message msg) throws JMSException {
        getSession().createProducer(getTestQueue()).send(msg);
        Message msgOut = getSession().createConsumer(getTestQueue()).receive();
        Assert.assertNotNull("Got null message back", msgOut);
        msgOut.acknowledge();
        return msgOut;
    }

    private void initializeAWSCredentials() throws IOException {
        Properties prop = new Properties();
        InputStream in = getClass().getResourceAsStream("/aws.properties");
        prop.load(in);
        in.close();

        _awsAccessKey = prop.getProperty("aws.accessKey");
        _awsSecretKey = prop.getProperty("aws.secretKey");
        if (_awsAccessKey == null || _awsAccessKey.trim().length() == 0
            || _awsSecretKey == null || _awsSecretKey.trim().length() == 0) {
                System.out.println("ATTENTION: You have not set up your AWS credentials.  Follow the following steps:\n" +
                        "    1. Copy src/test/resources/aws.properties.TEMPLATE to src/test/resources/aws.properties\n" +
                        "    2. Edit aws.properties with your access keys from https://aws-portal.amazon.com/gp/aws/securityCredentials\n" +
                        "    3. Have git ignore the new file.  Add the following line to .git/info/exclude:\n" +
                        "        src/test/resources/aws.properties\n\n" +
                        "Be careful to keep your keys in a safe place and don't commit them to source control.");
            throw new MissingResourceException("Resource /aws.properties does not exist",
                    null, null);
        }
    }

    @After
    public void tearDown() throws JMSException {
        // Do nothing
    }

    protected Session getSession() {
        return _session;
    }
    
    protected Queue getTestQueue() {
        return _testQueue;
    }
    
    protected String getAwsAccessKey() {
        return _awsAccessKey;
    }

    protected String getAwsSecretKey() {
        return _awsSecretKey;
    }
}
