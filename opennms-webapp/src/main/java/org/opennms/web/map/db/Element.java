/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.web.map.MapsException;

/**
 * @author
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Element implements Cloneable {
    private int mapId;

    private int id;

    protected String type;

    private String label;

    private String iconName;

    private int x;

    private int y;

    public static final String MAP_TYPE = "M";

    public static final String NODE_TYPE = "N";
    
    public static final String defaultNodeIcon = "unspecified";
    public static final String defaultMapIcon = "map";

    protected Element() {
        // blank
    }

    public Element(Element e) throws MapsException {
        this(e.mapId, e.id, e.type, e.label, e.iconName, e.x, e.y);
    }

    public Element(int mapId, int id, String type, String label,
            String iconName, int x, int y)throws MapsException {
        this.mapId = mapId;
        this.id = id;
        this.setType(type);
        this.label = label;
        setIcon(iconName);
        this.x = x;
        this.y = y;
    }

    /**
     * @return Returns the iconName.
     */
    public String getIcon() {
        return iconName;
    }

    /**
     * @param iconName
     *            The iconName to set.
     */
    public void setIcon(String iconName) {
    	if(iconName==null){
    		iconName=defaultNodeIcon;
    	}
        this.iconName = iconName;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            The label to set.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return Returns the x.
     */
    public int getX() {
        return x;
    }

    /**
     * @param x
     *            The x to set.
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * @return Returns the y.
     */
    public int getY() {
        return y;
    }

    /**
     * @param y
     *            The y to set.
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type
     *            The type to set.
     */
    public void setType(String type) throws MapsException {
        if (type.equals(MAP_TYPE) || type.equals(NODE_TYPE))  this.type = type;
        new MapsException("Cannot create an Element with type " + type);
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }

    /**
     * @return Returns the id.
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            The id to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    public Element clone() {
        try {
            return (Element) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UndeclaredThrowableException(e, "CloneNotSupportedException thrown while calling super.clone(), which is odd since we implement the Cloneable interface");
        }
    }
    
    public boolean isMap() {
    	if (type.equals(MAP_TYPE)) return true;
    	return false;
    }

    public boolean isNode() {
    	if (type.equals(NODE_TYPE)) return true;
    	return false;
    }

}
