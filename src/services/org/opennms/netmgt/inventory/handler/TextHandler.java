/*
 * Creato il 27-ago-2004
 *
 */
package org.opennms.netmgt.inventory.handler;

import java.io.*;
import java.util.*;


import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.config.inventory.plugin.*;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.Unmarshaller;
/**
 * @author maurizio
 *
 */

class Killer extends TimerTask
{
    Process process;
    
    public Killer(Process p) {
        process = p;
    }
    
    public void run()
    {
        process.destroy();
    }
}

public class TextHandler implements DataHandler {

	private final long DEFAULT_TIMEOUT = 5000;
	
	public String handle(Object data, Map parameters)throws IllegalStateException {

		PluginConfiguration plugInConfig = null;
		String pluginConfigFile = parameters.get("plugin-conf-file").toString();
		if(pluginConfigFile==null){
			throw new IllegalStateException("parameter 'plugin-conf-file' not found.");
		}
		try{
		InputStream cfgIn = new FileInputStream(pluginConfigFile);
		plugInConfig = (PluginConfiguration)Unmarshaller.unmarshal(PluginConfiguration.class,new InputStreamReader(cfgIn));
		}catch(FileNotFoundException f){
			throw new IllegalStateException(f.getMessage());
		}catch(ValidationException v){
			throw new IllegalStateException("Unable to validate plugin-conf-file: "+pluginConfigFile+v.getMessage());
		}
		catch(MarshalException m){
			throw new IllegalStateException("Unable to parse plugin-conf-file: "+pluginConfigFile+m.getMessage());
		}
		boolean imappFound = false;
		String strToHandle = (String) data;
		org.opennms.netmgt.config.inventory.parser.Inventory  inv = new org.opennms.netmgt.config.inventory.parser.Inventory();
		String command = parameters.get("command").toString();
		inv.setName(command);
		Enumeration enumCorrisp = null;
		Enumeration enumItemMapping = plugInConfig.enumerateItemMapping();
		
		
		while(enumItemMapping.hasMoreElements()){
			 ItemMapping imapp = (ItemMapping) enumItemMapping.nextElement();
			 if(imapp.getCommand().equals(command)){
				imappFound = true;
				enumCorrisp = imapp.enumerateCorrespondence();
				break;
			 }
		}
		if(imappFound==false){
			throw new IllegalStateException("Item-mapping with command '"+command+"' not found in plugin-conf-file: "+pluginConfigFile);
		}
		
		while(enumCorrisp.hasMoreElements()){
			Correspondence corr = (Correspondence) enumCorrisp.nextElement();
			long timeout=DEFAULT_TIMEOUT;
			String itemName = corr.getItemName();
			String assetField = corr.getAssettableColumn();
			String filterCommand = corr.getFilterCommand();
			String itemSeparator = corr.getItemSeparator();
			
			String dataItem = strToHandle;
			if(filterCommand!=null){
				if(corr.getTimeout()>0)
				{
					timeout=corr.getTimeout();
				}
				try{
					dataItem = execCommand(filterCommand,timeout,strToHandle);
				}catch(IOException io){
					throw new IllegalStateException("Unable to execute filter command "+filterCommand+" \n "+io);
				}
			}
			String lineSeparator = System.getProperty("line.separator");
			if(itemSeparator!=null){
				Vector dataItemVec = mySplit(dataItem,itemSeparator);
				for(int i=0;i<dataItemVec.size();i++){
					Item newItem = new Item();
					newItem.setName(itemName+"("+i+")");
					Vector linesVec = mySplit(((String) dataItemVec.get(i)), lineSeparator);
					String[] lines = (String[]) linesVec.toArray(new String[0]); 
					Dataitem dI = new Dataitem();
					dI.setLine(lines);
					newItem.setDataitem(dI);
					//newItem.setDataitem((String) dataItemVec.get(i));
					inv.addItem(newItem);
				}
			}else{
				Item newItem = new Item();
				newItem.setName(itemName);
				
				Vector linesVec = mySplit(((String) dataItem), lineSeparator);
				String[] lines = (String[]) linesVec.toArray(new String[0]); 

				Dataitem dI = new Dataitem();
				dI.setLine(lines);
				newItem.setDataitem(dI);
				//newItem.setDataitem(dataItem);
				if(assetField!=null){
					newItem.setAssetField(assetField);
				}
				inv.addItem(newItem);
			}
		}
		StringWriter sw = new StringWriter();

		try{
			Marshaller.marshal(inv,sw);
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage());
		}

		try{
		sw.close();
		}catch(IOException io){
			throw new IllegalStateException(io.getMessage());
		}
		
		return sw.toString();
	}
	
	
  private String execCommand(String cmdline, long timeout, String strToParse)throws IOException {
        String retString = "";

        Process process = Runtime.getRuntime().exec(cmdline);
        Timer t = new Timer();
        Killer k = new Killer(process);
        t.schedule(k, timeout);
        
        OutputStreamWriter standardInput = new OutputStreamWriter(process.getOutputStream());
        BufferedReader standardOutput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        standardInput.write(strToParse);
        /* forced input close */
        standardInput.close();
        String line;
        String lineSeparator = System.getProperty("line.separator");

        while ((line = standardOutput.readLine()) != null) {
            retString +=  line+lineSeparator;
        }
        standardOutput.close();
        t.cancel();
	    return retString;
	   }
  
  
	  private Vector mySplit(String strToSplit, String separator){
			Vector strVector = new Vector();
			int begin=0,end=0;
			while(true){
				end=strToSplit.indexOf(separator,begin+1);
				if(end==-1){
					strVector.add(strToSplit.substring(begin,strToSplit.length()));
					return strVector;
				}
			    strVector.add(strToSplit.substring(begin,end));
				begin=end+1;
			}
		}
}
