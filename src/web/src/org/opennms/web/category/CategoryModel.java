//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.category;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.resource.Vault;
import org.opennms.netmgt.config.CategoryFactory;


public class CategoryModel extends Object
{
    /** The name of the category that includes all services and nodes. */
    public static final String OVERALL_AVAILABILITY_CATEGORY = "Overall Service Availability";

    /** The singleton instance of this class. */
    private static CategoryModel instance;

    
    /**
     * Return the <code>CategoryModel</code>.
     */        
    public synchronized static CategoryModel getInstance() throws IOException, MarshalException, ValidationException {
        if( CategoryModel.instance == null ) {
            CategoryModel.instance = new CategoryModel();            
        }
        
        return( CategoryModel.instance );
    }


    /** A mapping of category names to category instances. */
    protected HashMap categories = new HashMap();
    
    /** A reference to the CategoryFactory to get to category definitions. */
    protected CategoryFactory catFactory = null;

    /** The Log4J category for logging status and debug messages. */
    protected org.apache.log4j.Category log = org.opennms.core.utils.ThreadCategory.getInstance("RTC");    

    
    /** 
     * Create the instance of the CategoryModel.
     */    
    private CategoryModel() throws IOException, MarshalException, ValidationException {
        CategoryFactory.init();        
        this.catFactory = CategoryFactory.getInstance();
        
        this.log.debug("The CategoryModel object was created");
    }


    /**
     * Return the <code>Category</code> instance for the given
     * category name.  Return null if there is no match for the
     * given name.
     */
    public Category getCategory(String categoryName)  {
        if( categoryName == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }
        
        return (Category)this.categories.get(categoryName);
    }

    
    /** 
     * Return a mapping of category names to instances.
     */
    public Map getCategoryMap() {
        return (Map)this.categories.clone();
    }


