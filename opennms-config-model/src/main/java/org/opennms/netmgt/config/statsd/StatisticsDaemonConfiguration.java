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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the statsd-configuration.xml
 *  configuration file.
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "statistics-daemon-configuration")
@XmlAccessorType(XmlAccessType.FIELD)

@SuppressWarnings("all") public class StatisticsDaemonConfiguration implements java.io.Serializable {


    /**
     * Package encapsulating nodes eligible to have
     *  this report run on them.
     */
    @XmlElement(name = "package", required = true)
    private java.util.List<org.opennms.netmgt.config.statsd.Package> _packageList;

    /**
     * Reports
     */
    @XmlElement(name = "report", required = true)
    private java.util.List<org.opennms.netmgt.config.statsd.Report> reportList;

    public StatisticsDaemonConfiguration() {
        this._packageList = new java.util.ArrayList<org.opennms.netmgt.config.statsd.Package>();
        this.reportList = new java.util.ArrayList<org.opennms.netmgt.config.statsd.Report>();
    }

    /**
     * 
     * 
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final org.opennms.netmgt.config.statsd.Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(vPackage);
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addPackage(final int index, final org.opennms.netmgt.config.statsd.Package vPackage) throws IndexOutOfBoundsException {
        this._packageList.add(index, vPackage);
    }

    /**
     * 
     * 
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReport(final org.opennms.netmgt.config.statsd.Report vReport) throws IndexOutOfBoundsException {
        this.reportList.add(vReport);
    }

    /**
     * 
     * 
     * @param index
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addReport(final int index, final org.opennms.netmgt.config.statsd.Report vReport) throws IndexOutOfBoundsException {
        this.reportList.add(index, vReport);
    }

    /**
     * Method enumeratePackage.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.statsd.Package> enumeratePackage() {
        return java.util.Collections.enumeration(this._packageList);
    }

    /**
     * Method enumerateReport.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public java.util.Enumeration<org.opennms.netmgt.config.statsd.Report> enumerateReport() {
        return java.util.Collections.enumeration(this.reportList);
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
        
        if (obj instanceof StatisticsDaemonConfiguration) {
            StatisticsDaemonConfiguration temp = (StatisticsDaemonConfiguration)obj;
            boolean equals = Objects.equals(temp._packageList, _packageList)
                && Objects.equals(temp.reportList, reportList);
            return equals;
        }
        return false;
    }

    /**
     * Method getPackage.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.statsd.Package at the
     * given index
     */
    public org.opennms.netmgt.config.statsd.Package getPackage(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("getPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.statsd.Package) _packageList.get(index);
    }

    /**
     * Method getPackage.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.statsd.Package[] getPackage() {
        org.opennms.netmgt.config.statsd.Package[] array = new org.opennms.netmgt.config.statsd.Package[0];
        return (org.opennms.netmgt.config.statsd.Package[]) this._packageList.toArray(array);
    }

    /**
     * Method getPackageCollection.Returns a reference to '_packageList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.statsd.Package> getPackageCollection() {
        return this._packageList;
    }

    /**
     * Method getPackageCount.
     * 
     * @return the size of this collection
     */
    public int getPackageCount() {
        return this._packageList.size();
    }

    /**
     * Method getReport.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the org.opennms.netmgt.config.statsd.Report at the
     * given index
     */
    public org.opennms.netmgt.config.statsd.Report getReport(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reportList.size()) {
            throw new IndexOutOfBoundsException("getReport: Index value '" + index + "' not in range [0.." + (this.reportList.size() - 1) + "]");
        }
        
        return (org.opennms.netmgt.config.statsd.Report) reportList.get(index);
    }

    /**
     * Method getReport.Returns the contents of the collection in an Array. 
     * <p>Note:  Just in case the collection contents are changing in another
     * thread, we pass a 0-length Array of the correct type into the API call. 
     * This way we <i>know</i> that the Array returned is of exactly the correct
     * length.
     * 
     * @return this collection as an Array
     */
    public org.opennms.netmgt.config.statsd.Report[] getReport() {
        org.opennms.netmgt.config.statsd.Report[] array = new org.opennms.netmgt.config.statsd.Report[0];
        return (org.opennms.netmgt.config.statsd.Report[]) this.reportList.toArray(array);
    }

