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

package uk.co.marcoratto.jmsqueuetofile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Queue;

import uk.co.marcoratto.util.EtmQueueLocator;
import uk.co.marcoratto.util.MessagePublisher;
import uk.co.marcoratto.util.Utility;
import uk.co.marcoratto.util.Config;

@SuppressWarnings("serial")
public class JmsQueueToFile implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener {
	
    private javax.ejb.MessageDrivenContext fMessageDrivenCtx;

    private static final String CLASS_NAME = JmsQueueToFile.class.getName();  
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * getMessageDrivenContext
     */
    public javax.ejb.MessageDrivenContext getMessageDrivenContext() {
        return fMessageDrivenCtx;
    }

    /**
     * setMessageDrivenContext
     */
    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext ctx) {
        fMessageDrivenCtx = ctx;
    }

    /**
     * ejbCreate
     */
    public void ejbCreate() {
    }

    /**
     * ejbRemove
     */
    public void ejbRemove() {
    }

    /**
     * onMessage
     */
    public void onMessage(javax.jms.Message msg) {
        logger.entering(CLASS_NAME, "onMessage()");
        consume(msg);
        logger.exiting(CLASS_NAME, "onMessage()");
    }
    
    /**
     * onMessage
     */
    public void consume(javax.jms.Message msg) {
        final String METHOD_NAME = "consume";
        logger.entering(CLASS_NAME, METHOD_NAME);

        long startTime = System.currentTimeMillis();
                
        Queue errorQueue = null;
         
        try {
        	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Start at " + new Date());

        	StringBuffer sb = new StringBuffer();
            sb.append("Received JMS message to destination : " + msg.getJMSMessageID());                
            sb.append("\nMessage ID : " + msg.getJMSMessageID());
            sb.append("\nCorrelation ID : " + msg.getJMSCorrelationID());
            sb.append("\nReplyTo ID : " + msg.getJMSReplyTo());            
            logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, sb.toString());
            
            save(msg);               
        } catch (Exception e) {
            errorQueue = EtmQueueLocator.getQueueError();
            logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "The received event generated an unexpected exception.", e);
        }

        // if an error occurred while processing the message, the errorQueue
        // instance refers the auditing queue where we need to put the error
        // message.
        if (errorQueue != null) {
            MessagePublisher publisher = new MessagePublisher();
            try {
                publisher.publish(errorQueue, msg);
            } catch (Exception e) {
                logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "The system is not able to publish the event to the error queue.", e);
            }
        }
        long endTime = System.currentTimeMillis();        
        long delay = endTime - startTime;
        logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "End at " + new Date());
        logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Delay " + delay + " ms");
        
        logger.exiting(CLASS_NAME, METHOD_NAME);
    }

    private void save(javax.jms.Message msg) throws Exception {
        final String METHOD_NAME = "convertJMSMsgToStringMsg";

        try {                        
            String pathname = Config.getInstance().getStringProperty("target.pathname", null);
            String targetFilename = Utility.parseStringWithPattern(pathname);

            File f = new File(targetFilename);
            
            boolean targetCreateDir = Config.getInstance().getBooleanProperty("target.create.dir", false);
        	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "target.create.dir=" + targetCreateDir);

        	boolean targetOverwriteFile = Config.getInstance().getBooleanProperty("target.overwrite.file", false);
        	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "target.overwrite.file=" + targetOverwriteFile);
            
        	File parentDir = new File(f.getParent());
            if (targetCreateDir && !parentDir.exists()) {
            	try {
            		parentDir.mkdirs();
            		logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Create " + parentDir.getAbsolutePath());
            	} catch (Throwable t) {
                    logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, t.getMessage(), t);
                    throw new Exception(t.getMessage(), t);            		
            	}
            }
            
            if (targetOverwriteFile) {
            	f.delete();
	            logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Delete file " + f.getAbsolutePath());
            }            
            if (f.exists()) {
	            logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "File " + f.getAbsolutePath() + " already exists.");            	
            } else {
	            InputStream in = Utility.getInputStream(msg);            
	            logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "Write message to file " + f.getAbsolutePath());
	            FileOutputStream out = new FileOutputStream(f);	                
	            Utility.copyInputStream(in, out);
            }
                            
        } catch (Exception e) {
            logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            throw new Exception(e.getMessage(), e);
        }
    }
    
}
