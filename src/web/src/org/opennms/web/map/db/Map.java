/*
 * Created on 9-dic-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.web.map.db;

import java.sql.Timestamp;

/**
 * @author
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class Map {

    private int id;

    private String name;

    private String background;

    private String owner;

    private String accessMode;

    private String userLastModifies;

    private Timestamp createTime;

    private Timestamp lastModifiedTime;

    private float scale;

    private int offsetX;

    private int offsetY;

    private String type;

    public static final String USER_GENERATED_MAP = "U";

    public static final String AUTOMATICALLY_GENERATED_MAP = "A";

    public static final String DELETED_MAP = "D";

    private boolean isNew = false;

    protected Map() {
        this.isNew = true;
    }
    
  
    
    protected Map(int id, String name, String background, String owner,
            String accessMode, String userLastModifies, float scale,
            int offsetX, int offsetY, String type) {
        this.id = id;
        this.name = name;
        this.background = background;
        this.owner = owner;
        this.accessMode = accessMode;
        this.userLastModifies = userLastModifies;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.type = type;
    }

    /**
     * @return Returns the accessMode.
     */
    public String getAccessMode() {
        return accessMode;
    }

    /**
     * @param accessMode
     *            The accessMode to set.
     */
    protected void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    /**
     * @return Returns the background.
     */
    public String getBackground() {
        return background;
    }

    /**
     * @param background
     *            The background to set.
     */
    protected void setBackground(String background) {
        this.background = background;
    }

    /**
     * @return Returns the createTime.
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime
     *            The createTime to set.
     */
    protected void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * @return Returns the lastModifiedTime.
     */
    public Timestamp getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * @param lastModifiedTime
     *            The lastModifiedTime to set.
     */
    protected void setLastModifiedTime(Timestamp lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    protected void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the offsetX.
     */
    public int getOffsetX() {
        return offsetX;
    }

    /**
     * @param offsetX
     *            The offsetX to set.
     */
    protected void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * @return Returns the offsetY.
     */
    public int getOffsetY() {
        return offsetY;
    }

    /**
     * @param offsetY
     *            The offsetY to set.
     */
    protected void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * @return Returns the owner.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            The owner to set.
     */
    protected void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return Returns the scale.
     */
    public float getScale() {
        return scale;
    }

    /**
     * @param scale
     *            The scale to set.
     */
    protected void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * @return Returns the userLastModifies.
     */
    public String getUserLastModifies() {
        return userLastModifies;
    }

    /**
     * @param userLastModifies
     *            The userLastModifies to set.
     */
    protected void setUserLastModifies(String userLastModifies) {
        this.userLastModifies = userLastModifies;
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

    public int getId() {
        return id;
    }

    protected void setId(int id) {
        this.id = id;
    }

    protected boolean isNew() {
        return this.isNew;
    }

    void setAsNew(boolean v) {
        this.isNew = v;
    }
}
