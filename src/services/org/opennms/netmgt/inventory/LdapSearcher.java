//Copyright (C) 2004 Dimension, Data Corp. All rights reserved
//Parts Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
//Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//	 OpenNMS Licensing       <license@opennms.org>
//	 http://www.opennms.org/
//	 http://www.sortova.com/
//   http://www.didata.it/
//
//

package org.opennms.netmgt.inventory;
import java.io.*;
import java.util.*;
import java.lang.reflect.UndeclaredThrowableException;
import javax.xml.transform.*;
import java.util.Map;
import org.w3c.dom.*;
import org.apache.xpath.*;
import org.apache.log4j.Priority;
import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.*;
import com.novell.ldap.LDAPException;
import com.novell.ldap.util.*;
import org.opennms.netmgt.config.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.opennms.netmgt.config.inventory.plugin.ldap.*;

/**
 * @author rssntn67@yahoo.it
 *
 * 
 */

public class LdapSearcher {

	private static final String DEFAULT_BASE = "sub";

	private String ipaddr = "";
	private String title = "Ldap Root";
	private String fileContent = "";
	private String confFile = "";
	private String definitionName = "";
	private int timeout;
	private int retry;
	private Map LdapAttribs = new HashMap();
	private Map correspondenceMap = new HashMap();
	private List newPathList = new ArrayList();
	
	
	public LdapSearcher(String ipAddress, String configFile, String definitionName, int timeout, int retry) {
		ipaddr=ipAddress;
		confFile=configFile;
		this.timeout=timeout;
		this.retry=retry;
		this.definitionName= definitionName;
	}


	public void setIpaddr(String ipaddr) {
		this.ipaddr = ipaddr;
	}




	public void setRootTitle(String title) throws IOException {
		this.title = title;
	}



	private String getData(org.w3c.dom.Node node, String tag)
		throws TransformerException {
		org.w3c.dom.Node selectedNode = XPathAPI.selectSingleNode(node, tag);
		if (selectedNode == null)
			return "";
		selectedNode = selectedNode.getFirstChild();
		if (selectedNode == null)
			return "";
		else
			return ((Text) selectedNode).getData();
	}

