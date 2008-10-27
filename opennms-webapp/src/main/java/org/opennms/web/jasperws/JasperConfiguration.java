//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.web.jasperws;

/**
 * This class reads the configuration file and has get-Methods to retrieve all
 * required values for the webserviceclient.
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a>
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class JasperConfiguration
{
  /**
   * Map contains all configuration parameter and values
   */
  private Map<String, String> m_config = new HashMap<String, String> ();

  /**
   * Create the JasperConfiguration. The file should be found in
   * $OPENNMS_HOME/etc
   * 
   * @throws Exception
   *           Configuration file do not exist
   */
  public JasperConfiguration () throws Exception
  {
    /**
     * Initialize Tokenizer to determine parameter or value for a single line in
     * the configuration file.
     */
    StringTokenizer token = null;

    /** File reader for the configuration file */
    BufferedReader fileReader = null;

    /** the current line while reading */
    String cfgLine = "";

    /** Initialize an empty parameter */
    String param = "";

    /** Initialize an empty value */
    String value = "";

    /*
     * File "$OPENNMS_HOME/etc/jasperclient.enable"
     */
    File configFile = new File (System
        .getenv (JasperClientConstants.OPENNMS_ENV)
        + "/" + JasperClientConstants.CONFIG_FILE);
    // File configFile = new File (
    // "/Users/indigo/Projects/Develop/Java-Workspace/OpenNMS-JasperServer-Integration/"
    // + JasperClientConstants.CONFIG_FILE);

    /*
     * If the configfile does not exist do nothing.
     */
    if (configFile.exists ())
    {

      /*
       * Try to read the configurationfile for url, user, password, reportbase
       */
      try
      {
        fileReader = new BufferedReader (new InputStreamReader (
            new FileInputStream (configFile)));

        /*
         * for each line in configuration file
         */
        while ((cfgLine = fileReader.readLine ()) != null)
        {
          /*
           * check if the line in config is comment and contains the delimiter
           * for parameter and value.
           */
          if (!cfgLine.startsWith (JasperClientConstants.CONFIG_COMMENT)
              && cfgLine.contains (JasperClientConstants.CONFIG_DELIMITER))
          {
            /*
             * Build a token between '=' for parameter and value
             */
            token = new StringTokenizer (cfgLine,
                JasperClientConstants.CONFIG_DELIMITER);

            /*
             * Assign current parameter and value
             */
            param = token.nextToken ();
            value = token.nextToken ();

            /*
             * Put config-parameter and values in a keyed hashmap
             */
            this.m_config.put (param, value);
          }
          ;
        }
        ;
      } catch (FileNotFoundException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      } catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace ();
      }
      ;
    } else
    {
      throw new Exception ("Configurationfile: " + configFile
          + " do not exist!");
    }
  };

  /**
   * Get the repository-url from configuration file
   * 
   * @return String Repository-URL
   * @throws Exception
   *           Configuration-Key not found
   */
  public String getRepositoryUrl () throws Exception
  {
    if (this.m_config.containsKey (JasperClientConstants.CONFIG_URL))
    {
      return this.m_config.get (JasperClientConstants.CONFIG_URL);
    } else
    {
      throw new Exception ("Configuration " + JasperClientConstants.CONFIG_URL
          + " not found!");
    }
  };

  /**
   * Get the basedirectory for the reports
   * 
   * @return String Repository-basedirectory
   * @throws Exception
   *           Access to Reportbase-directory failed
   */
  public String getReportBase () throws Exception
  {
    if (this.m_config.containsKey (JasperClientConstants.CONFIG_REPORT_BASE))
    {
      return this.m_config.get (JasperClientConstants.CONFIG_REPORT_BASE);
    } else
    {
      throw new Exception ("Configuration "
          + JasperClientConstants.CONFIG_REPORT_BASE + " not found!");
    }
  };

  /**
   * Get the JasperServer from configuration file to access the
   * JasperServer-Repository
   * 
   * @return String Repository-URL
   * @throws Exception
   *           Configuration-Key not found
   */
  public String getJasperUser () throws Exception
  {
    if (this.m_config.containsKey (JasperClientConstants.CONFIG_USER))
    {
      return this.m_config.get (JasperClientConstants.CONFIG_USER);
    } else
    {
      throw new Exception ("Configuration " + JasperClientConstants.CONFIG_USER
          + " not found!");
    }
  };

  /**
   * Get the clear password to access JasperServer-Repository from configuration
   * file.
   * 
   * @return String JasperServer-Password
   * @throws Exception
   *           Configuration-Key not found
   */
  public String getJasperPassword () throws Exception
  {
    if (this.m_config.containsKey (JasperClientConstants.CONFIG_PASS))
    {
      return this.m_config.get (JasperClientConstants.CONFIG_PASS);
    } else
    {
      throw new Exception ("Configuration " + JasperClientConstants.CONFIG_PASS
          + " not found!");
    }
  };
};
