
package org.opennms.web.nodemap;


public class CategoryResult {

    private int id;
    private String name;
    private String description;

    public int getId() {
	return id;
    }

    public String getName() {
	return name;
    }

    public String getDesc() {
	return description;
    }

    public void setDesc(String desc) {
	this.description = desc;
    }

    public void setName(String name) {
	this.name = name;
    }

    public void setId(int id) {
	this.id = id;
    }


    public CategoryResult(int id, String name, String desc) {
	this.id   = id;
	this.name = name;
	this.description = desc;
    }

    public CategoryResult() {
    }
    
    public String toJson() {
	String out = "{ ";

	out += "\"id\": " + id + ", ";
	out += "\"name\": \"" + name + "\", ";
	out += "\"description\": \"" + description + "\"";
	out += " }";

	return out;
	
    }
    
}