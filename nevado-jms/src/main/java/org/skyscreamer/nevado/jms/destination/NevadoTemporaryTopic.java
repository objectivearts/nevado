package org.skyscreamer.nevado.jms.destination;

import org.skyscreamer.nevado.jms.NevadoConnection;

import javax.jms.JMSException;
import javax.jms.TemporaryTopic;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * Nevado implementation of a temporary topic
 *
 * @author Carter Page <carter@skyscreamer.org>
 */
public class NevadoTemporaryTopic extends NevadoTopic implements TemporaryTopic {
    private final transient NevadoConnection _connection;

    public NevadoTemporaryTopic(NevadoConnection connection, NevadoTopic topic) throws JMSException {
        super(topic);
        _connection = connection;
    }

    public synchronized void delete() throws JMSException {
        if (_connection != null) {
            _connection.deleteTemporaryTopic(this);
        }
    }

    @Override
    public Reference getReference() throws NamingException {
        throw new NamingException("NevadoTemporaryTopic is not supported.  Temporary destinations must remain within " +
                "the connection that created them.");
    }
}
