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

import java.net.URL;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.jms.Queue;
import javax.jms.QueueConnectionFactory;
import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;

public class ServiceLocator {

    private Context ivContext;

    private static ServiceLocator cvInstance;

    protected ServiceLocator() {
    }

    public static ServiceLocator getInstance() throws ServiceLocatorException {
        if (cvInstance == null) {
            cvInstance = new ServiceLocator();
        }
        return cvInstance;
    }

    public EJBLocalHome getLocalHome(String ejbReferenceName) throws ServiceLocatorException {
        return (EJBLocalHome) lookupService(ejbReferenceName, getInitialContext());
    }

    public EJBHome getRemoteHome(String ejbReferenceName, Class homeClass) throws ServiceLocatorException {
        Object objref = lookupService(ejbReferenceName, getInitialContext());
        Object obj = PortableRemoteObject.narrow(objref, homeClass);
        return (EJBHome) obj;
    }

    public QueueConnectionFactory getQueueConnectionFactory(String connectionFactoryName) throws ServiceLocatorException {
        return (QueueConnectionFactory) lookupService(connectionFactoryName, getInitialContext());
    }

    public Queue getQueue(String queueName) throws ServiceLocatorException {
        return (Queue) lookupService(queueName, getInitialContext(), false);
    }

    public Session getMailSession(String sessionName) throws ServiceLocatorException {
        return (Session) lookupService(sessionName, getInitialContext());
    }

    public URL getResourceURL(String urlName) throws ServiceLocatorException {
        return (URL) lookupService(urlName, getInitialContext());
    }

    public String getResourceString(String name) throws ServiceLocatorException {
        return (String) lookupService(name, getInitialContext());
    }
    
    public DataSource getDataSource(String jndiName) throws ServiceLocatorException {
        return (DataSource) lookupService(jndiName, getInitialContext());
    }
    
    protected String getNamespace() {
        return "java:comp/env/";
    };

    //
    // Private utility methods
    //

    private Context getInitialContext() throws ServiceLocatorException {
        if (ivContext == null) {
            try {
                ivContext = new InitialContext();
            } catch (NamingException e) {
                throw new ServiceLocatorException(e);
            }
        }
        return ivContext;
    }

    private Object lookupService(String referenceName, Context context, boolean nameSpace) throws ServiceLocatorException {
        if (referenceName == null) {
            throw new IllegalArgumentException("The service reference name cannot be null");
        }
        try {
        	String prefix = (nameSpace)?getNamespace():"";
            return context.lookup( prefix + referenceName);
        } catch (NamingException e) {
            ivContext = null;
            throw new ServiceLocatorException("Unabe to lookup service " + referenceName, e);
        }
    }
    
    private Object lookupService(String referenceName, Context context) throws ServiceLocatorException {
        return lookupService(referenceName, context, false);
    }
    
}
