/*
 * Created on 27-ago-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.opennms.netmgt.inventory;
import java.util.*;
import java.io.*;
import org.opennms.netmgt.config.inventory.parser.*;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
/**
 * @author maurizio
 */
public class Comparator {
	private org.opennms.netmgt.config.inventory.parser.Inventory m_inventory;
	private String oldInventoryPathFile;
	private String newInventory;
	private List oldPathList = new ArrayList();
	private List newPathList = new ArrayList();
	private Map oldItemMap = new HashMap();
	private Map newItemMap = new HashMap();
	private String compareMessage="";
	
	
	public Comparator(String oldInventoryPathFile ,String newInventory){
			this.oldInventoryPathFile=oldInventoryPathFile;
			this.newInventory=newInventory;
			return;
		}
		
	private void visit(Item currItem, List path, List list, Map itemMap){
			List tmpPathList=new ArrayList(path);
			tmpPathList.add(currItem.getName());
			if(currItem.getItemCount()==0){
					itemMap.put(tmpPathList,currItem);
					list.add(tmpPathList);
			}else{
			
				Enumeration enumItem = currItem.enumerateItem();
				while(enumItem.hasMoreElements()){
					visit((Item)enumItem.nextElement(),tmpPathList, list, itemMap);
				}
			}
		}
	
		/*
		 * @param newInvent
		 * 
		 */
		private void init(org.opennms.netmgt.config.inventory.parser.Inventory newInvent){
			List oldPathListTmp = new ArrayList();
			oldPathListTmp.add(m_inventory.getName());
			if(m_inventory.getItemCount()>0){
					Enumeration enumItem = m_inventory.enumerateItem();
				while(enumItem.hasMoreElements()){
					visit((Item)enumItem.nextElement(),oldPathListTmp, oldPathList, oldItemMap); 
				}
			}			
			//System.out.println(oldPathListTmp.toString());
			//System.out.println(oldPathList.toString());
	
			List newPathListTmp = new ArrayList();
			newPathListTmp.add(newInvent.getName());
			if(newInvent.getItemCount()>0){
					Enumeration enumItem = newInvent.enumerateItem();
				while(enumItem.hasMoreElements()){
					visit((Item)enumItem.nextElement(),newPathListTmp, newPathList, newItemMap); 
				}
			}	
			//System.out.println(newPathListTmp.toString());
			//System.out.println(newPathList.toString());
			}
	
		private String compareDataItem(List itemPath){
			String retStr="";
			Item oldItem = (Item) oldItemMap.get(itemPath);
			Item newItem = (Item) newItemMap.get(itemPath);
			String oldDataItem = oldItem.getDataitem();
			String newDataItem = newItem.getDataitem();
			if(!oldDataItem.equals(newDataItem))
				retStr+=itemPath+" is changed. Old value: '"+oldDataItem+"', New value: '"+newDataItem+"'.\n<br>";
			return retStr;
		}
	
		/**
		 * Compares old and new inventory 
		 * 
		 * @return 
		 * @throws ValidationException
		 * @throws MarshalException
		 * @throws UnsupportedOperationException 
		 */
		public int compare()throws ValidationException, MarshalException,UnparsableConfigurationException{
			InputStream configIn =null;
			boolean fileFound=true;
			try{
				configIn = new FileInputStream(oldInventoryPathFile);
			}catch(FileNotFoundException f){
				fileFound=false;
				//compareMessage+="Old configuration not found.";
			}
			if(fileFound){
				try{
					m_inventory =	(org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,	new InputStreamReader(configIn));
				}catch(ValidationException ve){
					throw new UnparsableConfigurationException();
					}
				catch(MarshalException me){
					throw new UnparsableConfigurationException();
					}
				org.opennms.netmgt.config.inventory.parser.Inventory newInvent = (org.opennms.netmgt.config.inventory.parser.Inventory) Unmarshaller.unmarshal(org.opennms.netmgt.config.inventory.parser.Inventory.class,new StringReader(newInventory));
				String newInventName = newInvent.getName();
				String oldInventName = m_inventory.getName();
				if(!newInventName.equals(oldInventName)){
					throw new UnsupportedOperationException("Unable to compare inventories with different names.");
				}
	
				try{
					init(newInvent);
				}catch(Exception e){
					e.printStackTrace();
				}
			
				Iterator iterOldPathList = oldPathList.iterator();
				while(iterOldPathList.hasNext()){
					List currOldPath = (ArrayList)iterOldPathList.next();
					if(!newPathList.contains(currOldPath)){
						compareMessage+=currOldPath+" removed.\n<br>";
					}else{
	
						compareMessage += compareDataItem(currOldPath);
					}
				}
			}else{
				return InventoryMonitor.FIRST_ACTIVE_CONFIGURATION_DOWNLOAD;
			}
			Iterator iterNewPathList = newPathList.iterator();
			while(iterNewPathList.hasNext()){
				List currNewPath = (ArrayList)iterNewPathList.next();
				if(!oldPathList.contains(currNewPath)){
					compareMessage+=currNewPath+" added.\n<br>";
				}
			}
			return (compareMessage.equals(""))?InventoryMonitor.CONFIGURATION_NOT_CHANGED:InventoryMonitor.CONFIGURATION_CHANGED;
		}

		private String getDataItemValue(String itemTree){
			StringTokenizer st = new StringTokenizer(itemTree,",");
			List itemTreeList = new ArrayList();
			while (st.hasMoreTokens()) {
			  itemTreeList.add(st.nextToken().trim());
			}
			Item item = (Item) newItemMap.get(itemTreeList);
			if(item!=null)
			   return item.getDataitem();
			else return "";
		}
		
	
		/**
		 * returns the message generated by compare().
		 * @return an empty String if inventory is not changed, the compare message otherwise
		 */
		public String getCompareMessage() {
			return compareMessage;
		}

}
