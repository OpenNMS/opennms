package org.opennms.web.map;
/*
 * Created on 8-giu-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.asset.Asset;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.category.CategoryModel;
import org.opennms.web.element.DataLinkInterface;
import org.opennms.web.element.ExtendedNetworkElementFactory;
import org.opennms.web.element.*;
import org.opennms.web.element.Node;
import org.opennms.web.outage.OutageModel;
import org.opennms.web.outage.OutageSummary;
import java.util.*;
/**
 * @author mmigliore
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoadNodesServlet extends HttpServlet
{
	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	Category log;
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        log = ThreadCategory.getInstance(this.getClass());
        
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
	    log.info("Loading nodes");
	    String strToSend="loadNodesOK";
    
	    Node[] onmsNodes = null;
        OutageModel oModel = new OutageModel();
        Vector nodes = new Vector();
        Vector outages = new Vector();

        try {
            OutageSummary[] summaries = oModel.getCurrentOutageSummaries();
            for (int i = 0; i < summaries.length; i++) {
                OutageSummary summary = summaries[i];
                int nodeId = summary.getNodeId();
                outages.add(new Integer(nodeId));
            }
        } catch (Exception e) {
            
            log.error(e.toString());
        }

        CategoryModel cModel;

        AssetModel aModel = new AssetModel();
        try {
            onmsNodes = NetworkElementFactory.getAllNodes();
        } catch (SQLException e) {
            log.error(e.toString());
        }


        Asset[] assetarray = null;
        try {
            assetarray = aModel.getAllAssets();
        } catch (Exception e) {
            log.error(e.toString());
        }

        Hashtable assets = new Hashtable();

        for (int i = 0; i < assetarray.length; i++) {
            Asset a = assetarray[i];
            assets.put(new Integer(a.getNodeId()), a);
        }

        for (int i = 0; i < onmsNodes.length; i++) {
            Node n = onmsNodes[i];
           
            Asset asset;
            double overallRtcValue = 0.0;
            boolean isNew = false;

            if (assets.containsKey(new Integer(n.getNodeId()))) {
                asset = (Asset) assets.get(new Integer(n.getNodeId()));
            } else {
                asset = new Asset();
                isNew = true;
            }

            try {
                cModel = CategoryModel.getInstance();
                overallRtcValue = cModel.getNodeAvailability(n.getNodeId());
            } catch (Exception e) {
                log.error(e.toString());
            }
          try{
            if(i>0){
    			strToSend+="**";
    		}
            String icon="unspecified";
            if (!isNew) {
                icon=asset.getCategory().toLowerCase();
            }
            String status="O";		
            if (!outages.contains(new Integer(n.getNodeId()))) {
                status=""+n.getNodeType();
            }
            
            //constructs a list with the primary ip address (if exists) as first element 
            Interface[] ifaces= NetworkElementFactory.getAllInterfacesOnNode(n.getNodeId());
            String ipPrimaryAddress="";
            
            for(int j=0;j<ifaces.length;j++){
            	if(ifaces[j].getIsSnmpPrimary()=="P"){
            		ipPrimaryAddress=ifaces[j].getIpAddress();
            		}
            }
            
			List interfaces = new ArrayList();
            if(ipPrimaryAddress!=""){
            	interfaces.add(ipPrimaryAddress);
            }
            for(int j=0;j<ifaces.length;j++){
            	if(!interfaces.contains(ifaces[j].getIpAddress())){
            		interfaces.add(ifaces[j].getIpAddress());
            		}
            }
            
            
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(3);
			String nodeStr=n.getNodeId()+"@@"+n.getLabel()+"@@"+df.format(overallRtcValue).replace(',','.')+"@@"+status+"@@"+icon+"@@";
	    	for(int j=0;j<interfaces.size();j++){
	    		nodeStr+=interfaces.get(j)+"@@";
	    	}
	    	//end of ip addresses = ^^
	    	nodeStr+="^^";
			DataLinkInterface[] links = ExtendedNetworkElementFactory.getDataLinks(n.getNodeId());
	    	if(links!=null){
	        	for(int j=0;j<links.length;j++){
	    	    	DataLinkInterface dli= links[j];
	    	    	int id=dli.get_nodeparentid();
    	    		nodeStr+="@@";
	    	    	nodeStr+=id;
	        	}
	    	}
	    	links = ExtendedNetworkElementFactory.getDataLinksFromNodeParent(n.getNodeId());
	    	if(links!=null){
	        	for(int j=0;j<links.length;j++){
	    	    	DataLinkInterface dli= links[j];
	    	    	int id=dli.get_nodeId();
    	    		nodeStr+="@@";	    	    	
	    	    	nodeStr+=id;
	    	    	
	        	}
	    	}			
            strToSend+=nodeStr;
	      }catch(Exception e){
	        	log.error(e);
	      }
        }

      bw.write(strToSend);
      bw.close();
      log.info("Sending response to the client '"+strToSend+"'");

    }

    public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException 
    {
      doPost(request,response); 	
    }
    
    private String nodeToString(Node node)throws Exception{
    	String strToSend=node.getNodeId()+"@@"+node.getLabel()+"@@";
    	DataLinkInterface[] links = ExtendedNetworkElementFactory.getDataLinks(node.getNodeId());
    	if(links!=null){
        	for(int i=0;i<links.length;i++){
    	    	DataLinkInterface dli= links[i];
    	    	int id=dli.get_nodeparentid();
    	    	strToSend+=id+"@@";
    	    	
        	}
    	}
    	links = ExtendedNetworkElementFactory.getDataLinksFromNodeParent(node.getNodeId());
    	if(links!=null){
        	for(int i=0;i<links.length;i++){
    	    	DataLinkInterface dli= links[i];
    	    	int id=dli.get_nodeId();
    	    	strToSend+=id+"@@";
    	    	
        	}
    	}
    	return strToSend;
    }
}
