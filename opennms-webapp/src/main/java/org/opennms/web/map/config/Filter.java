package org.opennms.web.map.config;

public class Filter{
	String table;
	String condition;
	public Filter(String table, String condition) {
		super();
		this.table = table;
		this.condition = condition;
	}
	protected String getCondition() {
		return condition;
	}
	protected void setCondition(String condition) {
		this.condition = condition;
	}
	protected String getTable() {
		return table;
	}
	protected void setTable(String table) {
		this.table = table;
	}
	
}