	private void toStandardFormat()
		throws
			IOException,
			FileNotFoundException,
			MarshalException,
			ValidationException,
			ClassNotFoundException,
			LDAPException,InterruptedException {

		Category log = ThreadCategory.getInstance(getClass());


		try {
			LdapPeerFactory.reload(confFile, definitionName);
		} catch (MarshalException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("initialize: Failed to load LDAP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (ValidationException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("initialize: Failed to load LDAP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		} catch (IOException ex) {
			if (log.isEnabledFor(Priority.FATAL))
				log.fatal("initialize: Failed to load LDAP configuration", ex);
			throw new UndeclaredThrowableException(ex);
		}

		LdapBroker ldapSrc = new LdapBroker();
		ldapSrc.setRetries(retry);
		ldapSrc.setTimeout(timeout);
		ldapSrc.search(ipaddr);
		Map LdapEntries = ldapSrc.getLdapDataMap();
		LdapAttribs = ldapSrc.getLdapAttribMap();
		log.debug(LdapAttribs);
		Map parameters = LdapPeerFactory.getInstance().getPeer();
		String searchBase =	ParameterMap.getKeyedString(parameters, "searchbase", DEFAULT_BASE);

		log.debug("searchbase = " + searchBase);

		//loop on LdapEntry!!!! compare DN
		Map folderTree = new HashMap();
		List FolderCon = new ArrayList();

		Map attribTree = new HashMap();


		folderTree.put(searchBase, FolderCon);
		attribTree.put(searchBase, FolderCon);
		log.debug("populating Map folderTree");

		Iterator iteratorLdapEntry = LdapEntries.keySet().iterator();
		while (iteratorLdapEntry.hasNext()) {
			String curStrDn = (String) iteratorLdapEntry.next();
			
			log.debug("iterating over DN: current entry" + curStrDn);
			DN curDN = new DN(curStrDn);
			if (folderTree.containsKey(curDN.getParent().toString())){
				List curFolderCon =	(ArrayList) folderTree.get(curDN.getParent().toString());
				if (!curFolderCon.contains(curDN.toString())) {
					curFolderCon.add(curDN.toString());
					folderTree.put(curDN.getParent().toString(),curFolderCon);
					Map attribMap = (Map) LdapAttribs.get(curDN.toString());

					attribTree.put(curDN.toString(),attribMap );
					log.debug("putting " + curDN.toString() + " to existing key" + curDN.getParent().toString() + " in folderTree.");
					log.debug("putting attrib " + attribMap + " to existing key" + curDN.toString() + " in attribTree.");
				}
			} else {
				List curFolderCon = new ArrayList();
				curFolderCon.add(curDN.toString());
				folderTree.put(curDN.getParent().toString(),curFolderCon);
				Map attribMap = (Map) LdapAttribs.get(curDN.toString());

				attribTree.put(curDN.toString(),attribMap);
				log.debug("putting " + curDN.toString() + " to new key" + curDN.getParent().toString() + " in folderTree.");
				log.debug("putting attrib " + attribMap + " to existing key" + curDN.toString() + " in attribTree.");
			}
		}

		log.debug("End Data constructor");

//		Iterator iteratorLog = folderTree.keySet().iterator();
//		while (iteratorLog.hasNext()) {
//			String curDN = (String) iteratorLog.next();
//			List curListLog = (ArrayList) folderTree.get(curDN);
//			log.debug(
//				"key "
//					+ curDN.toString()
//					+ " number of subelement "
//					+ curListLog.size());
//		}
//		log.debug("enteriting XML data constructor");
		openInventory(title);
		log.debug("opening XML root folder");

		String curBaseStr = searchBase;
		List curFolderList = (ArrayList) folderTree.get(curBaseStr);
		while (!curFolderList.isEmpty()) {
			String curDNStr = (String) curFolderList.remove(0);
			log.debug("Removing DN " + curDNStr + "from curBaseDN "	+ curBaseStr);
			folderTree.put(curBaseStr, curFolderList);
			String name = getLdapCN(curDNStr);
			if (folderTree.containsKey(curDNStr)) {
			//	String imagetype = getLdapCNFolderType(curDNStr);
				log.debug("contain objects opening folder");
				openItem(name);
				curBaseStr = curDNStr;
				curFolderList = (ArrayList) folderTree.get(curBaseStr);
			} else {
				log.debug("not contain objects using leaf");
				Map attribMap = (Map) attribTree.get(curDNStr);
				addLeafItem(name, attribMap);
			}
			while (curFolderList.isEmpty() && !curBaseStr.equals(searchBase)) {
				log.debug("LIST of " + curBaseStr + " is empty: escalating");
				closeItem();
				DN curBaseDN = new DN(curBaseStr);
				curBaseDN = curBaseDN.getParent();
				curBaseStr = curBaseDN.toString();
				curFolderList = (ArrayList) folderTree.get(curBaseStr);
				log.debug("current base " + curBaseStr);
			}
			if (curFolderList.isEmpty()&& curBaseStr.equals(searchBase))
					break;

		}
		closeInventory();
	}

	private String getLdapCN(String curDNStr) {
		DN curDN = new DN(curDNStr);
		String v[] = curDN.explodeDN(true);
		return v[0].toString();
		//return "folder or leaf";
	}

	


	private void openInventory(String name) {
			if (name != null)
				title = name;
			fileContent += "<inventory name=\"" + name + "\">\n";
		}

	private void closeInventory() {
		fileContent += "</inventory>";
	}

	private void openItem(String name) {

		fileContent += "\t<item name=\""+ name	+ "\">\n";
	}

	private void closeItem() {
		fileContent += "\t</item>\n";
	}

	private void addLeafItem(String name, Map attribMap) {
		
		fileContent += "\t\t<item name=\""	+ name	+ "\">";
		if(attribMap.isEmpty()){
			fileContent+="<dataitem/>";
		}else{
			Iterator iter = attribMap.keySet().iterator();
			while(iter.hasNext()){
				String itemName = (String) iter.next();
				String dataItem = (String) attribMap.get(itemName);
				dataItem = dataItem.trim();
				addDataItem(itemName, dataItem);
			}
		}
		fileContent+="</item>\n";
	}

	private void addDataItem(String name, String dataItemValue) {
		
			fileContent += "\t\t<item name=\""	+ name	+ "\">" +
				"<dataitem>" + dataItemValue+"</dataitem>\n"+
				"</item>\n";
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
	
	private void createCorrespondenceMap()throws MarshalException, ValidationException,FileNotFoundException{
		Map parameters = LdapPeerFactory.getInstance().getPeer();
		Correspondence [] corresp= (Correspondence[]) parameters.get("correspondences");
		for(int i=0; i< corresp.length; i++){
			Correspondence corr = corresp[i];
			String itemTree = corr.getItemTree();
			String assetField = corr.getAssetField();
			List itemTreeList = treeToList(itemTree);
			correspondenceMap.put(itemTreeList, assetField);
		}
	}
	
	private String insertAssetField()throws MarshalException, ValidationException, FileNotFoundException{
		createCorrespondenceMap();
		Category log = ThreadCategory.getInstance(getClass());
		org.opennms.netmgt.config.inventory.parser.Inventory newInvent = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,new StringReader(fileContent));
		List newPathListTmp = new ArrayList();
		if(newInvent.getItemCount()>0){
				Enumeration enumItem = newInvent.enumerateItem();
			while(enumItem.hasMoreElements()){
				visit((Item)enumItem.nextElement(),newPathListTmp, newPathList); 
			}
		}
		StringWriter sw = new StringWriter();
		Marshaller.marshal(newInvent,sw);
		log.debug(correspondenceMap);
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




	public String getLdapInventory()
		throws
			TransformerException,
			ValidationException,
			IOException,
			MarshalException,
			FileNotFoundException,
			ClassNotFoundException,
			LDAPException, 
			InterruptedException {
		fileContent =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

		toStandardFormat();
		fileContent=insertAssetField();
		return fileContent;
	}




	


}
