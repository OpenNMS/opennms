/*
 * Created on 12-nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.inventory;


import org.opennms.netmgt.config.inventory.parser.Item;

/**
 * @author maurizio
 */
public class InventoryItem {
	//this variable will contain the number of newline in the dataitem + 1 (name of the inventoryItem)
	private int numRows=0;
	private int numColumn=0;
	private String name=null;
	private String dataitem=null;

	public static final int EQUAL_STATUS=0;
	public static final int ADDED_STATUS=1;
	public static final int REMOVED_STATUS=2;
	public static final int CHANGED_STATUS=3;
	public static final int EMPTY_STATUS=4;
	
	private int status = EQUAL_STATUS;
	
	public InventoryItem(String name){
		this.name = name;
		calculateNewLine();
	}
	
	public InventoryItem(String name, String dataitem){
		this.name = name;
		this.dataitem = dataitem;
		calculateNewLine();
	}
	
	
	public InventoryItem(Item it, int status, int numColumn){
		this.name=it.getName();
		this.dataitem = it.getDataitem();
		this.status=status;
		this.numColumn = numColumn;
		calculateNewLine();
	}
	
	public InventoryItem(int status, int numRows, int numColumns){
		this.status=status;
		this.numRows=numRows;
		this.numColumn=numColumns;
		}
	
	public boolean equals(InventoryItem invItem){
		if((name==null && invItem.getName()==null) || (name.equals(invItem.getName())))
			return dataItemEquals(invItem);
		return false;
	}
	
	private boolean dataItemEquals(InventoryItem invItem){
		if(dataitem==null && invItem.getDataitem()==null)
			return true;
		if(dataitem==null && invItem.getDataitem()!=null)
			return false;
		if(dataitem!=null && invItem.getDataitem()==null)
			return false;
		return dataitem.equals(invItem.getDataitem());
	}
	/**
	 * @return Returns the dataitem.
	 */
	public String getDataitem() {
		return dataitem;
	}
	/**
	 * @param dataitem The dataitem to set.
	 */
	public void setDataitem(String dataitem) {
		this.dataitem = dataitem;
		calculateNewLine();
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
		calculateNewLine();
	}
	/**
	 * @return Returns the numColumn.
	 */
	public int getNumColumn() {
		return numColumn;
	}
	/**
	 * @param numColumn The numColumn to set.
	 */
	public void setNumColumn(int numColumn) {
		this.numColumn = numColumn;
	}
	/**
	 * @return Returns the numRows.
	 */
	public int getNumRows() {
		return numRows;
	}
	/**
	 * @param numRows The numRows to set.
	 */
	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}
	/**
	 * @return Returns the status.
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int countNewLine(String str) {
		int begin=0;
		int count=0;
		int lastBegin=0;
		while((begin=str.indexOf(System.getProperty("line.separator"),lastBegin))>=0){
			lastBegin=begin+1;
			count++;
		}
		return count;
	}

	private void calculateNewLine(){
		int count = 0;
		if(name != null && !name.equals("")){
			count = 1 + countNewLine(name);
		}
		if(dataitem != null && !dataitem.equals("")){
			count += 1 + countNewLine(dataitem);
		}
		numRows=count;
	}
	
}
