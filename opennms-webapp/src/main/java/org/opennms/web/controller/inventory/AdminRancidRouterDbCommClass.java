package org.opennms.web.controller.inventory;

/**
 * <p>AdminRancidRouterDbCommClass class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AdminRancidRouterDbCommClass {
        
    private String groupName;
    
    private String deviceName;
    
    private String statusName;
    
    private String deviceTypeName;

    private String comment;
    
    /**
     * <p>Getter for the field <code>comment</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getComment() {
        return comment;
    }

    /**
     * <p>Setter for the field <code>comment</code>.</p>
     *
     * @param comment a {@link java.lang.String} object.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * <p>Getter for the field <code>deviceTypeName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    /**
     * <p>Setter for the field <code>deviceTypeName</code>.</p>
     *
     * @param deviceTypeName a {@link java.lang.String} object.
     */
    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    /**
     * <p>Getter for the field <code>statusName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStatusName() {
        return statusName;
    }
    
    /**
     * <p>Setter for the field <code>statusName</code>.</p>
     *
     * @param statusName a {@link java.lang.String} object.
     */
    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
    
    /**
     * <p>Getter for the field <code>groupName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * <p>Setter for the field <code>groupName</code>.</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * <p>Getter for the field <code>deviceName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * <p>Setter for the field <code>deviceName</code>.</p>
     *
     * @param deviceName a {@link java.lang.String} object.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
