/*
 * Creato il 27-ago-2004
 *
 */
package org.opennms.netmgt.inventory.handler;
import java.io.*;
import java.util.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.config.inventory.plugin.ssh.*;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.*;
import org.exolab.castor.xml.Unmarshaller;
import java.util.regex.*;
/**
 * @author maurizio
 *
 */
public class SshHandler implements DataHandler {
	private static Pattern pattern;
	private static Matcher matcher;
	
	
	public String handle(Object data, Map parameters)throws IllegalStateException {
		SshConfig sshConfig = null;
		String sshConfigFile = parameters.get("plugin-conf-file").toString();
		if(sshConfigFile==null){
			throw new IllegalStateException("parameter 'plugin-conf-file' not found.");
		}
		try{
		InputStream cfgIn = new FileInputStream(sshConfigFile);
		sshConfig = (SshConfig)Unmarshaller.unmarshal(SshConfig.class,new InputStreamReader(cfgIn));
		}catch(FileNotFoundException f){
			throw new IllegalStateException(f.getMessage());
		}catch(ValidationException v){
			throw new IllegalStateException("Unable to validate plugin-conf-file "+v.getMessage());
		}
		catch(MarshalException m){
			throw new IllegalStateException("Unable to parse plugin-conf-file "+m.getMessage());
		}
		boolean sessionFound = false;
		String strToHandle = (String) data;
		org.opennms.netmgt.config.inventory.parser.Inventory  inv = new org.opennms.netmgt.config.inventory.parser.Inventory();
		String sessionName = parameters.get("session").toString();
		inv.setName(sessionName);
		Enumeration enumCorrespondences = null;
		Enumeration enumSession = sshConfig.enumerateSession();
			while(enumSession.hasMoreElements()){
			 Session sess = (Session) enumSession.nextElement();
			 if(sess.getName().equals(sessionName)){
				sessionFound = true;
				enumCorrespondences = sess.enumerateCorrespondence();
			 }
		}
		if(sessionFound==false){
			throw new IllegalStateException("Session with name '"+sessionName+"' not found in plugin-conf-file");
		}
		
		while(enumCorrespondences.hasMoreElements()){
			Correspondence corr = (Correspondence) enumCorrespondences.nextElement();
			String itemName = corr.getItemName();
			String regExpr = corr.getRegExpression();
			String assetField = corr.getAssetField();
			pattern = Pattern.compile(regExpr,Pattern.DOTALL);
			matcher = pattern.matcher(strToHandle);

			String dataItem = "";
			Item item = new Item();
			item.setName(itemName);
			boolean found = false;
			if(assetField!=null){
				item.setAssetField(assetField); 
				}
			while(matcher.find()) {
						found=true;
						dataItem += matcher.group();
					}
			String[] lines = dataItem.split("\n");
			Dataitem dI = new Dataitem();
			dI.setLine(lines);
			item.setDataitem(dI);
			//item.setDataitem(dataItem);
			if(found){
				inv.addItem(item);
				}
			}
		StringWriter sw = new StringWriter();
		try{
			Marshaller.marshal(inv,sw);
		}catch(Exception e){
			throw new IllegalStateException(e.getMessage());
		}

		return sw.toString();
	}
	
		
}
