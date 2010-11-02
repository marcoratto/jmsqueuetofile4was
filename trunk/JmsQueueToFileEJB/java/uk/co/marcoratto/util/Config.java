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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Questa e' la classe base per la gestione dei file di <code>properties</code>. Per utilizzarla e' necessario sviluppae una classe che la estende.
 * <BR>Si consiglia di sviluppare le sottoclassi in modo che implementino il Pattern Singleton.
 * <BR>Inoltre, nel costruttore della sottoclasse, ricordarsi di invocare il costruttore della superclasse (<code>super()</code>).
 * @author Marco Ratto
 */
public class Config {
	
    private static final String CLASS_NAME = Config.class.getName();  
    private static Logger logger = Logger.getLogger(CLASS_NAME);

  /**
   * Contenitore per tutte le properties contenute nel file.
   */
  protected Properties prop = null;

  /**
   * Questo costruttore deve essere invocato nel costruttore di ogni sottoclasse.
   */
  protected Config() {
	final String METHOD_NAME = "Constructor";
    this.prop = new Properties();
    try {
		readFileProperties();
	} catch (IOException e) {
	    logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, e.getMessage(), e);        	
	}
  }

  public static Config getInstance() throws IOException {
	    if (instance == null) {
	      synchronized(Config.class) {
	        if (instance == null) {
	          instance = new Config();
	        }
	      }
	    }
	    return instance;
	  }
  
  /**
   * Il metodo ritorna la stringa contenente il <i>path</i> ed il nome del file di <code>properties</code>.
   * Il nome del file di properties e' determinato come segue:
   * <BR>- in prima istanza si cerca il path nella variabile di <i>JVM</i> con nome <b>infrastruttura_properties_path</b>;
   * <BR>- se la variabile non viene trovata, si cerca il path tramite una variabile di ambiente del <code>Context</code> sempre con nome <b>infrastruttura_properties_path</b>;
   * <BR>- se anche la variabile del <code>Context</code> non esiste, il path di default e' <b>/</b> (root del classloader).
   * <BR>- poi si cerca il filename nella variabile di <i>JVM</i> con nome <b>infrastruttura_properties_filename</b>;
   * <BR>- se la variabile non viene trovata, si cerca il filename tramite una variabile di ambiente del <code>Context</code> sempre con nome <b>infrastruttura_properties_filename</b>;
   * <BR>- se anche la variabile del <code>Context</code> non esiste, il filename di default e' <b>infrastruttura.properties</b>.
   * @return String filename
   */
  private String getPropertiesFilename() {
	final String METHOD_NAME = "getPropertiesFilename";
	
    String filename = null;

    // ricerca del filename...
    try {
      // ricerca del filepath come parametro della JVM (-d ...)
      filename = System.getProperty(this.getFilenamePropertyName());
      logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "filename=" + filename);        	
      if (filename == null) {
      	URL urlConfigFile = ServiceLocator.getInstance().getResourceURL(FILENAME_URL);
        logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "urlConfigFile=" + urlConfigFile);        	
        if (urlConfigFile == null)
          filename = this.getDefaultFileName();
      }
    }
    catch (Throwable e) {
      // in caso di errore di assegna il default
      filename = this.getDefaultFileName();
    }
    return filename;
  }


  /**
   * Acquisisce dal file di properties tutti i parametri, valorizzando l'attributo <code>prop</code>.
   * <BR>La lettura del file di properties avviene dapprima cercando un oggetto di tipo URL
   * all'indirizzo jdni <code>(java:comp/env)<nomejndi></code>. Se non si trova via URL allora
   * si cerca con il nome file passato attraverso i metodi abstract.
   * <BR>il path pu� essere relativo al Classloader oppure assoluto sul file system.
   * <BR>Prima si cerca nel classloader e poi nel file system.
   * @exception java.io.IOException
   */
  public void readFileProperties() throws IOException {
	final String METHOD_NAME = "getPropertiesFilename";
	
    //costruzione del nome del file di properties, necessario per leggere da file system.
    String filename = this.getPropertiesFilename();

    try { // ricerca del file via URL    	
      logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "readFileProperties(" + filename + "): looking for file by URL...");
      URL url = ServiceLocator.getInstance().getResourceURL(FILENAME_URL);
      logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking up java:comp/env... OK");
      logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking up " + url + "... OK");
      logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "object retrieved " + url.toString());
      readFileProperties(url);
    } catch(ServiceLocatorException sle) { // si � verificato un errore in fase di lookup JNDI
    	logger.logp(Level.WARNING, CLASS_NAME, METHOD_NAME, "errore in lookup. " + sle.getMessage());
      // ricerca del file tramite classloader
    	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking for file in classloader...");
      InputStream is = Config.class.getResourceAsStream(filename);
      if (is != null) {  // OK, file trovato
    	 logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME," looking for file in classloader... OK");
         this.readFileProperties(is);
      } else {
        // se non trovato si cerca su file system
        try {
          logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking for file in file system...");
          is = new FileInputStream(filename);
          logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking for file in file system... OK");
          this.readFileProperties(is); // prop.load(is);
        } catch (FileNotFoundException e) {
          logger.logp(Level.SEVERE, CLASS_NAME, METHOD_NAME, "errore in fase di lettura file. " + e.getMessage());
          throw new IOException(e.getMessage());
        }
      }
    } 
  }


  /**
   * Acquisisce dal file di properties tutti i parametri, valorizzando l'attributo <code>prop</code>.
   * <BR>Il file di properties e' passato in formato <code>String</code>;
   * <BR>il path puo' essere relativo al Classloader oppure assoluto sul file system.
   * <BR>Prima si cerca nel classloader e poi nel file system.
   * @param String filename
   * @exception java.io.IOException
   */
  public void readFileProperties(String filename) throws IOException {
	final String METHOD_NAME = "readFileProperties";
    
    logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking for file " + filename + " in file system...");
    InputStream is = Config.class.getResourceAsStream(filename);
    // se non trovato si cerca nel file system
    if (is == null) {
        is = new FileInputStream(filename);    	
    }
    prop.load(is);
    logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "looking for file " + filename + " in file system... OK");
  }



  /**
   * Acquisisce dal file di properties tutti i parametri, valorizzando l'attributo <code>prop</code>.
   * <BR>Il file di properties e' passato in formato <code>InputStream</code>.
   * @param InputStream is
   * @exception java.io.IOException
   */
  public void readFileProperties(InputStream is) throws IOException {
	final String METHOD_NAME = "readFileProperties";
	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "loading file...");
    prop.load(is);    
    logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "loading file... OK");
  }

  /**
   * Acquisisce dal file di properties tutti i parametri, valorizzando l'attributo <code>prop</code>.
   * <BR>Il file di properties e' passato in formato <code>File</code> ed il suo pathname deve essere assoluto partendo dalla root delle classi.
   * @exception java.io.IOException
   * @param File propFile
   */
  public void readFileProperties(File propFile) throws IOException {
    InputStream is = new FileInputStream(propFile);
    prop.load(is);
  }

  /**
   * Acquisisce dal file di properties tutti i parametri, valorizzando l'attributo <code>prop</code>.
   * <BR>Il file di properties e' passato in formato <code>URL</code>.
   * @exception java.io.IOException
   * @param URL propUrl
   */
  public void readFileProperties(URL url) throws IOException {
	final String METHOD_NAME = "readFileProperties";
	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "opening connection...");
    URLConnection connection = (URLConnection) url.openConnection();
    if (connection != null) {
    	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "opening connection... OK");    	
    } else {
    	logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "connection null");    	
    }
    logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "getting inputStream...");
    InputStream inputStream = connection.getInputStream();
    logger.logp(Level.INFO, CLASS_NAME, METHOD_NAME, "getting inputStream... OK");
    this.readFileProperties(inputStream); 
    inputStream.close();
  }

  /**
   * Il metodo restituisce il valore della property di formato <code>String</code> identificata dal parametro <code>key</code>. Se la property non esiste, il metodo ritorna <code>null</code>.
   * @param String key
   * @return String
   */
  public String getStringProperty(String key) {
    return prop.getProperty(key);
  }

  /**
   * Il metodo restituisce il valore della property di formato <code>String</code> identificata dal parametro <code>key</code>. Se la property non esiste, il metodo ritorna il parametro <code>defaultValue</code>.
   * @param String key
   * @param String defaultValue
   * @return String
   */
  public String getStringProperty(String key, String defaultValue) {
    return prop.getProperty(key, defaultValue);
  }

  /**
   * Imposta una nella properties una coppia <code>(key,value)</code>, con <code>value</code> di tipo <code>String</code>.
   * @throws EccezioneGrave
   * @param String key
   * @param String value
   */
  public void setStringProperty(String key, String value) {
    prop.setProperty(key, value);
  }

  /**
   * Il metodo restituisce il valore della property identificata dal parametro <code>key</code> nel formato <code>int</code>. Se la property non esiste, il metodo ritorna il parametro <code>defaultValue</code>.
   * @param String key
   * @param int defaultValue
   * @return int
   */
  public int getIntProperty(String key, int defaultValue) {
    int value = -1;
    String s = this.getStringProperty(key, null);

    try {
      value = Integer.parseInt(s);
    } catch (Exception nfe) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Imposta una nella properties una coppia <code>(key,value)</code>, con <code>value</code> di tipo <code>int</code>.
   * @param String key
   * @param int value
   */
  public void setIntProperty(String key, int value) {
    this.setStringProperty(key, "" + value);
  }

  /**
   * Il metodo restituisce il valore della property identificata dal parametro <code>key</code> nel formato <code>long</code>. Se la property non esiste, il metodo ritorna il parametro <code>defaultValue</code>.
   * @param String key
   * @param long defaultValue
   * @return long
   */
  public long getLongProperty(String key, long defaultValue) {
    long value = -1;
    String s = this.getStringProperty(key, null);

    try {
      value = Long.parseLong(s);
    }
    catch (Exception nfe) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Imposta una nella properties una coppia <code>(key, value)</code>, con <code>value</code> di tipo <code>long</code>.
   * @param String key
   * @param long value*/
  public void setLongProperty(String key, long value) {
    this.setStringProperty(key, "" + value);
  }

  /**
   * Il metodo restituisce il valore della property identificata dal parametro <code>key</code> nel formato <code>double</code>. Se la property non esiste, il metodo ritorna il parametro <code>defaultValue</code>.
   * @param String key
   * @param double defaultValue
   * @return double
   */
  public double getDoubleProperty(String key, double defaultValue) {
    double value = -1;
    String s = this.getStringProperty(key, null);

    try {
      value = Double.parseDouble(s);
    }
    catch (Exception nfe) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Imposta una nella properties una coppia <code>(key, value)</code>, con <code>value</code> di tipo <code>double</code>.
   * @param String key
   * @param double value
   */
  public void setDoubleProperty(String key, double value) {
    this.setStringProperty(key, "" + value);
  }

  /**
   * Il metodo restituisce il valore della property identificata dal parametro <code>key</code> nel formato <code>boolean</code>. Se la property non esiste, il metodo ritorna il parametro <code>defaultValue</code>.
   * @param String key
   * @param boolean defaultValue
   * @return boolean
   */
  public boolean getBooleanProperty(String key, boolean defaultValue) {
    boolean value = false;
    String s = this.getStringProperty(key, null);

    try {
      value = s.trim().toLowerCase().equals("true");
    }
    catch (Exception e) {
      value = defaultValue;
    }
    return value;
  }

  /**
   * Imposta una nella properties una coppia <code>(key, value)</code>, con <code>value</code> di tipo <code>boolean</code>.
   * @param String key
   * @param boolean value
   */
  public void setBooleanProperty(String key, boolean value) {
    this.setStringProperty(key, "" + value);
  }

  /**
   * Il metodo restituisce l'oggetto di tipo <code>java.util.Properties</code> nel quale sono contenute le properties.
   * @return Properties
   */
  protected Properties getProperties(){
    return this.prop;
  }

  protected String getFileURL() {
	    return FILENAME_URL;
	  }

	  protected String getDefaultFileName() {
		  String os = System.getProperty("os.name").toLowerCase();

		  if (os.indexOf( "win" ) >= 0) {
			  return "c:/" + DEFAULT_PROPERTIES_FILE_NAME;
		  } else if (os.indexOf( "mac" ) >= 0) {
			  return "/etc/" + DEFAULT_PROPERTIES_FILE_NAME;
		  } else if (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0) {
			  return "/etc/" + DEFAULT_PROPERTIES_FILE_NAME;
		  }
	    return null;
	  }

	  protected String getFilenamePropertyName() {
	    return FILENAME_PROPERTYNAME;
	  }	

	  private static Config instance = null;

	  // nome jndi del file di properties
	  public static final String FILENAME_URL = "url/JmsQueueToFileConfigFile";

	  // nome di default del file di properties
	  public static final String DEFAULT_PROPERTIES_FILE_NAME = "JmsQueueToFile.properties";

	  // nome della property contenente il nome del file di properties
	  private static final String FILENAME_PROPERTYNAME = "JmsQueueToFileConfigFile";
}
