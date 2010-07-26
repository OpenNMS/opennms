package org.opennms.web.map.view;

import java.util.List;

import org.opennms.web.map.config.Avail;
import org.opennms.web.map.config.ContextMenu;
import org.opennms.web.map.config.Link;
import org.opennms.web.map.config.LinkStatus;
import org.opennms.web.map.config.Severity;
import org.opennms.web.map.config.Status;

/**
 * <p>VProperties class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
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
    
    boolean useSemaphore=true;

    int unknownstatusid;
    int maxLinks;
    int summaryLink;
    
    String summaryLinkColor;
    String multilinkIgnoreColor;
    String multilinkStatus;

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
	
    /**
     * <p>Getter for the field <code>multilinkIgnoreColor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilinkIgnoreColor() {
        return multilinkIgnoreColor;
    }

    /**
     * <p>Setter for the field <code>multilinkIgnoreColor</code>.</p>
     *
     * @param multilinkIgnoreColor a {@link java.lang.String} object.
     */
    public void setMultilinkIgnoreColor(String multilinkIgnoreColor) {
        this.multilinkIgnoreColor = multilinkIgnoreColor;
    }

    /**
     * <p>Getter for the field <code>multilinkStatus</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMultilinkStatus() {
        return multilinkStatus;
    }

    /**
     * <p>Setter for the field <code>multilinkStatus</code>.</p>
     *
     * @param multilinkStatus a {@link java.lang.String} object.
     */
    public void setMultilinkStatus(String multilinkStatus) {
        this.multilinkStatus = multilinkStatus;
    }
	
    /**
     * <p>Getter for the field <code>summaryLinkColor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSummaryLinkColor() {
        return summaryLinkColor;
    }

    /**
     * <p>Setter for the field <code>summaryLinkColor</code>.</p>
     *
     * @param summaryLinkColor a {@link java.lang.String} object.
     */
    public void setSummaryLinkColor(String summaryLinkColor) {
        this.summaryLinkColor = summaryLinkColor;
    }

    /**
     * <p>Getter for the field <code>summaryLink</code>.</p>
     *
     * @return a int.
     */
    public int getSummaryLink() {
        return summaryLink;
    }

    /**
     * <p>Setter for the field <code>summaryLink</code>.</p>
     *
     * @param summaryLink a int.
     */
    public void setSummaryLink(int summaryLink) {
        this.summaryLink = summaryLink;
    }

    /**
     * <p>Getter for the field <code>maxLinks</code>.</p>
     *
     * @return a int.
     */
    public int getMaxLinks() {
        return maxLinks;
    }

    /**
     * <p>Setter for the field <code>maxLinks</code>.</p>
     *
     * @param maxLinks a int.
     */
    public void setMaxLinks(int maxLinks) {
        this.maxLinks = maxLinks;
    }

	/**
	 * <p>Getter for the field <code>unknownstatusid</code>.</p>
	 *
	 * @return a int.
	 */
	public int getUnknownstatusid() {
        return unknownstatusid;
    }

    /**
     * <p>Setter for the field <code>unknownstatusid</code>.</p>
     *
     * @param unknownstatusid a int.
     */
    public void setUnknownstatusid(int unknownstatusid) {
        this.unknownstatusid = unknownstatusid;
    }

    /**
     * <p>isAvailEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isAvailEnabled() {
        return availEnabled;
    }

    /**
     * <p>Setter for the field <code>availEnabled</code>.</p>
     *
     * @param availEnabled a boolean.
     */
    public void setAvailEnabled(boolean availEnabled) {
        this.availEnabled = availEnabled;
    }

    /**
     * <p>isDoubleClickEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isDoubleClickEnabled() {
        return doubleClickEnabled;
    }

    /**
     * <p>Setter for the field <code>doubleClickEnabled</code>.</p>
     *
     * @param doubleClickEnabled a boolean.
     */
    public void setDoubleClickEnabled(boolean doubleClickEnabled) {
        this.doubleClickEnabled = doubleClickEnabled;
    }

    /**
     * <p>isContextMenuEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isContextMenuEnabled() {
        return contextMenuEnabled;
    }

    /**
     * <p>Setter for the field <code>contextMenuEnabled</code>.</p>
     *
     * @param contextMenuEnabled a boolean.
     */
    public void setContextMenuEnabled(boolean contextMenuEnabled) {
        this.contextMenuEnabled = contextMenuEnabled;
    }

    /**
     * <p>isReload</p>
     *
     * @return a boolean.
     */
    public boolean isReload() {
        return reload;
    }

    /**
     * <p>Setter for the field <code>reload</code>.</p>
     *
     * @param reload a boolean.
     */
    public void setReload(boolean reload) {
        this.reload = reload;
    }

    /**
     * <p>isUserAdmin</p>
     *
     * @return a boolean.
     */
    public boolean isUserAdmin() {
        return isUserAdmin;
    }

    /**
     * <p>setUserAdmin</p>
     *
     * @param isUserAdmin a boolean.
     */
    public void setUserAdmin(boolean isUserAdmin) {
        this.isUserAdmin = isUserAdmin;
    }

    /**
     * <p>Getter for the field <code>contextMenu</code>.</p>
     *
     * @return a {@link org.opennms.web.map.config.ContextMenu} object.
     */
    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    /**
     * <p>Setter for the field <code>contextMenu</code>.</p>
     *
     * @param contextMenu a {@link org.opennms.web.map.config.ContextMenu} object.
     */
    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }

    /**
     * <p>Getter for the field <code>links</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Link> getLinks() {
        return links;
    }

    /**
     * <p>Setter for the field <code>links</code>.</p>
     *
     * @param links a {@link java.util.List} object.
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    /**
     * <p>Getter for the field <code>linkStatuses</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<LinkStatus> getLinkStatuses() {
        return linkStatuses;
    }

    /**
     * <p>Setter for the field <code>linkStatuses</code>.</p>
     *
     * @param linkStatuses a {@link java.util.List} object.
     */
    public void setLinkStatuses(List<LinkStatus> linkStatuses) {
        this.linkStatuses = linkStatuses;
    }

    /**
     * <p>Getter for the field <code>statuses</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Status> getStatuses() {
        return statuses;
    }

    /**
     * <p>Setter for the field <code>statuses</code>.</p>
     *
     * @param statuses a {@link java.util.List} object.
     */
    public void setStatuses(List<Status> statuses) {
        this.statuses = statuses;
    }

    /**
     * <p>Getter for the field <code>severities</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Severity> getSeverities() {
        return severities;
    }

    /**
     * <p>Setter for the field <code>severities</code>.</p>
     *
     * @param severities a {@link java.util.List} object.
     */
    public void setSeverities(List<Severity> severities) {
        this.severities = severities;
    }

    /**
     * <p>Getter for the field <code>avails</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Avail> getAvails() {
        return avails;
    }

    /**
     * <p>Setter for the field <code>avails</code>.</p>
     *
     * @param avails a {@link java.util.List} object.
     */
    public void setAvails(List<Avail> avails) {
        this.avails = avails;
    }

    /**
     * <p>Getter for the field <code>icons</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public java.util.Map<String, String> getIcons() {
        return icons;
    }

    /**
     * <p>Setter for the field <code>icons</code>.</p>
     *
     * @param icons a {@link java.util.Map} object.
     */
    public void setIcons(java.util.Map<String, String> icons) {
        this.icons = icons;
    }

    /**
     * <p>Getter for the field <code>backgroundImages</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public java.util.Map<String, String> getBackgroundImages() {
        return backgroundImages;
    }

    /**
     * <p>Setter for the field <code>backgroundImages</code>.</p>
     *
     * @param backgroundImages a {@link java.util.Map} object.
     */
    public void setBackgroundImages(java.util.Map<String, String> backgroundImages) {
        this.backgroundImages = backgroundImages;
    }

    /**
     * <p>Getter for the field <code>mapElementDimensions</code>.</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public java.util.Map<String, String> getMapElementDimensions() {
        return mapElementDimensions;
    }

    /**
     * <p>Setter for the field <code>mapElementDimensions</code>.</p>
     *
     * @param mapElementDimensions a {@link java.util.Map} object.
     */
    public void setMapElementDimensions(
            java.util.Map<String, String> mapElementDimensions) {
        this.mapElementDimensions = mapElementDimensions;
    }

    /**
     * <p>Getter for the field <code>defaultNodeIcon</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultNodeIcon() {
        return defaultNodeIcon;
    }

    /**
     * <p>Setter for the field <code>defaultNodeIcon</code>.</p>
     *
     * @param defaultNodeIcon a {@link java.lang.String} object.
     */
    public void setDefaultNodeIcon(String defaultNodeIcon) {
        this.defaultNodeIcon = defaultNodeIcon;
    }

    /**
     * <p>Getter for the field <code>defaultMapIcon</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultMapIcon() {
        return defaultMapIcon;
    }

    /**
     * <p>Setter for the field <code>defaultMapIcon</code>.</p>
     *
     * @param defaultMapIcon a {@link java.lang.String} object.
     */
    public void setDefaultMapIcon(String defaultMapIcon) {
        this.defaultMapIcon = defaultMapIcon;
    }

    /**
     * <p>Getter for the field <code>defaultBackgroundColor</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    /**
     * <p>Setter for the field <code>defaultBackgroundColor</code>.</p>
     *
     * @param defaultBackgroundColor a {@link java.lang.String} object.
     */
    public void setDefaultBackgroundColor(String defaultBackgroundColor) {
        this.defaultBackgroundColor = defaultBackgroundColor;
    }

    /**
     * <p>Getter for the field <code>categories</code>.</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * <p>Setter for the field <code>categories</code>.</p>
     *
     * @param categories a {@link java.util.List} object.
     */
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    /**
     * <p>Getter for the field <code>defaultMapElementDimension</code>.</p>
     *
     * @return a int.
     */
    public int getDefaultMapElementDimension() {
        return defaultMapElementDimension;
    }

    /**
     * <p>Setter for the field <code>defaultMapElementDimension</code>.</p>
     *
     * @param defaultMapElementDimension a int.
     */
    public void setDefaultMapElementDimension(int defaultMapElementDimension) {
        this.defaultMapElementDimension = defaultMapElementDimension;
    }

    /**
     * <p>isUseSemaphore</p>
     *
     * @return a boolean.
     */
    public boolean isUseSemaphore() {
        return useSemaphore;
    }

    /**
     * <p>Setter for the field <code>useSemaphore</code>.</p>
     *
     * @param useSemaphore a boolean.
     */
    public void setUseSemaphore(boolean useSemaphore) {
        this.useSemaphore = useSemaphore;
    }

}
