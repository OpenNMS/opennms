/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

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

    protected Element() {
        // blank
    }

    protected Element(Element e) {
        this(e.mapId, e.id, e.type, e.label, e.iconName, e.x, e.y);
    }

    protected Element(int mapId, int id, String type, String label,
            String iconName, int x, int y) {
        this.mapId = mapId;
        this.id = id;
        this.type = type;
        this.label = label;
        this.iconName = iconName;
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
    protected void setType(String type) {
        this.type = type;
    }

    public int getMapId() {
        return mapId;
    }

    protected void setMapId(int mapId) {
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
    protected void setId(int id) {
        this.id = id;
    }

    public Object clone() {
        try {
            Element e;
            e = (Element) super.clone();
            return e;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
