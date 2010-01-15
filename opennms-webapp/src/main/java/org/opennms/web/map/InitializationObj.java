package org.opennms.web.map;

import java.util.List;

import org.opennms.web.map.config.Avail;
import org.opennms.web.map.config.ContextMenu;
import org.opennms.web.map.config.Icon;
import org.opennms.web.map.config.Link;
import org.opennms.web.map.config.LinkStatus;
import org.opennms.web.map.config.MapPropertiesFactory;
import org.opennms.web.map.config.Severity;
import org.opennms.web.map.config.Status;
import org.opennms.web.map.view.Manager;

public class InitializationObj extends  MapPropertiesFactory {
	boolean availEnabled=true;
	boolean doubleClickEnabled=true;
	boolean contextMenuEnabled=true;
	boolean reload=false;
	ContextMenu contextMenu;
	List<Link> links;
	List<LinkStatus> linkStatuses;
	List<Status> statuses;
	List<Severity> severities;
	List<Avail> avails;
	java.util.Map<String,Icon> icons;
	java.util.Map<String,String> iconsBySysoid;
	java.util.Map<String,String> backgroundImages;
	java.util.Map<String, String> mapElementDimensions;
    String defaultNodeIcon;
    String defaultMapIcon;
    String defaultBackgroundColor;
	String NODE_TYPE = MapsConstants.NODE_TYPE;
	String MAP_TYPE=MapsConstants.MAP_TYPE;
	int MAP_NOT_OPENED = MapsConstants.MAP_NOT_OPENED;
	int NEW_MAP = MapsConstants.NEW_MAP;
	boolean isUserAdmin=false;
	List<String> categories;
	
    
    public ContextMenu getContextMenu() {
		return contextMenu;
	}

	public void setContextMenu(ContextMenu contextMenu) {
		this.contextMenu = contextMenu;
	}

	public String getNODE_TYPE() {
		return NODE_TYPE;
	}

	public void setNODE_TYPE(String node_type) {
		NODE_TYPE = node_type;
	}

	public String getMAP_TYPE() {
		return MAP_TYPE;
	}

	public void setMAP_TYPE(String map_type) {
		MAP_TYPE = map_type;
	}

	public int getMAP_NOT_OPENED() {
		return MAP_NOT_OPENED;
	}

	public void setMAP_NOT_OPENED(int map_not_opened) {
		MAP_NOT_OPENED = map_not_opened;
	}

	public int getNEW_MAP() {
		return NEW_MAP;
	}

	public void setNEW_MAP(int new_map) {
		NEW_MAP = new_map;
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

	public java.util.Map<String, Icon> getIcons() {
		return icons;
	}

	public void setIcons(java.util.Map<String, Icon> icons) {
		this.icons = icons;
	}

	 public java.util.Map<String, String> getIconsBySysoid() {
	     return iconsBySysoid;
    }

	public void setIconsBySysoid(java.util.Map<String, String> iconsBySysoid) {
	    this.iconsBySysoid = iconsBySysoid;
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

	
	public boolean isUserAdmin() {
		return isUserAdmin;
	}

	public void setUserAdmin(boolean isUserAdmin) {
		this.isUserAdmin = isUserAdmin;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	InitializationObj(MapPropertiesFactory mpf, Manager manager, boolean isUserAdmin)throws Exception{
	        
	        doubleClickEnabled=mpf.isDoubleClickEnabled();
	        contextMenuEnabled=mpf.isContextMenuEnabled();
	        reload=mpf.isReload();
	        contextMenu=mpf.getContextMenu();
	        links=mpf.getLinks();
	        linkStatuses=mpf.getLinkStatuses();
	        statuses=mpf.getStatuses();
	        severities=mpf.getSeverities();
	        avails=mpf.getAvails();
	        icons=mpf.getIcons();
	        iconsBySysoid=mpf.getIconsBySysoid();
	        backgroundImages=mpf.getBackgroundImages();
	        mapElementDimensions=mpf.getMapElementDimensions();
	        defaultNodeIcon=mpf.getDefaultNodeIcon();
	        defaultMapIcon=mpf.getDefaultMapIcon();
	        defaultBackgroundColor=mpf.getDefaultBackgroundColor();
	        this.isUserAdmin=isUserAdmin;
	        categories=manager.getCategories();
	    }

	InitializationObj(MapPropertiesFactory mpf, Manager manager)throws Exception{
		
		doubleClickEnabled=mpf.isDoubleClickEnabled();
		contextMenuEnabled=mpf.isContextMenuEnabled();
		reload=mpf.isReload();
		contextMenu=mpf.getContextMenu();
		links=mpf.getLinks();
		linkStatuses=mpf.getLinkStatuses();
		statuses=mpf.getStatuses();
		severities=mpf.getSeverities();
		avails=mpf.getAvails();
		icons=mpf.getIcons();
		iconsBySysoid=mpf.getIconsBySysoid();
		backgroundImages=mpf.getBackgroundImages();
		mapElementDimensions=mpf.getMapElementDimensions();
        defaultNodeIcon=mpf.getDefaultNodeIcon();
        defaultMapIcon=mpf.getDefaultMapIcon();
        defaultBackgroundColor=mpf.getDefaultBackgroundColor();
        isUserAdmin=false;
        categories=manager.getCategories();
    }
    
}
