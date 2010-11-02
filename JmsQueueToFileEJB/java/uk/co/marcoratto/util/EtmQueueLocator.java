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

import javax.jms.Queue;

public class EtmQueueLocator {

	private static final String CLASS_NAME = EtmQueueLocator.class.getName();
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private static Map<String, Queue> queues = new HashMap<String, Queue>();
    
    public static final String QUEUE_INBOUND = "jms/JmsQueueToFile";

    private static final String QUEUE_ERROR = "jms/JmsQueueToFileError";

    /**
     * Returns the Queue that contains correlation messages generated starting
     * from an ETM event.
     * 
     * @return Connection
     */
    public static Queue getQueueInbound() {
        Queue queue = getQueue(QUEUE_INBOUND);
        return queue;
    }

    public static Queue getQueueError() {
        Queue queue = getQueue(QUEUE_ERROR);
        return queue;
    }
    
    /**
     * Returns the queue identified by the provided JNDI name.
     * 
     * @return Queue The required queue.
     */
    public static Queue getQueue(String jndiName) {
        if (queues.containsKey(jndiName)) {
            return queues.get(jndiName);
        } else {
            Queue queue = lookupQueue(jndiName);
            logger.logp(Level.FINEST, CLASS_NAME, "getQueue", "Created Queue with jndiName: " + jndiName);
            return queue;
        }
    }

    private static synchronized Queue lookupQueue(String jndiName) {
        if (queues.containsKey(jndiName)) {
            return queues.get(jndiName);
        }
        Queue queue = null;
        try {
            queue = ServiceLocator.getInstance().getQueue(jndiName);
            queues.put(jndiName, queue);
        } catch (Exception e) {
            logger.logp(Level.SEVERE, CLASS_NAME, "lookupQueue", "Error lookup queue: " + jndiName, e);
        }
        return queue;
    }
}
