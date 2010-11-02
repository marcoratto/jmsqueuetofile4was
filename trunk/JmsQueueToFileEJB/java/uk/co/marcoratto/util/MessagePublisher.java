/*
 * Copyright (C) 2010 Marco Ratto
 *
 * This file is part of the project JmsQueueToFile.
 *
 * JmsQueueToFile is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * JmsQueueToFile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JmsQueueToFile; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package uk.co.marcoratto.util;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

public class MessagePublisher {

    static final String CLASS_NAME = MessagePublisher.class.getName();
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * The JNDI queue reference name for the deafult destination queue.
     */
    private String queueRef;

    /**
     * Create a new MessagePublisher for publishing messages. A default
     * destination queue can be defined by supplying its JNDI reference name.
     * 
     * @param queueRef
     */
    public MessagePublisher(String queueRef) {
        this.queueRef = queueRef;
    }

    /**
     * Create a new MessagePublisher for bean-style usage.
     * 
     * Note: A default destination Queue can be set before using the instance
     * setting the JNDI queue reference name via <code>setQueueRef</code>.
     */
    public MessagePublisher() {
    }

    public String getQueueRef() {
        return queueRef;
    }

    /**
     * Set the default destination queue for the MessagePublisher.
     * 
     * @param queueRef
     *            The JNDI queue reference name.
     */
    public void setQueueRef(String queueRef) {
        this.queueRef = queueRef;
    }

    /**
     * Publish the message to the supplied destination queue through a JMS queue
     * session "not transacted" and "auto-acknowledge".
     * <P>
     * The object message is converted in <code>TextMessage</code> for a
     * String object and in an <code>ObjectMessage</code> for a Serializable
     * object.
     * 
     * @param message
     *            The object representing the message to be published.
     * @throws SubmitException
     *             The JMS service provider is not available or failed to send
     *             the message due some communication problems or the message
     *             object is not of a supported type.
     */
    public void publish(Queue queue, Object message) throws MessagePublisherException {
        final String METHOD_NAME = "publish";
        logger.entering(CLASS_NAME, METHOD_NAME);
        
        QueueConnection conn = null;
        QueueSession session = null;
        QueueSender sender = null;
        try {
            logger.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Creating connection");
            conn = EtmQueueConnection.getQueueConnection();

            logger.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Creating Session");

            // Create a new queue session from the queue connection. The session
            // should not be transacted and should use automatic message
            // acknowledgement.
            session = conn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

            // Create a new queue sender using the queue session.
            sender = session.createSender(queue);

            // Create the JMS message using the queue session.
            Message jmsMsg = toMessage(session, message);

            // Send the sentMessage object using the queue sender.
            logger.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Sending message to destination queue.");
            sender.send(jmsMsg);

            logger.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Closing connection");

        } catch (JMSException jmse) {
            logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "JMSException during publish message: " + jmse.getMessage(), jmse);
            Exception linked = jmse.getLinkedException();
            logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "Exception during publish message: " + linked.getMessage(), linked);
            throw new MessagePublisherException(jmse.getMessage(), jmse);
        } finally {

            QueueUtils.closeQuietly(conn, session, sender);
        }

        logger.exiting(CLASS_NAME, METHOD_NAME);
    }

    /**
     * Publish the message to the default destination queue through a JMS queue
     * session "not transacted" and "auto-acknowledge".
     * <P>
     * The object message is converted in <code>TextMessage</code> for a
     * String object and in an <code>ObjectMessage</code> for a Serializable
     * object.
     * 
     * @param message
     *            The object representing the message to be published.
     * @throws SubmitException
     *             The JMS service provider is not available or failed to send
     *             the message due some communication problems or the message
     *             object is not of a supported type.
     */
    public void publish(Object message) throws MessagePublisherException {
        final String METHOD_NAME = "publish";
        logger.entering(CLASS_NAME, METHOD_NAME);

        // Lookup the queue to be used to send and receive
        // messages from the initial context.
        Queue queue = EtmQueueLocator.getQueue(queueRef);

        publish(queue, message);

        logger.exiting(CLASS_NAME, METHOD_NAME);
    }

    /**
     * Convert a Java object to a JMS message using the provided session. The
     * method supplies the convertion of a String object to a
     * <code>TextMessage</code>, of a Serializable object to a
     * <code>ObjectMessage</code> object.
     * 
     * @param message
     *            The message to transport.
     * @param session
     *            The current JMS session.
     * @return The JMS message that contains the required message object.
     * @throws JMSException
     *             The JMS service provider is not available or failed to send
     *             the message due some communication problems or the message
     *             object is not of a supported type.
     */
    private Message toMessage(Session session, Object message) throws JMSException {

        Message jmsMsg = null;
        if (message instanceof String) {
            // Create a text message using the queue session.
            // Initialize the message"s data to a String of your choice.
            jmsMsg = session.createTextMessage((String) message);
        } else if (message instanceof Message) {
            jmsMsg = (Message)message;
        } else if (message instanceof Serializable) {
            jmsMsg = session.createObjectMessage((Serializable) message);
        } else {
            throw new JMSException("The message type is not supported yet.");
        }

        return jmsMsg;
    }

}
