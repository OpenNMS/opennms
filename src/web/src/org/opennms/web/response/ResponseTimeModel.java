//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
package org.opennms.web.response;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.PropertyConstants;
import org.opennms.netmgt.utils.IfLabel;

import org.opennms.web.Util;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.graph.*;


/**
 * Encapsulates all SNMP performance reporting for the web user interface.
 *
 * @author <a href="mailto:tarus@opennms.org">Tarus Balog</a>
 * @author <a href="mailto:seth@opennms.org">Seth Leger</a>
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class ResponseTimeModel extends Object
{
    public static final String RRDTOOL_GRAPH_PROPERTIES_FILENAME = "/etc/response-graph.properties";
    public static final String INTERFACE_GRAPH_TYPE = "interface";
    public static final String NODE_GRAPH_TYPE = "node";
    
    
    /** Convenience filter that matches directories with RRD files in them. 
     * 
     * @deprecated Replaced by 
     *     {@link org.opennms.netmgt.utils.RrdFileConstants#INTERFACE_DIRECTORY_FILTER}
     */
    public static final FileFilter INTERFACE_DIRECTORY_FILTER = new FileFilter() {
        public boolean accept(File file) {
            if(!file.isDirectory()) {
                return false;              
            }
                        
            File[] intfRRDs = file.listFiles(GraphUtil.RRD_FILENAME_FILTER);

            if(intfRRDs != null && intfRRDs.length > 0) {
                return true;
            }

            return false;
        }
    };    
    

    protected Properties props;
    protected PrefabGraph[] queries;
    protected Map reportMap;
    protected File rrdDirectory;
    protected String infoCommand;


    /**
    * Create a new instance.
    *
    * @param homeDir the OpenNMS home directory, see {@link Vault#getHomeDir
    * Vault.getHomeDir}.
    */
    public ResponseTimeModel( String homeDir ) throws IOException {
        if( homeDir == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        this.props = new java.util.Properties();
        this.props.load( new FileInputStream( homeDir + RRDTOOL_GRAPH_PROPERTIES_FILENAME ));

        this.rrdDirectory = new File( this.props.getProperty( "command.input.dir" ));
        this.infoCommand = this.props.getProperty( "info.command" );

        this.reportMap = PrefabGraph.getPrefabGraphDefinitions(props);        
    }

    
    public File getRrdDirectory() {
        return this.rrdDirectory;
    }
    
    
    public PrefabGraph getQuery(String queryName) {
        return (PrefabGraph)this.reportMap.get(queryName);
    }
    

    /** 
     * Return a list of all known prefabricated graph definitions.
     */
    public PrefabGraph[] getQueries() {
        if( this.queries == null ) {
            Collection values = this.reportMap.values();
            Iterator iter = values.iterator();
            
            PrefabGraph[] graphs = new PrefabGraph[values.size()];
            
            for(int i=0; i < graphs.length; i++) {
                graphs[i] = (PrefabGraph)iter.next();
            }
            
            this.queries = graphs;
        }

        return( this.queries );
    }


    public PrefabGraph[] getQueries(int nodeId) {
        return this.getQueries(String.valueOf(nodeId));
    }

    
    public PrefabGraph[] getQueries(String nodeId) {
        if(nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        //create a temporary list of queries to return
        ArrayList returnList = new ArrayList();

        //get the full list of all possible queries
        PrefabGraph[] queries = this.getQueries();

        //get all the data sources supported by node        
        List availDataSourceList = this.getDataSourceList(nodeId);

        //for each query, see if all the required data sources are available
        //in the available data source list, if so, add that query to the returnList
        for( int i=0; i < queries.length; i++ ) {
            List requiredList = Arrays.asList(queries[i].getColumns());

            if(availDataSourceList.containsAll(requiredList)) {
                returnList.add(queries[i]);   
            }
        }

        //put the queries in returnList into an array
        PrefabGraph[] availQueries = (PrefabGraph[])returnList.toArray(new PrefabGraph[returnList.size()]);

        return availQueries;
    }


    public PrefabGraph[] getQueries(int nodeId, String intf, boolean includeNodeQueries) {
        return this.getQueries(String.valueOf(nodeId), intf, includeNodeQueries);
    }

    
    public PrefabGraph[] getQueries(String nodeId, String intf, boolean includeNodeQueries) {
        if(nodeId == null || intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        //create a temporary list of queries to return
        ArrayList returnList = new ArrayList();

        //get the full list of all possible queries
        PrefabGraph[] queries = this.getQueries();

        //get all the data sources supported by this interface (and possibly node)        
        List availDataSourceList = this.getDataSourceList(nodeId, intf, includeNodeQueries);

        //for each query, see if all the required data sources are available
        //in the available data source list, if so, add that query to the returnList
        for( int i=0; i < queries.length; i++ ) {
            List requiredList = Arrays.asList(queries[i].getColumns());

            if(availDataSourceList.containsAll(requiredList)) {
                returnList.add(queries[i]);   
            }
        }

        //put the queries in returnList into an array
        PrefabGraph[] availQueries = (PrefabGraph[])returnList.toArray(new PrefabGraph[returnList.size()]);

        return availQueries;
    }

    
    public String[] getDataSources(int nodeId) {
        return this.getDataSources(String.valueOf(nodeId));
    }
    

    public String[] getDataSources(String nodeId) {
        List dataSourceList = this.getDataSourceList(String.valueOf(nodeId));
        String[] dataSources = (String[])dataSourceList.toArray(new String[dataSourceList.size()]);

        return dataSources;        
    }
    
        
    public String[] getDataSources(int nodeId, String intf, boolean includeNodeQueries) {
        return this.getDataSources(String.valueOf(nodeId), intf, includeNodeQueries);
    }
    

    public String[] getDataSources(String nodeId, String intf, boolean includeNodeQueries) {
        List dataSourceList = this.getDataSourceList(String.valueOf(nodeId), intf, includeNodeQueries);
        String[] dataSources = (String[])dataSourceList.toArray(new String[dataSourceList.size()]);

        return dataSources;        
    }
    
        
    public List getDataSourceList(int nodeId) {
        return this.getDataSourceList(String.valueOf(nodeId));
    }
    

    public List getDataSourceList(String nodeId) {
        if(nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        ArrayList dataSources = new ArrayList();        
        File nodeDir = new File(this.rrdDirectory, nodeId);        
        int suffixLength = GraphUtil.RRD_SUFFIX.length();
        
        //get the node data sources
        File[] nodeFiles = nodeDir.listFiles(GraphUtil.RRD_FILENAME_FILTER);
        
        for(int i = 0; i < nodeFiles.length; i++ ) {
            String fileName = nodeFiles[i].getName();
            String dsName = fileName.substring(0, fileName.length() - suffixLength);
            
            dataSources.add(dsName); 
        }
    
        return dataSources;
    }    

    
    public List getDataSourceList(int nodeId, String intf, boolean includeNodeQueries) {
        return this.getDataSourceList(String.valueOf(nodeId), intf, includeNodeQueries);
    }
    

    public List getDataSourceList(String nodeId, String intf, boolean includeNodeQueries) {
        if(nodeId == null || intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        ArrayList dataSources = new ArrayList();
        
        //File nodeDir = new File(this.rrdDirectory, nodeId);
        File intfDir = new File(this.rrdDirectory, intf);
        
        int suffixLength = GraphUtil.RRD_SUFFIX.length();
        
        //get the node data sources
        //if(includeNodeQueries) {            
         //   dataSources.addAll(this.getDataSourceList(nodeId));
        //}
        
        //get the interface data sources
        File[] intfFiles = intfDir.listFiles(GraphUtil.RRD_FILENAME_FILTER);
        
        for(int i = 0; i < intfFiles.length; i++ ) {
            String fileName = intfFiles[i].getName();
            String dsName = fileName.substring(0, fileName.length() - suffixLength);
            
            dataSources.add(dsName); 
        }        
        
        return dataSources;
    }
    

    /**
     * Return a human-readable description (usually an IP address or hostname) 
     * for the interface given.
    */
    public String getHumanReadableNameForIfLabel(int nodeId, String ifLabel)
        throws SQLException
    {
        if (nodeId < 1)
        {
            throw new IllegalArgumentException("Illegal nodeid encountered when looking for performance information: \"" + String.valueOf(nodeId) + "\"");
        }
        if (ifLabel == null)
        {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }        
        
        // Retrieve the extended information for this nodeid/ifLabel pair
        Map intfMap = IfLabel.getInterfaceInfoFromIfLabel(nodeId, ifLabel);

        String descr = new String();

        // If there is no extended information, the ifLabel is not associated with a
        // current SNMP interface.
        if (intfMap.size() < 1)
        {
            descr = ifLabel;
        }
        // Otherwise, add the extended information to the description
        else
        {
            String parenString = new String();
            if ((intfMap.get("ipaddr") != null) && !((String)intfMap.get("ipaddr")).equals("0.0.0.0"))
            {
                parenString += (String)intfMap.get("ipaddr");
            }
            if ((intfMap.get("snmpifspeed") != null) && (Integer.parseInt((String)intfMap.get("snmpifspeed")) != 0))
            {
                String speed = Util.getHumanReadableIfSpeed(Integer.parseInt((String)intfMap.get("snmpifspeed")));
                parenString += ((parenString.equals("")) ? speed : (", " + speed));
            }

            if (intfMap.get("snmpifname") != null)
            {
                descr = (String)intfMap.get("snmpifname");
            }
            else if (intfMap.get("snmpifdescr") != null)
            {
                descr = (String)intfMap.get("snmpifdescr");
            }
            else
            {
                // Should never reach this point, since ifLabel
                // is based on the values of ifName and ifDescr
                // but better safe than sorry.
                descr = ifLabel;
            }

            // Add the extended information in parenthesis after the ifLabel,
            // if such information was found
            descr = descr + ((parenString.equals("")) ? "" : (" (" + parenString + ")"));
        }

        return descr;
    }


    /** Convenient data structure for storing nodes with RRDs available. */
    public static class QueryableNode extends Object {
        public int nodeId;
        public String nodeLabel;
    }

    
    /**
    * Returns a list of data structures representing the nodes that have
    * SNMP performance data collected.
    *
    * <p>First the list of RRD files is collected.  From those filenames, the
    * IP address is extracted from each.  A list of unique IP addresses is created,
    * discarding the duplicates.  At the same time, a mapping of unique IP address
    * to RRD files is created.  Then a database call is made to determine the node
    * identifier and human-readable label for each node containing the IP addresses.
    * From that list, an array of data structures, <code>QueryableNode</code>s, are
    * created.</p>
    */
    public QueryableNode[] getQueryableNodes() throws SQLException {
        QueryableNode[] nodes = new QueryableNode[0];

        // Get all of the numeric directory names in the RRD directory; these
        // are the nodeids of the nodes that have performance data
        File[] intDirs = this.rrdDirectory.listFiles(INTERFACE_DIRECTORY_FILTER);

        if(intDirs != null && intDirs.length > 0) {
            ArrayList nodeList = new ArrayList();

            //create the main stem of the select statement
            StringBuffer select = new StringBuffer("SELECT DISTINCT ipinterface.nodeid, node.nodeLabel FROM node, ipinterface WHERE node.nodetype != 'D' AND ipinterface.nodeid=node.nodeid AND ipinterface.ipaddr IN ('");

            //add all but the last node id with a comma
            for( int i=0; i < intDirs.length - 1; i++ ) {
                select.append(intDirs[i].getName());
                select.append("','");
            }

            //add the last node id without a comma
            select.append(intDirs[intDirs.length - 1].getName());

            //close the select
            select.append("') ORDER BY node.nodeLabel");

            Connection conn = Vault.getDbConnection();

            try {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(select.toString());


                while( rs.next() ) {
                    QueryableNode node = new QueryableNode();

                    node.nodeId = rs.getInt("nodeid");
                    node.nodeLabel = rs.getString("nodeLabel");

                    nodeList.add(node);
                }

                rs.close();
                stmt.close();
            }
            finally {
                Vault.releaseDbConnection(conn);
            }

            nodes = (QueryableNode[])nodeList.toArray(new QueryableNode[nodeList.size()]);
        }

        return( nodes );
    }

    
    public ArrayList getQueryableInterfacesForNode(int nodeId) 
        throws SQLException
    {
        return this.getQueryableInterfacesForNode(String.valueOf(nodeId));
    }

    
    public ArrayList getQueryableInterfacesForNode(String nodeId) 
        throws SQLException
    {
        if(nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        //create the main stem of the select statement
        StringBuffer select = new StringBuffer("SELECT DISTINCT ipaddr FROM ipinterface WHERE nodeid=");

        select.append(nodeId);

        //close the select
        select.append(" ORDER BY ipaddr");

        Connection conn = Vault.getDbConnection();
	ArrayList intfs = new ArrayList();

        try 
        {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(select.toString());

            while( rs.next() ) 
            {
	        String ipAddr = rs.getString("ipaddr");
                File intfDir = new File(this.rrdDirectory, ipAddr);        
        
                if(intfDir.exists() && intfDir.isDirectory()) 
		{
                    intfs.add(ipAddr);
                }
            }
            rs.close();
            stmt.close();
        }
        finally 
        {
            Vault.releaseDbConnection(conn);
        }

        return intfs;
    }
        


    public boolean isQueryableNode(int nodeId) {
        return this.isQueryableNode(String.valueOf(nodeId));
    }

    
    public boolean isQueryableNode(String nodeId) {
        if(nodeId == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        boolean isQueryable = false;        
        File nodeDir = new File(this.rrdDirectory, nodeId);        
        
        if(nodeDir.exists() && nodeDir.isDirectory()) {
            File[] nodeFiles = nodeDir.listFiles(GraphUtil.RRD_FILENAME_FILTER);
            
            if(nodeFiles != null && nodeFiles.length > 0) {
                isQueryable = true;
            }
            else {                         
                File[] intfDirs = nodeDir.listFiles(INTERFACE_DIRECTORY_FILTER);
                
                if(intfDirs != null && intfDirs.length > 0) {
                    isQueryable = true;                
                }
            }            
        }
        
        return isQueryable;
    }

    
    public boolean isQueryableInterface(int nodeId, String ifLabel) {
        if(ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        boolean isQueryable = false;        
        File nodeDir = new File(this.rrdDirectory, String.valueOf(nodeId));        
        
        if(nodeDir.exists() && nodeDir.isDirectory()) {            
            File intfDir = new File(nodeDir, ifLabel);
            
            if(intfDir.exists() && intfDir.isDirectory()) {
                File[] intfFiles = intfDir.listFiles(GraphUtil.RRD_FILENAME_FILTER);
                
                if(intfFiles != null && intfFiles.length > 0) {
                    isQueryable = true;
                }
            }
        }
        
        return isQueryable;        
    }
    
    
    public boolean isQueryableInterface(String nodeId, String ifLabel) {
        if(nodeId == null || ifLabel == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        return this.isQueryableInterface(Integer.parseInt(nodeId), ifLabel);
    }
        
}

