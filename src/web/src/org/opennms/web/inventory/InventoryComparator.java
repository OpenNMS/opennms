/*
 * Created on 12-nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.inventory;

import java.util.Comparator;

/**
 * @author maurizio
 *
 * This class is used to sort inventories returned by InventoryFactory
 * on the nodelabel.
 */
public class InventoryComparator implements Comparator {
	int sort = 1;
	public static final int NODE_LABEL_SORT = 1;
	public static final int LAST_POLL_TIME_SORT = 2;
	public static final int CREATE_TIME_SORT = 3;
	public static final int CATEGORY_SORT =4;
	public static final int STATUS_SORT = 5;
	
	
	public int compare(Object firstInv, Object secondInv) {
		Inventory firstInventory = (Inventory) firstInv;
		Inventory secondInventory = (Inventory) secondInv;
		if(sort==NODE_LABEL_SORT){
			return firstInventory.getNodeLabel().compareTo(secondInventory.getNodeLabel());
		}
		if(sort==LAST_POLL_TIME_SORT){
			return firstInventory.getLastPollTime().compareTo(secondInventory.getLastPollTime())*-1;
		}
		if(sort==CREATE_TIME_SORT){
			return firstInventory.getCreateTime().compareTo(secondInventory.getCreateTime())*-1;
		}		
		if(sort==CATEGORY_SORT){
			return firstInventory.getName().compareTo(secondInventory.getName());
		}
		return firstInventory.getStatus().compareTo(secondInventory.getStatus());
		
	}

	/**
	 * @return Returns the sort value.
	 */
	public int getSort() {
		return sort;
	}
	/**
	 * @param sort.
	 */
	public void setSort(int sort) {
		this.sort = sort;
	}

}
