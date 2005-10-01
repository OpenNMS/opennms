/*
 * Created on 13-ott-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.web.inventory;
import java.io.*;
import java.util.*;
import org.exolab.castor.xml.Unmarshaller;
import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.inventory.UnparsableConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;


/**
 * @author maurizio
 */
public class TreeVisualization implements Visualization {
    private String fileContent="";
	private String fileDtd="treeview.dtd";
	private String fileXslt="treeview.xslt";
	private String newLine = System.getProperty("line.separator");

	
	public String getVisualization(String filePath, Map parameters) throws IOException, UnparsableConfigurationException{
		
		fileDtd = (String)parameters.get("fileDtd");
		if(fileDtd==null){
			throw new IOException("Parameter fileDtd not found.");
		}
		fileXslt = (String)parameters.get("fileXslt");
		if(fileXslt==null){
			throw new IOException("Parameter fileXslt not found.");
		}
		org.opennms.netmgt.config.inventory.parser.Inventory inventory = null;
		try{
			inventory =	(org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,	new InputStreamReader(new FileInputStream(filePath)));
		}catch(Exception e){
			throw new UnparsableConfigurationException(e.toString());
		}
		
		fileContent =	"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + newLine
					+ "<!DOCTYPE treeview SYSTEM \""
					+ fileDtd+"\">" + newLine
					+ "<?xml-stylesheet type=\"text/xsl\" href=\""
					+ fileXslt
					+ "\"?>" + newLine;
		createTreeView(inventory);
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		Writer w;
		try {
			transformer = tFactory.newTransformer(new javax.xml.transform.stream.StreamSource(fileXslt));
			Reader r = new StringReader(fileContent);
			w = new StringWriter();
			transformer.transform(new StreamSource(r),new javax.xml.transform.stream.StreamResult(w));
		}catch (TransformerException t) {
			throw new IOException(t.toString());
		}
		return w.toString();
	}
	
	
	private void visit(Item it, int code){
		if(it.getItemCount()<=0){
			String dataItem = it.getDataitem();
			if(!(dataItem.trim()).equals("")){
				dataItem=": "+dataItem;
			}
			addLeaf(it.getName()+dataItem,"leaf.gif",""+code);
		}else{
			openFolder(it.getName(),"folder.gif",""+code,false);
			Enumeration enumItem = it.enumerateItem();
			List itemNameList = new ArrayList();
			Map itemMap = new HashMap();
			while(enumItem.hasMoreElements()){
				Item currItem =(Item)  enumItem.nextElement();
				String itemName =  currItem.getName();
				itemNameList.add(itemName);
				itemMap.put(itemName, currItem);
			}
			Collections.sort(itemNameList,String.CASE_INSENSITIVE_ORDER);
			Iterator iter = itemNameList.iterator();
			while(iter.hasNext()){
				Item currItem = (Item) itemMap.get(iter.next());
				visit(currItem, ++code);
			}
			closeFolder();
		}
	}
	
	private void createTreeView(org.opennms.netmgt.config.inventory.parser.Inventory inventory) {
			openTreeView(inventory.getName());
			openFolder(inventory.getName(),"folder.gif",""+0,true);
			Enumeration enumItem = inventory.enumerateItem();
			int code  = 1;
			List itemNameList = new ArrayList();
			Map itemMap = new HashMap();
			while(enumItem.hasMoreElements()){
				Item currItem =(Item)  enumItem.nextElement();
				String itemName =  currItem.getName();
				itemNameList.add(itemName);
				itemMap.put(itemName, currItem);
			}
			Collections.sort(itemNameList,String.CASE_INSENSITIVE_ORDER);
			Iterator it = itemNameList.iterator();
			while(it.hasNext()){
				Item currItem = (Item) itemMap.get(it.next());
				visit(currItem, ++code);
			}
			closeFolder();
			closeTreeView();
		}
	
	
	private void openTreeView(String title) {
			fileContent += "<treeview title=\"" + title + "\">" + newLine;
		}

		private void openFolder(String title, String img, String code, boolean expanded) {

			fileContent += "\t<folder title=\""
				+ title
				+ "\" img=\""
				+ img
				+ "\" code=\""
				+ code
				+ "\" expanded=\""
				+ expanded
				+ "\">" + newLine;
		}

		private void closeFolder() {
			fileContent += "\t</folder>" + newLine;
		}

		private void addLeaf(String title, String img, String code) {

			fileContent += "\t\t<leaf title=\""
				+ title
				+ "\" img=\""
				+ img
				+ "\" code=\""
				+ code
				+ "\"/>" + newLine;
		}

		private void closeTreeView() {
			fileContent += "</treeview>";
		}
		

			
	/**
	 * @return
	 */
	public String getFileDtd() {
		return fileDtd;
	}

	/**
	 * @return
	 */
	public String getFileXslt() {
		return fileXslt;
	}

	/**
	 * @param string
	 */
	public void setFileDtd(String string) {
		fileDtd = string;
	}

	/**
	 * @param string
	 */
	public void setFileXslt(String string) {
		fileXslt = string;
	}

}
