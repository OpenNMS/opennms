/*
 * Creato il 27-ago-2004
 *
 * Per modificare il modello associato a questo file generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
package org.opennms.netmgt.inventory.handler;

import java.io.*;
import java.util.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.config.inventory.plugin.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import org.exolab.castor.xml.*;
import org.opennms.core.utils.*;
import org.apache.log4j.*;
/**
 * @author maurizio
 *
 * Per modificare il modello associato al commento di questo tipo generato, aprire
 * Finestra&gt;Preferenze&gt;Java&gt;Generazione codice&gt;Codice e commenti
 */
public class XmlHandler implements DataHandler {
	private List newPathList = new ArrayList();
	private Map correspondenceMap = new HashMap();
	
	public String handle(Object data, Map parameters)throws IllegalStateException {
		String xsltFile=(String)parameters.get("xslt-file");
		if(xsltFile==null)
			throw new IllegalStateException("Unable to handle data: set file xslt before.");
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		
		try {
			transformer =
				tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(xsltFile));
		}
		catch (TransformerConfigurationException tce) {
			throw (new IllegalStateException("Unable to handle data."+tce));
		}
		BufferedReader filebuf = null;
		String strToParse = (String) data;
		
		Reader r = new StringReader(strToParse);
		Writer w = new StringWriter();
		try {
			transformer.transform(
				new StreamSource(r),
				new javax.xml.transform.stream.StreamResult(w));
		}
		catch (TransformerException t) {
			throw (new IllegalArgumentException("Unable to handle data."+t));
		}
		String retInventory = new String();
		try{
			w.write(retInventory);
		}catch(IOException i){
			throw (new IllegalArgumentException("Unable to handle data."+i));
		}
		String pluginConfFile =(String)parameters.get("plugin-conf-file");
		if(pluginConfFile!=null){
			try{
				retInventory = insertAssetField(w, pluginConfFile);
				return retInventory;
			}catch(Exception e){
				throw new IllegalStateException(e.getMessage());
			}
		}
		return w.toString();
	}
	
	
		
	private void visit(Item currItem, List path, List list){
				List tmpPathList=new ArrayList(path);
				tmpPathList.add(currItem.getName());
				if(currItem.getItemCount()==0){
						list.add(tmpPathList);
						if(correspondenceMap.containsKey(tmpPathList))
							currItem.setAssetField((String)correspondenceMap.get(tmpPathList));
				}else{
				 	 Enumeration enumItem = currItem.enumerateItem();
					 while(enumItem.hasMoreElements()){
						 visit((Item)enumItem.nextElement(),tmpPathList, list);
					 }
				}
			}
	
	private void createCorrespondenceMap(String confFile)throws MarshalException, ValidationException,FileNotFoundException{
		InputStream configIn = new FileInputStream(confFile);
		PluginConfiguration pconf = (PluginConfiguration) Unmarshaller.unmarshal(PluginConfiguration.class, new InputStreamReader(configIn)); 
		Enumeration enumItemMapping = pconf.enumerateItemMapping();
		while(enumItemMapping.hasMoreElements()){
			ItemMapping im = (ItemMapping) enumItemMapping.nextElement();
			Enumeration enumCorr = im.enumerateCorrespondence();
			while(enumCorr.hasMoreElements()){
				Correspondence corr = (Correspondence) enumCorr.nextElement();
				String itemTree = corr.getItemName();
				String assetField = corr.getAssettableColumn();
				List itemTreeList = treeToList(itemTree);
				correspondenceMap.put(itemTreeList, assetField);
			}
		}
	}
	
	private String insertAssetField(Writer w, String confFile)throws MarshalException, ValidationException, FileNotFoundException{
		createCorrespondenceMap(confFile);
		Category log = ThreadCategory.getInstance(getClass());
		org.opennms.netmgt.config.inventory.parser.Inventory newInvent = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,new StringReader(w.toString()));
		List newPathListTmp = new ArrayList();
		if(newInvent.getItemCount()>0){
				Enumeration enumItem = newInvent.enumerateItem();
			while(enumItem.hasMoreElements()){
				visit((Item)enumItem.nextElement(),newPathListTmp, newPathList); 
			}
		}
		StringWriter sw = new StringWriter();
		
		Marshaller.marshal(newInvent,sw);
		return sw.toString();

	}
	
	private List treeToList(String itemTree){
		itemTree = itemTree.trim();
		StringTokenizer st = new StringTokenizer(itemTree,",");
		List itemTreeList = new ArrayList();
		while (st.hasMoreTokens()) {
		  itemTreeList.add(st.nextToken().trim());
		}
		return itemTreeList;
	}
}
