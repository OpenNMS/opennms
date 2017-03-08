/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * 
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.statsd;


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Package encapsulating nodes eligible to have
 *  this report run on them.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class Package implements java.io.Serializable {


    /**
     * The name or identifier for this
     *  package
     */
    @XmlAttribute(name = "name", required = true)
    private String name;

    /**
     * A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter.
     */
    @XmlElement(name = "filter")
    private org.opennms.netmgt.config.statsd.Filter filter;

    /**
     * Reports to be run on the nodes in this
     *  package
     */
    @XmlElement(name = "packageReport")
    private java.util.List<org.opennms.netmgt.config.statsd.PackageReport> packageReportList;

    public Package() {
        this.packageReportList = new java.util.ArrayList<org.opennms.netmgt.config.statsd.PackageReport>();
    }

    /**
     * 
     * 
     * @param vPackageReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackageReport(final org.opennms.netmgt.config.statsd.PackageReport vPackageReport) throws IndexOutOfBoundsException {
        this.packageReportList.add(vPackageReport);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackageReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackageReport(final int index, final org.opennms.netmgt.config.statsd.PackageReport vPackageReport) throws IndexOutOfBoundsException {
        this.packageReportList.add(index, vPackageReport);
    }

    /**
     * Method enumeratePackageReport.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.statsd.PackageReport> enumeratePackageReport() {
        return java.util.Collections.enumeration(this.packageReportList);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Package) {
            Package temp = (Package)obj;
            boolean equals = Objects.equals(temp.name, name)
                && Objects.equals(temp.filter, filter)
                && Objects.equals(temp.packageReportList, packageReportList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'filter'. The field 'filter' has the following
     * description: A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter.
     * 
     * @return the value of field 'Filter'.
     */
    public org.opennms.netmgt.config.statsd.Filter getFilter() {
        return this.filter;
    }

    /**
     * Returns the value of field 'name'. The field 'name' has the following
     * description: The name or identifier for this
     *  package
     * 
     * @return the value of field 'Name'.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Method getPackageReport.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.statsd.PackageReport at
     * the given index
     */
    public org.opennms.netmgt.config.statsd.PackageReport getPackageReport(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.packageReportList.size()) {
            throw new IndexOutOfBoundsException("getPackageReport: Index value '" + index + "' not in range [0.." + (this.packageReportList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.statsd.PackageReport) packageReportList.get(index);
    }

    /**
     * Method getPackageReport.Returns the contents of the collection in an Array.
     *  <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.statsd.PackageReport[] getPackageReport() {
        org.opennms.netmgt.config.statsd.PackageReport[] array = new org.opennms.netmgt.config.statsd.PackageReport[0];
        return (org.opennms.netmgt.config.statsd.PackageReport[]) this.packageReportList.toArray(array);
    }

    /**
     * Method getPackageReportCollection.Returns a reference to
     * 'packageReportList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.statsd.PackageReport> getPackageReportCollection() {
        return this.packageReportList;
    }

    /**
     * Method getPackageReportCount.
     * 
     * @return the size of this collection
     */
    public int getPackageReportCount() {
        return this.packageReportList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            name, 
            filter, 
            packageReportList);
        return hash;
    }

    /**
     * Method iteratePackageReport.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.statsd.PackageReport> iteratePackageReport() {
        return this.packageReportList.iterator();
    }

    /**
     */
    public void removeAllPackageReport() {
        this.packageReportList.clear();
    }

    /**
     * Method removePackageReport.
     * 
     * @param vPackageReport
     * @return true if the object was removed from the collection.
     */
    public boolean removePackageReport(final org.opennms.netmgt.config.statsd.PackageReport vPackageReport) {
        boolean removed = packageReportList.remove(vPackageReport);
        return removed;
    }

    /**
     * Method removePackageReportAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.statsd.PackageReport removePackageReportAt(final int index) {
        Object obj = this.packageReportList.remove(index);
        return (org.opennms.netmgt.config.statsd.PackageReport) obj;
    }

    /**
     * Sets the value of field 'filter'. The field 'filter' has the following
     * description: A rule which adresses belonging to this package
     *  must pass. This package is applied only to addresses that pass
     *  this filter.
     * 
     * @param filter the value of field 'filter'.
     */
    public void setFilter(final org.opennms.netmgt.config.statsd.Filter filter) {
        this.filter = filter;
    }

    /**
     * Sets the value of field 'name'. The field 'name' has the following
     * description: The name or identifier for this
     *  package
     * 
     * @param name the value of field 'name'.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackageReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setPackageReport(final int index, final org.opennms.netmgt.config.statsd.PackageReport vPackageReport) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.packageReportList.size()) {
            throw new IndexOutOfBoundsException("setPackageReport: Index value '" + index + "' not in range [0.." + (this.packageReportList.size() - 1) + "]");
        }
        
        this.packageReportList.set(index, vPackageReport);
    }

    /**
     * 
     * 
     * @param vPackageReportArray
     */
    public void setPackageReport(final org.opennms.netmgt.config.statsd.PackageReport[] vPackageReportArray) {
        //-- copy array
        packageReportList.clear();
        
        for (int i = 0; i < vPackageReportArray.length; i++) {
                this.packageReportList.add(vPackageReportArray[i]);
        }
    }

    /**
     * Sets the value of 'packageReportList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vPackageReportList the Vector to copy.
     */
    public void setPackageReport(final java.util.List<org.opennms.netmgt.config.statsd.PackageReport> vPackageReportList) {
        // copy vector
        this.packageReportList.clear();
        
        this.packageReportList.addAll(vPackageReportList);
    }

    /**
     * Sets the value of 'packageReportList' by setting it to the given Vector. No
     * type checking is performed.
     * @deprecated
     * 
     * @param packageReportList the Vector to set.
     */
    public void setPackageReportCollection(final java.util.List<org.opennms.netmgt.config.statsd.PackageReport> packageReportList) {
        this.packageReportList = packageReportList;
    }

}
