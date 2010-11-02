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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;

/**
 * @author Marco Ratto
 */
public class QueueUtils {

    private final static String CLASS_NAME = QueueUtils.class.getName();
    private static Logger logger = Logger.getLogger(CLASS_NAME);
    
    private QueueUtils() {
    }

    public static void closeQuietly(QueueConnection conn, QueueSession session, QueueSender sender) {
    	final String METHOD_NAME = "closeQuietly";
    	if (sender != null) {
            try {
                sender.close();
            } catch (JMSException e) {
            	logger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            }
        }
        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
            	logger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (JMSException e) {
            	logger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            }
        }

    }

}
