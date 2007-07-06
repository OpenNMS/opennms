/*
 * Created on 10-gen-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.view;

/**
 * @author micmas
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
final public class VMapInfo {
	public static final String DEFAULT_NAME = "NewMap";

    private int id = -2;
    private String name = DEFAULT_NAME;
    private String owner = "admin";


	/**
	 * @param id
	 * @param name
	 * @param owner
	 */
	public VMapInfo(int id, String name, String owner) {
		super();
		this.id = id;
		this.name = name;
		this.owner = owner;
	}

	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	
	public String getOwner() {
		return owner;
	}
	
	public boolean equals(Object obj){
		VMapInfo otherMapMenu = (VMapInfo) obj;
		if(id==otherMapMenu.getId()){
			return true;
		}
		return false;
	}
}