    /**
     * Method getReportCollection.Returns a reference to 'reportList'. No type
     * checking is performed on any modifications to the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public java.util.List<org.opennms.netmgt.config.statsd.Report> getReportCollection() {
        return this.reportList;
    }

    /**
     * Method getReportCount.
     * 
     * @return the size of this collection
     */
    public int getReportCount() {
        return this.reportList.size();
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            _packageList, 
            reportList);
        return hash;
    }

    /**
     * Method iteratePackage.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.statsd.Package> iteratePackage() {
        return this._packageList.iterator();
    }

    /**
     * Method iterateReport.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public java.util.Iterator<org.opennms.netmgt.config.statsd.Report> iterateReport() {
        return this.reportList.iterator();
    }

    /**
     */
    public void removeAllPackage() {
        this._packageList.clear();
    }

    /**
     */
    public void removeAllReport() {
        this.reportList.clear();
    }

    /**
     * Method removePackage.
     * 
     * @param vPackage
     * @return true if the object was removed from the collection.
     */
    public boolean removePackage(final org.opennms.netmgt.config.statsd.Package vPackage) {
        boolean removed = _packageList.remove(vPackage);
        return removed;
    }

    /**
     * Method removePackageAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.statsd.Package removePackageAt(final int index) {
        Object obj = this._packageList.remove(index);
        return (org.opennms.netmgt.config.statsd.Package) obj;
    }

    /**
     * Method removeReport.
     * 
     * @param vReport
     * @return true if the object was removed from the collection.
     */
    public boolean removeReport(final org.opennms.netmgt.config.statsd.Report vReport) {
        boolean removed = reportList.remove(vReport);
        return removed;
    }

    /**
     * Method removeReportAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public org.opennms.netmgt.config.statsd.Report removeReportAt(final int index) {
        Object obj = this.reportList.remove(index);
        return (org.opennms.netmgt.config.statsd.Report) obj;
    }

    /**
     * 
     * 
     * @param index
     * @param vPackage
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setPackage(final int index, final org.opennms.netmgt.config.statsd.Package vPackage) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this._packageList.size()) {
            throw new IndexOutOfBoundsException("setPackage: Index value '" + index + "' not in range [0.." + (this._packageList.size() - 1) + "]");
        }
        
        this._packageList.set(index, vPackage);
    }

    /**
     * 
     * 
     * @param vPackageArray
     */
    public void setPackage(final org.opennms.netmgt.config.statsd.Package[] vPackageArray) {
        //-- copy array
        _packageList.clear();
        
        for (int i = 0; i < vPackageArray.length; i++) {
                this._packageList.add(vPackageArray[i]);
        }
    }

    /**
     * Sets the value of '_packageList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vPackageList the Vector to copy.
     */
    public void setPackage(final java.util.List<org.opennms.netmgt.config.statsd.Package> vPackageList) {
        // copy vector
        this._packageList.clear();
        
        this._packageList.addAll(vPackageList);
    }

    /**
     * Sets the value of '_packageList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param _packageList the Vector to set.
     */
    public void setPackageCollection(final java.util.List<org.opennms.netmgt.config.statsd.Package> _packageList) {
        this._packageList = _packageList;
    }

    /**
     * 
     * 
     * @param index
     * @param vReport
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setReport(final int index, final org.opennms.netmgt.config.statsd.Report vReport) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.reportList.size()) {
            throw new IndexOutOfBoundsException("setReport: Index value '" + index + "' not in range [0.." + (this.reportList.size() - 1) + "]");
        }
        
        this.reportList.set(index, vReport);
    }

    /**
     * 
     * 
     * @param vReportArray
     */
    public void setReport(final org.opennms.netmgt.config.statsd.Report[] vReportArray) {
        //-- copy array
        reportList.clear();
        
        for (int i = 0; i < vReportArray.length; i++) {
                this.reportList.add(vReportArray[i]);
        }
    }

    /**
     * Sets the value of 'reportList' by copying the given Vector. All elements
     * will be checked for type safety.
     * 
     * @param vReportList the Vector to copy.
     */
    public void setReport(final java.util.List<org.opennms.netmgt.config.statsd.Report> vReportList) {
        // copy vector
        this.reportList.clear();
        
        this.reportList.addAll(vReportList);
    }

    /**
     * Sets the value of 'reportList' by setting it to the given Vector. No type
     * checking is performed.
     * @deprecated
     * 
     * @param reportList the Vector to set.
     */
    public void setReportCollection(final java.util.List<org.opennms.netmgt.config.statsd.Report> reportList) {
        this.reportList = reportList;
    }

}
