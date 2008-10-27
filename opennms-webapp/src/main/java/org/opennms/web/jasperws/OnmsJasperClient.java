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

import java.io.Serializable;
import java.util.List;

import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
import com.jaspersoft.jasperserver.irplugin.JServer;

/**
 * This class realise the JasperServer-Webserviceclient. Connect to a
 * JasperServer and use the Webservicelclient to get all available Reports.
 * 
 * @author <a href="mailto:r.trommer@open-factory.org">Ronny Trommer</a>
 */
public class OnmsJasperClient implements Serializable
{
  /** Serial ID */
  private static final long serialVersionUID = 1467563234386610230L;

  /** JasperServer with his webservice */
  private JServer m_jserver;

  /** Reportdescriptor to request reportunits from webserviceclient */
  private ResourceDescriptor m_rd;

  /** List with all reportobjects */
  private List<ResourceDescriptor> m_descriptorList;

  /** Configuration-Parameter from jasperclient.enable */
  private JasperConfiguration m_jconfig;

  /**
   * Client can only instantiate with configuration.
   * 
   * @throws Exception
   *           JasperConfiguration
   */
  public OnmsJasperClient () throws Exception
  {
    /** Your JasperServer */
    this.m_jserver = new JServer ();

    /** Load the configuration */
    this.m_jconfig = new JasperConfiguration ();

    /** Set username, password, url from configurationfile */
    this.m_jserver.setUsername (this.m_jconfig.getJasperUser ());
    this.m_jserver.setPassword (this.m_jconfig.getJasperPassword ());
    this.setReportUrl (this.m_jconfig.getRepositoryUrl ());

    /** Descriptor to retrieve reportunits from JasperServer */
    this.m_rd = new ResourceDescriptor ();
    this.m_rd.setWsType (ResourceDescriptor.TYPE_FOLDER);
    this.m_rd.setUriString (this.m_jconfig.getReportBase ());
  }

  /**
   * Method to get all resources from JasperServer.
   * 
   * @return List Resources from JasperServer
   */
  @SuppressWarnings ("unchecked")
  public List<ResourceDescriptor> getDescriptorList ()
  {
    /*
     * Try to connect to JasperServer with given configuration.
     */
    try
    {
      this.m_descriptorList = this.m_jserver.getWSClient ().list (this.m_rd);
    } catch (Exception e)
    {
      /* Connection failed, user, password, baseurl wrong */
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return this.m_descriptorList;
  }
  
  /**
   * Method to get all resources from JasperServer.
   * 
   * @param folder String folder
   * @return List Resources from JasperServer
   */
  @SuppressWarnings ("unchecked")
  public List<ResourceDescriptor> getDescriptorList (String folder)
  {
    this.setReportUri (folder);
    
    /*
     * Try to connect to JasperServer with given configuration.
     */
    try
    {
      this.m_descriptorList = this.m_jserver.getWSClient ().list (this.m_rd);
    } catch (Exception e)
    {
      /* Connection failed, user, password, baseurl wrong */
      // TODO Auto-generated catch block
      e.printStackTrace ();
    }
    return this.m_descriptorList;
  }
  
  /**
   * Method to set the repository folder
   * 
   * @param reportUrl
   *          Folder to list
   */
  public void setReportUrl (String reportUrl)
  {
    this.m_jserver.setUrl (reportUrl);
  }

  /**
   * Method to get the repository Folder
   * 
   * @return String current Folder
   */
  public String getReportUrl ()
  {
    return this.m_jserver.getUrl ();
  }

  /**
   * Set the folder to list
   * 
   * @param reportFolder
   *          folder to list
   */
  public void setReportUri (String reportFolder)
  {
    this.m_rd.setUriString (reportFolder);
  }

  /**
   * Get the requested folder
   * 
   * @return String requested folder
   */
  public String getReportUri ()
  {
    return this.m_rd.getUriString ();
  }

  /**
   * Get parent-folder
   * 
   * @return String parent-folder
   */
  public String getParentFolder ()
  {
    return this.m_rd.getParentFolder ();
  }

  /**
   * Get name from ReportUnit
   * 
   * @return String Name of ReportUnit
   */
  public String getName ()
  {
    return this.m_rd.getName ();
  }

  /**
   * Check if ReportUnit has parents
   * 
   * @return boolean has parent folder 
   */
  public boolean hasParentFolder ()
  {
    boolean hasParent = false;
    if (!this.m_rd.getParentFolder ().isEmpty ())
    {
      hasParent = false;
    } else
    {
      hasParent = true;
    }
    return hasParent;
  }
  
  /**
   * Get the report-root directory from configuration
   * 
   * @return String Report-root directory
   */
  public String getReportBase ()
  {
    String reportBase = "<Empty-Report-Base";
    try
    {
      reportBase = this.m_jconfig.getReportBase ();
    } catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return reportBase;
  }
}