    /** Look up the category definition and return the category's normal threshold. */    
    public double getCategoryNormalThreshold(String categoryName) {
        if( categoryName == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return this.catFactory.getNormal(categoryName);
    }
    

    /** Look up the category definition and return the category's warning threshold. */    
    public double getCategoryWarningThreshold(String categoryName) {
        if( categoryName == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        return this.catFactory.getWarning(categoryName);
    }


    /** Look up the category definition and return the category's description. */    
    public String getCategoryComment(String categoryName) {
        if( categoryName == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        String comment = null;
        org.opennms.netmgt.config.categories.Category category = this.catFactory.getCategory(categoryName);
        
        if( category != null ) {
            comment = category.getComment();
        }
        
        return comment;
    }
    
    
    /** Update a category with new values. */
    public void updateCategory(org.opennms.netmgt.xml.rtc.Category rtcCategory) {
        if( rtcCategory == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        String categoryName = rtcCategory.getCatlabel();        
        org.opennms.netmgt.config.categories.Category categoryDef = this.catFactory.getCategory(categoryName);
        org.opennms.web.category.Category category = new org.opennms.web.category.Category(categoryDef, rtcCategory, new Date()); 
        
        synchronized(this.categories) {
            this.categories.put(categoryName, category);
        }
        
        this.log.debug(categoryName + " was updated");
    }

    
    /**
     * Return the availability percentage for all managed services on the given node
     * for the last 24 hours.  If there are no managed services on this node, 
     * then a value of -1 is returned.
     */
    public double getNodeAvailability(int nodeId) throws SQLException {        
        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add( Calendar.DATE, -1 );
        Date yesterday = cal.getTime();        
        
        return this.getNodeAvailability(nodeId, yesterday, now);
    }
    
    
    /**
     * Return the availability percentage for all managed services on the given node
     * from the given start time until the given end time.  If there are no managed
     * services on this node, then a value of -1 is returned.
     */
    public double getNodeAvailability(int nodeId, Date start, Date end) throws SQLException {
        if( start == null || end == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        if( end.before(start) ) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }
        
        if( end.equals(start) ) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }
        
        double avail = -1;

        Connection conn = Vault.getDbConnection();
        
        try {
            PreparedStatement stmt = conn.prepareStatement("select getManagePercentAvailNodeWindow(?, ?, ?) as avail");

            stmt.setInt(1, nodeId);
            //yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(2, new Timestamp(end.getTime()));
            stmt.setTimestamp(3, new Timestamp(start.getTime()));

            ResultSet rs = stmt.executeQuery();
            
            if( rs.next() ) {
                avail = rs.getDouble("avail");
            }
        }
        finally {
            Vault.releaseDbConnection(conn);
        }
        
        return avail;
    }
    
    
    /**
     * Return the availability percentage for all managed services on the given interface
     * for the last 24 hours.  If there are no managed services on this interface, 
     * then a value of -1 is returned.
     */
    public double getInterfaceAvailability(int nodeId, String ipAddr) throws SQLException {
        if( ipAddr == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add( Calendar.DATE, -1 );
        Date yesterday = cal.getTime();        
        
        return this.getInterfaceAvailability(nodeId, ipAddr, yesterday, now);
    }

    
    /**
     * Return the availability percentage for all managed services on the given interface
     * from the given start time until the given end time.  If there are no managed
     * services on this interface, then a value of -1 is returned.
     */
    public double getInterfaceAvailability(int nodeId, String ipAddr, Date start, Date end) throws SQLException {
        if( ipAddr == null || start == null || end == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        if( end.before(start) ) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }
        
        if( end.equals(start) ) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }
        
        double avail = -1;

        Connection conn = Vault.getDbConnection();
        
        try {
            PreparedStatement stmt = conn.prepareStatement("select getManagePercentAvailIntfWindow(?, ?, ?, ?) as avail");

            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            //yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(3, new Timestamp(end.getTime()));
            stmt.setTimestamp(4, new Timestamp(start.getTime()));

            ResultSet rs = stmt.executeQuery();
            
            if( rs.next() ) {
                avail = rs.getDouble("avail");
            }
        }
        finally {
            Vault.releaseDbConnection(conn);
        }
        
        return avail;
    }


    /**
     * Return the availability percentage for a managed service for the last 
     * 24 hours.  If the service is not managed, then a value of -1 is returned.
     */
    public double getServiceAvailability(int nodeId, String ipAddr, int serviceId) throws SQLException {
        if( ipAddr == null ) {
            throw new IllegalArgumentException( "Cannot take null parameters." );
        }

        Calendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        cal.add( Calendar.DATE, -1 );
        Date yesterday = cal.getTime();        
        
        return this.getServiceAvailability(nodeId, ipAddr, serviceId, yesterday, now);
    }
    

    /**
     * Return the availability percentage for a managed service from the given 
     * start time until the given end time.  If the service is not managed, 
     * then a value of -1 is returned.
     */    
    public double getServiceAvailability(int nodeId, String ipAddr, int serviceId, Date start, Date end) throws SQLException {
        if( ipAddr == null || start == null || end == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        if( end.before(start) ) {
            throw new IllegalArgumentException("Cannot have an end time before the start time.");
        }
        
        if( end.equals(start) ) {
            throw new IllegalArgumentException("Cannot have an end time equal to the start time.");
        }
        
        double avail = -1;

        Connection conn = Vault.getDbConnection();
        
        try {
            PreparedStatement stmt = conn.prepareStatement("select getPercentAvailabilityInWindow(?, ?, ?, ?, ?) as avail from ifservices where ifservices.ipaddr = ipinterface.ipaddr and ifservices.nodeid = ipinterface.nodeid and ipinterface.ismanaged='M' and nodeid=? and ipaddr=? and serviceid=?");
            
            stmt.setInt(1, nodeId);
            stmt.setString(2, ipAddr);
            stmt.setInt(3, serviceId);
            //yes, these are supposed to be backwards, the end time first
            stmt.setTimestamp(4, new Timestamp(end.getTime()));
            stmt.setTimestamp(5, new Timestamp(start.getTime()));
            stmt.setInt(6, nodeId);
            stmt.setString(7, ipAddr);
            stmt.setInt(8, serviceId);
            
            ResultSet rs = stmt.executeQuery();
            
            if( rs.next() ) {
                avail = rs.getDouble("avail");
            }
        }
        finally {
            Vault.releaseDbConnection(conn);
        }
        
        return avail;                
    }    
}
