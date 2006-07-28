package org.opennms.web.map;

/*
 * Created on 8-giu-2005
 *
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.web.asset.Asset;
import org.opennms.web.asset.AssetModel;
import org.opennms.web.element.Interface;
import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.map.config.MapPropertiesFactory;


/**
 * @author mmigliore
 *
 */
public class LoadInfosServlet extends HttpServlet {

	private static final String LOG4J_CATEGORY = "OpenNMS.Map";
	
	private static final String LOAD_NODES_INFO_ACTION = "LoadNodesInfo";

	Category log;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
		log = ThreadCategory.getInstance(this.getClass());

		String action = request.getParameter("action");
		
		String elem = request.getParameter("elem");
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(response
				.getOutputStream()));
		
		String strToSend = action + "OK";
		
		log.info("Loading infos");

		
		if(action.equals(LOAD_NODES_INFO_ACTION)){
			AssetModel am = new AssetModel();
			SimpleDateFormat formatter = new SimpleDateFormat("HH.mm.ss dd/MM/yy");
			MapPropertiesFactory.init();
			MapPropertiesFactory mpf = MapPropertiesFactory.getInstance();
			List af = mpf.getAssetFields();
			Asset nodeAsset =null;
			Interface[] iface=null;
			try{
				nodeAsset = am.getAsset(Integer.parseInt(elem));
				iface = NetworkElementFactory.getAllInterfacesOnNode(Integer.parseInt(elem));
			}catch(SQLException e){
				strToSend = action + "Failed";
			}
			String  ipAddress="-";
			if(iface!=null){
				if(iface.length>1){
					for( int i=0; i < iface.length; i++ ) { 
		                 if( "P".equals( iface[i].getIsSnmpPrimary() )){
		                 	ipAddress=iface[i].getIpAddress();
		                 	break;
		                 }
					}
				}else{
					ipAddress=iface[0].getIpAddress();
				}
			}
			strToSend+="+Ip: "+ipAddress;
			if(nodeAsset!=null){
				for(int i=0; i<af.size();i++){
					String assetField = (String)af.get(i);
					String assetValue=null;
					if(assetField.equalsIgnoreCase("category")){
						assetValue=nodeAsset.getCategory();
					}else 
						if(assetField.equalsIgnoreCase("manufacturer")){
							assetValue=nodeAsset.getManufacturer();
						}else 			if(assetField.equalsIgnoreCase("vendor")){
							assetValue=nodeAsset.getVendor();
						}else 			if(assetField.equalsIgnoreCase("modelnumber")){
							assetValue=nodeAsset.getModelNumber();
						}else 			if(assetField.equalsIgnoreCase("serialnumber")){
							assetValue=nodeAsset.getSerialNumber();
						}else 			if(assetField.equalsIgnoreCase("description")){
							assetValue=nodeAsset.getDescription();
						}else 			if(assetField.equalsIgnoreCase("circuitid")){
							assetValue=nodeAsset.getCircuitId();
						}else 			if(assetField.equalsIgnoreCase("assetnumber")){
							assetValue=nodeAsset.getAssetNumber();
						}else 			if(assetField.equalsIgnoreCase("operatingsystem")){
							assetValue=nodeAsset.getOperatingSystem();
						}else 			if(assetField.equalsIgnoreCase("rack")){
							assetValue=nodeAsset.getRack();
						}else 			if(assetField.equalsIgnoreCase("slot")){
							assetValue=nodeAsset.getSlot();
						}else 			if(assetField.equalsIgnoreCase("port")){
							assetValue=nodeAsset.getPort();
						}else 			if(assetField.equalsIgnoreCase("region")){
							assetValue=nodeAsset.getRegion();
						}else 			if(assetField.equalsIgnoreCase("division")){
							assetValue=nodeAsset.getDivision();
						}else 			if(assetField.equalsIgnoreCase("department")){
							assetValue=nodeAsset.getDepartment();
						}else 			if(assetField.equalsIgnoreCase("address1")){
							assetValue=nodeAsset.getAddress1();
						}else 			if(assetField.equalsIgnoreCase("address2")){
							assetValue=nodeAsset.getAddress2();
						}else 			if(assetField.equalsIgnoreCase("city")){
							assetValue=nodeAsset.getCity();
						}else 			if(assetField.equalsIgnoreCase("state")){
							assetValue=nodeAsset.getState();
						}else 			if(assetField.equalsIgnoreCase("zip")){
							assetValue=nodeAsset.getZip();
						}else 			if(assetField.equalsIgnoreCase("building")){
							assetValue=nodeAsset.getBuilding();
						}else 			if(assetField.equalsIgnoreCase("floor")){
							assetValue=nodeAsset.getFloor();
						}else 			if(assetField.equalsIgnoreCase("room")){
							assetValue=nodeAsset.getRoom();
						}else 			if(assetField.equalsIgnoreCase("vendorphone")){
							assetValue=nodeAsset.getVendorPhone();
						}else 			if(assetField.equalsIgnoreCase("vendorfax")){
							assetValue=nodeAsset.getVendorFax();
						}else 			if(assetField.equalsIgnoreCase("vendorassetnumber")){
							assetValue=nodeAsset.getVendorAssetNumber();
						}else 			if(assetField.equalsIgnoreCase("userlastmodified")){
							assetValue=nodeAsset.getUserLastModified();
						}else 			if(assetField.equalsIgnoreCase("lastmodifieddate")){
							assetValue=((nodeAsset.getLastModifiedDate()!=null)?formatter.format(nodeAsset.getLastModifiedDate()):"");
						}else 			if(assetField.equalsIgnoreCase("dateinstalled")){
							assetValue=nodeAsset.getDateInstalled();
						}else 			if(assetField.equalsIgnoreCase("lease")){
							assetValue=nodeAsset.getLease();
						}else 			if(assetField.equalsIgnoreCase("leaseexpires")){
							assetValue=nodeAsset.getLeaseExpires();
						}else 			if(assetField.equalsIgnoreCase("supportphone")){
							assetValue=nodeAsset.getSupportPhone();
						}else 			if(assetField.equalsIgnoreCase("maintcontract")){
							assetValue=nodeAsset.getMaintContract();
						}else 			if(assetField.equalsIgnoreCase("displaycategory")){
							assetValue=nodeAsset.getDisplayCategory();
						}else 			if(assetField.equalsIgnoreCase("notifycategory")){
							assetValue=nodeAsset.getNotifyCategory();
						}else 			if(assetField.equalsIgnoreCase("pollercategory")){
							assetValue=nodeAsset.getPollerCategory();
						}else 			if(assetField.equalsIgnoreCase("maintcontractexpires")){
							assetValue=nodeAsset.getMaintContractExpires();
						}else 			if(assetField.equalsIgnoreCase("thresholdcategory")){
							assetValue=nodeAsset.getThresholdCategory();
						}
					strToSend+="+"+assetField+": "+((assetValue!=null && !assetValue.trim().equals(""))?assetValue:"-");
					}		
				}

			
			
				
			}else{
				strToSend = action + "Failed";
			}
				
		bw.write(strToSend);
		bw.close();
		log.info("Sending response to the client '" + strToSend + "'");

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}