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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

public class EtmQueueConnection {

    private static final String CLASS_NAME = EtmQueueConnection.class.getName();  
    private static Logger logger = Logger.getLogger(CLASS_NAME);

	private static Map<String, QueueConnectionFactory> queueConnections = new HashMap<String, QueueConnectionFactory>();
		
	private static final String etmQCF = "jms/QueueConnectionFactory";
	
	/**
	 * Returns the default container connection.
	 * 
	 * @return QueueConnection
	 * @throws JMSException
	 */
	public static QueueConnection getQueueConnection() throws JMSException {
		QueueConnectionFactory qcf = getQCF(etmQCF);
		QueueConnection conn = qcf.createQueueConnection();
		return conn;
	}
	
	public static QueueConnectionFactory getQCF(String jndiName) {
		if (queueConnections.containsKey(jndiName)) {
			return queueConnections.get(jndiName);
		} else {
		    QueueConnectionFactory qcf = lookupQCF(jndiName);
		    logger.logp(Level.FINEST, CLASS_NAME, "getQCF", "Created queueConnectonFactory with jndiName: " + jndiName );
		    return qcf;
		}
	}
	
	private static synchronized QueueConnectionFactory lookupQCF(String jndiName) {
		if (queueConnections.containsKey(jndiName)) {
			return queueConnections.get(jndiName);
		}
		QueueConnectionFactory qcf = null;
		try {
			qcf = ServiceLocator.getInstance().getQueueConnectionFactory(etmQCF);
			queueConnections.put(jndiName, qcf);
		} catch (Exception e) {
			logger.logp(Level.SEVERE, CLASS_NAME, "lookupQCF", "Error lookup qcf: " + jndiName, e);
		}
		return qcf;
	}
}
