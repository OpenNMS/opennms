/*
 * Created on 13-ott-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.inventory;

import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.inventory.*;
import org.opennms.netmgt.inventory.UnparsableConfigurationException;
import java.util.Map;
import java.io.IOException;
import java.util.Enumeration;
/**
 * @author maurizio
 */
public final class InventoryVisualization {
	private String inventoryCategory;
	private String pathFile;
	private String visualType;

    private InventoryVisualization(){
    	
    }
    
    public InventoryVisualization(String inventoryCategory, String pathFile)throws IOException{
    	this.inventoryCategory=inventoryCategory;
    	this.pathFile=pathFile;

		try{
			InventoryConfigFactory.reload();
		}catch(Exception e){
			throw new IOException(e.toString());
		}
		
		InventoryConfiguration iConfig = InventoryConfigFactory.getInstance().getConfiguration();
		Enumeration enumMonitors = iConfig.enumerateMonitor();
		while(enumMonitors.hasMoreElements()){
			Monitor monit = (Monitor) enumMonitors.nextElement();
			if(monit.getInventoryType().equals(inventoryCategory)){
				visualType = monit.getVisualization();
				break;
			}
		}
    }
    
	public  String getVisualization(Map parameters) throws IOException, UnparsableConfigurationException, InstantiationException, ClassNotFoundException, IllegalAccessException{
		String htmlStr="";
		if(visualType!= null){
			String className=null;
			InventoryConfiguration iConfig = InventoryConfigFactory.getInstance().getConfiguration();
			Enumeration enumVisual = iConfig.enumerateVisualization();
			while(enumVisual.hasMoreElements()){
				org.opennms.netmgt.config.inventory.Visualization currVisual = (org.opennms.netmgt.config.inventory.Visualization) enumVisual.nextElement();
				if(currVisual.getType().equals(visualType)){
					className = currVisual.getClassName();
				}
				
			}
			if(className!=null){
				Class vis = Class.forName(className);
				Visualization visual = (Visualization)vis.newInstance();
				htmlStr = visual.getVisualization(pathFile, parameters);
			}
			else{
				throw new IOException("Visualization with type ="+ visualType +" not found in configuration file: inventory-configuration.xml");
			}
		}
		else{
			throw new IOException("Inventory category "+ inventoryCategory +" not found in configuration file: inventory-configuration.xml");
		}
		return htmlStr;
	}
	/**
	 * @return
	 */
	public String getVisualType() {
		return visualType;
	}

}
