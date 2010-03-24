package org.opennms.web.map.view;

import java.util.List;

import org.opennms.web.map.config.Avail;
import org.opennms.web.map.config.ContextMenu;
import org.opennms.web.map.config.Link;
import org.opennms.web.map.config.LinkStatus;
import org.opennms.web.map.config.Severity;
import org.opennms.web.map.config.Status;

public class VProperties {
    

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    boolean isUserAdmin=false;
    boolean reload=false;

	boolean doubleClickEnabled=true;
	boolean contextMenuEnabled=true;
    boolean availEnabled=true;
    

    int unknownstatusid;

    ContextMenu contextMenu;

	
	List<Link> links;
	List<LinkStatus> linkStatuses;
	List<Status> statuses;
	List<Severity> severities;
	List<Avail> avails;
	
    int defaultMapElementDimension;

    java.util.Map<String,String> icons;
	java.util.Map<String,String> backgroundImages;
	java.util.Map<String, String> mapElementDimensions;
    
	String defaultNodeIcon;
    String defaultMapIcon;
    String defaultBackgroundColor;
	
	List<String> categories;
	
	
	public int getUnknownstatusid() {
        return unknownstatusid;
    }

    public void setUnknownstatusid(int unknownstatusid) {
        this.unknownstatusid = unknownstatusid;
    }

    public boolean isAvailEnabled() {
        return availEnabled;
    }

    public void setAvailEnabled(boolean availEnabled) {
        this.availEnabled = availEnabled;
    }

    public boolean isDoubleClickEnabled() {
        return doubleClickEnabled;
    }

    public void setDoubleClickEnabled(boolean doubleClickEnabled) {
        this.doubleClickEnabled = doubleClickEnabled;
    }

    public boolean isContextMenuEnabled() {
        return contextMenuEnabled;
    }

    public void setContextMenuEnabled(boolean contextMenuEnabled) {
        this.contextMenuEnabled = contextMenuEnabled;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public boolean isUserAdmin() {
        return isUserAdmin;
    }

    public void setUserAdmin(boolean isUserAdmin) {
        this.isUserAdmin = isUserAdmin;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<LinkStatus> getLinkStatuses() {
        return linkStatuses;
    }

    public void setLinkStatuses(List<LinkStatus> linkStatuses) {
        this.linkStatuses = linkStatuses;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    public List<Severity> getSeverities() {
        return severities;
    }

    public void setSeverities(List<Severity> severities) {
        this.severities = severities;
    }

    public List<Avail> getAvails() {
        return avails;
    }

    public void setAvails(List<Avail> avails) {
        this.avails = avails;
    }

    public java.util.Map<String, String> getIcons() {
        return icons;
    }

    public void setIcons(java.util.Map<String, String> icons) {
        this.icons = icons;
    }

    public java.util.Map<String, String> getBackgroundImages() {
        return backgroundImages;
    }

    public void setBackgroundImages(java.util.Map<String, String> backgroundImages) {
        this.backgroundImages = backgroundImages;
    }

    public java.util.Map<String, String> getMapElementDimensions() {
        return mapElementDimensions;
    }

    public void setMapElementDimensions(
            java.util.Map<String, String> mapElementDimensions) {
        this.mapElementDimensions = mapElementDimensions;
    }

    public String getDefaultNodeIcon() {
        return defaultNodeIcon;
    }

    public void setDefaultNodeIcon(String defaultNodeIcon) {
        this.defaultNodeIcon = defaultNodeIcon;
    }

    public String getDefaultMapIcon() {
        return defaultMapIcon;
    }

    public void setDefaultMapIcon(String defaultMapIcon) {
        this.defaultMapIcon = defaultMapIcon;
    }

    public String getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public void setDefaultBackgroundColor(String defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public int getDefaultMapElementDimension() {
        return defaultMapElementDimension;
    }

    public void setDefaultMapElementDimension(int defaultMapElementDimension) {
        this.defaultMapElementDimension = defaultMapElementDimension;
    }
    
}
