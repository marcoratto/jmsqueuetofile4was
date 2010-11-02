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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import uk.co.marcoratto.jmsqueuetofile.JmsQueueToFile;

public class Utility {

    private static final String CLASS_NAME = JmsQueueToFile.class.getName();  
    private static Logger logger = Logger.getLogger(CLASS_NAME);

    private final static int BUFFER = 10 * 1024;
    
    public static void copyInputStream(InputStream in, OutputStream out) throws UtilityException {
    	final String METHOD_NAME = "copyInputStream";
    	
        byte[] buffer = new byte[BUFFER];
        int len;

            try {
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
            	
            } catch (IOException e) {
            	logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            	throw new UtilityException(e.getMessage(), e);
			} finally {
            	if (in != null) {
            		try {
                        in.close();            		            			
            		} catch (IOException ioe) {            			
            		}
            	}
            	if (out != null) {
            		try {
            			out.close();            	            		
	        		} catch (IOException ioe) {            			
	        		}
            	}
            }
    }
    
    
    public static InputStream getInputStream(Message message) throws UtilityException {
    	final String METHOD_NAME = "getInputStream";
 	   
        try {
            // get the incoming msg content into a byte array
            if (message instanceof BytesMessage) {
                byte[] buffer = new byte[BUFFER];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                BytesMessage byteMsg = (BytesMessage) message;
                for (int bytesRead = byteMsg.readBytes(buffer); bytesRead != -1;
                     bytesRead = byteMsg.readBytes(buffer)) {
                    out.write(buffer, 0, bytesRead);
                }
                return new ByteArrayInputStream(out.toByteArray());

            } else if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                Enumeration propertyNames = txtMsg.getPropertyNames();
                while (propertyNames.hasMoreElements()) {
                	String key = (String) propertyNames.nextElement();
                	String value = txtMsg.getStringProperty(key);           
                	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, key + "=" + value);
                }
                
                // String contentType = message.getStringProperty(JMSConstants.CONTENT_TYPE);
                //if (contentType != null) {
                //    return
                //            new ByteArrayInputStream(txtMsg.getText().getBytes(BuilderUtil.getCharSetEncoding(contentType)));
                //} else {
                    return new ByteArrayInputStream(txtMsg.getText().getBytes());
                // }

            } else {
				throw new UtilityException("Unsupported JMS message type : " + message.getClass().getName());
            }
            
        } catch (JMSException e) {
        	logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
            throw new UtilityException(e.getMessage(), e);
        }
    }
    
    public static String getYear(Calendar now) {
    	String out = Integer.toString(now.get(Calendar.YEAR));
        return out;
    }
    
    public static String getMonth(Calendar now) {
        int mm = now.get(Calendar.MONTH)+1;
        String out = (mm <10 ? "0" : "") + mm;        
        return out;
    }

    public static String getDay(Calendar now) {
        int dd = now.get(Calendar.DAY_OF_MONTH);
        String out = (dd <10 ? "0" : "") + dd;        
        return out;
    }
    
    public static String getWeekOfDay(Calendar now) {
        int dow = now.get(Calendar.DAY_OF_WEEK);
        String out = (dow <10 ? "0" : "") + dow;        
        return out;
    }

    public static String getWeekOfYear(Calendar now) {
        int woy = now.get(Calendar.WEEK_OF_YEAR);
        String out = (woy <10 ? "0" : "") + woy;        
        return out;
    }

    public static String getHour(Calendar now) {
        int hh = now.get(Calendar.HOUR_OF_DAY);
        String out = (hh <10 ? "0" : "") + hh;        
        return out;
    }
    
    public static String getMinute(Calendar now) {
        int mi = now.get(Calendar.MINUTE);
        String out = (mi <10 ? "0" : "") + mi;        
        return out;
    }

    public static String getSecond(Calendar now) {
        int ss = now.get(Calendar.SECOND);
        String out = (ss <10 ? "0" : "") + ss;        
        return out;
    }

    public static String getMillisecond(Calendar now) {    	
        int ms = now.get(Calendar.MILLISECOND);            	
        String out = (ms <10 ? "0" : "") + ms;        
        return out;
    }

    
    public static String parseStringWithPattern(String source) {
    	String out = null;
    	Calendar now = Calendar.getInstance();
    	
    	out = source;
    	out = replaceAll(out, "%Y", getYear(now));
    	out = replaceAll(out, "%M", getMonth(now));
    	out = replaceAll(out, "%D", getDay(now));
    	out = replaceAll(out, "%W", getWeekOfDay(now));
    	out = replaceAll(out, "%w", getWeekOfYear(now));
    	out = replaceAll(out, "%h", getHour(now));
    	out = replaceAll(out, "%m", getMinute(now));
    	out = replaceAll(out, "%s", getSecond(now));
    	out = replaceAll(out, "%S", getMillisecond(now));
    	out = replaceAll(out, "%%", "%");
    	return out;
    }
    
    public static String replaceAll(String in, String oldWord, String newWord) {
        StringBuffer sb = new StringBuffer(in);
        int position = sb.toString().indexOf(oldWord);
        while (position > -1) {
             sb.replace(position, position+oldWord.length(), newWord);
             position = sb.toString().indexOf(oldWord,position+newWord.length());
        }
        return sb.toString();
    }
}
