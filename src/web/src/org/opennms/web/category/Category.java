package org.opennms.web.category;

import java.util.*;


/**
 * Adapts the functionality of the category definition and RTC category updates
 * into one simple interface.  Also adds many convenience methods.
 *
 * <p>The category definition is read from the categories.xml file by the 
 * {@link org.opennms.netmgt.config.CategoryFactory CategoryFactory}.  The 
 * RTC category updates are periodically sent from the RTC to the WebUI.
 * </p>
 *
 * @author <a href="mailto:larry@opennms.org">Lawrence Karnowski</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
public class Category extends Object 
{
    /** The category definition (from the categories.xml file). */
    protected org.opennms.netmgt.config.categories.Category categoryDef;
    
    /** An update from the RTC about the service level availability for this category. */ 
    protected org.opennms.netmgt.xml.rtc.Category rtcCategory;
    
    /** 
     * The last time this category was updated.  Note that with the current 
     * way this class and the CategoryModel are implemented, this value does
     * not change because a new instance of this class is created for each
     * RTC update.
     */
    protected Date lastUpdated;
    
    /** 
     * A cached value of the total number of services on nodes belonging 
     * to this category.
     */
    protected Long serviceCount;
    
    /** 
     * A cached value of the total number of services on nodes belonging 
     * to this category that are currently down.
     */    
    protected Long serviceDownCount;

    
    /**
     * Create a new instance to wrapper information from the categories.xml
     * file (that defines a category) and information from the RTC (that gives
     * current service level availability).
     */
    protected Category( org.opennms.netmgt.config.categories.Category categoryDef, org.opennms.netmgt.xml.rtc.Category rtcCategory, Date lastUpdated ) {
        if( categoryDef == null || rtcCategory == null || lastUpdated == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
            
        if( categoryDef.getLabel() == null || !categoryDef.getLabel().equals(rtcCategory.getCatlabel()) ) {
            throw new IllegalArgumentException("Cannot take category definition and rtc category value whose names do not match.");
        }
        
        this.categoryDef = categoryDef;
        this.rtcCategory = rtcCategory;        
        this.lastUpdated = lastUpdated;
        
        this.serviceCount = null;
        this.serviceDownCount = null;
    }
 
    
    /** Return the unique name for this category. */
    public String getName() {
        return this.categoryDef.getLabel();
    }
    
    
    /** Return the value considered to be the minimum "normal" value. */ 
    public double getNormalThreshold() {
        return this.categoryDef.getNormal();
    }


    /** 
     * Return the value considered to be the minimum value below the "normal"
     * value where only a warning is necessary.  Below this value the 
     * category's value will be considered unacceptable.
     */
    public double getWarningThreshold() {
        return this.categoryDef.getWarning();
    }
    
    
    /** Return a description explaining this category. */
    public String getComment() {
        return this.categoryDef.getComment();
    }


    /** Return the date and time this category was last updated by the RTC. */ 
    public Date getLastUpdated() {
        return this.lastUpdated;
    }


    /** Return the current service level availability for this category. */
    public double getValue() {
        return this.rtcCategory.getCatvalue();
    }
    
    
    /** 
     * Package protected implementation method that exposes the internal 
     * representation (a Castor-generated object) of the data from the RTC,
     * strictly for use in marshalling the data back to XML (via Castor).
     * In other words, this method is only for debugging purposes, please do
     * not use in normal situations.  Instead please use the public methods
     * of this class.
     */
    org.opennms.netmgt.xml.rtc.Category getRtcCategory() {
        return this.rtcCategory;
    }
    
    
    /** Return the number of services contained within this category. */
    public long getServiceCount() {
        if( this.serviceCount == null ) {
            long[] counts = getServiceCounts(this.rtcCategory);
            
            this.serviceCount = new Long(counts[0]);
            this.serviceDownCount = new Long(counts[1]);
        }        
        
        return this.serviceCount.longValue();
    }


    /** Return the number of services that are currently down with this category. */
    public long getServiceDownCount() {
        if( this.serviceCount == null ) {
            long[] counts = getServiceCounts(this.rtcCategory);
            
            this.serviceCount = new Long(counts[0]);
            this.serviceDownCount = new Long(counts[1]);
        }        
        
        return this.serviceDownCount.longValue();
    }


    /**
     * Returns an enumeration of the Castor-generated Node objects tied to
     * this category. 
     *
     * <p>Note, LJK Dec 5,2001: I'm not really happy about exposing the Castor
     * objects this way.  We do it all over the place, but I've already started
     * hiding them in this particular case (the rtceui.xsd objects).  I'm not
     * very pleased with this half approach.  I'd rather hide them completely or
     * not at all, but I don't want to introduce a new pass-through object.</p>
     */
    public Enumeration enumerateNode() {
        return this.rtcCategory.enumerateNode();
    }

    
    /**
     * Convenience method to count the number of services under a category
     * and the number of those services that are currently down.
     */
    protected static long[] getServiceCounts(org.opennms.netmgt.xml.rtc.Category category) {
        if( category == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        long count = 0;
        long downCount = 0;
        
        Enumeration nodeEnum = category.enumerateNode();
        
        while( nodeEnum.hasMoreElements() ) {
            org.opennms.netmgt.xml.rtc.Node node = (org.opennms.netmgt.xml.rtc.Node)nodeEnum.nextElement();            

            count += node.getNodesvccount();
            downCount += node.getNodesvcdowncount();
        }

        return new long[] { count, downCount };        
    }
    
}
