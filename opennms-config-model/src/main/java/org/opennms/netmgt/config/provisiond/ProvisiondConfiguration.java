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

package org.opennms.netmgt.config.provisiond;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Behavior configuration for the Provisioner Daemon
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "provisiond-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProvisiondConfiguration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_REQUISITION_DIR = "${install.dir}/etc/imports";
    private static final String DEFAULT_FOREIGN_SOURCE_DIR = "${install.dir}/etc/foreign-sources";

    @XmlAttribute(name = "importThreads")
    private Long importThreads;

    @XmlAttribute(name = "scanThreads")
    private Long scanThreads;

    @XmlAttribute(name = "rescanThreads")
    private Long rescanThreads;

    @XmlAttribute(name = "writeThreads")
    private Long writeThreads;

    @XmlAttribute(name = "requistion-dir")
    private String requistionDir;

    @XmlAttribute(name = "foreign-source-dir")
    private String foreignSourceDir;

    /**
     * Defines an import job with a cron expression
     *  
     *  http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
     *  Field Name Allowed Values Allowed Special Characters
     *  Seconds 0-59 , - /
     *  Minutes 0-59 , - /
     *  Hours 0-23 , - /
     *  Day-of-month 1-31 , - ? / L W C
     *  Month 1-12 or JAN-DEC , - /
     *  Day-of-Week 1-7 or SUN-SAT , - ? / L C #
     *  Year (Opt) empty, 1970-2099 , - /
     *  
     */
    @XmlElement(name = "requisition-def")
    private List<RequisitionDef> requisitionDefList = new ArrayList<>();

    /**
     * 
     * 
     * @param vRequisitionDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRequisitionDef(final RequisitionDef vRequisitionDef) throws IndexOutOfBoundsException {
        this.requisitionDefList.add(vRequisitionDef);
    }

    /**
     * 
     * 
     * @param index
     * @param vRequisitionDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void addRequisitionDef(final int index, final RequisitionDef vRequisitionDef) throws IndexOutOfBoundsException {
        this.requisitionDefList.add(index, vRequisitionDef);
    }

    /**
     */
    public void deleteImportThreads() {
        this.importThreads= null;
    }

    /**
     */
    public void deleteRescanThreads() {
        this.rescanThreads= null;
    }

    /**
     */
    public void deleteScanThreads() {
        this.scanThreads= null;
    }

    /**
     */
    public void deleteWriteThreads() {
        this.writeThreads= null;
    }

    /**
     * Method enumerateRequisitionDef.
     * 
     * @return an Enumeration over all possible elements of this collection
     */
    public Enumeration<RequisitionDef> enumerateRequisitionDef() {
        return Collections.enumeration(this.requisitionDefList);
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
        
        if (obj instanceof ProvisiondConfiguration) {
            ProvisiondConfiguration temp = (ProvisiondConfiguration)obj;
            boolean equals = Objects.equals(temp.importThreads, importThreads)
                && Objects.equals(temp.scanThreads, scanThreads)
                && Objects.equals(temp.rescanThreads, rescanThreads)
                && Objects.equals(temp.writeThreads, writeThreads)
                && Objects.equals(temp.requistionDir, requistionDir)
                && Objects.equals(temp.foreignSourceDir, foreignSourceDir)
                && Objects.equals(temp.requisitionDefList, requisitionDefList);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'foreignSourceDir'.
     * 
     * @return the value of field 'ForeignSourceDir'.
     */
    public String getForeignSourceDir() {
        return this.foreignSourceDir != null ? this.foreignSourceDir : DEFAULT_FOREIGN_SOURCE_DIR;
    }

    /**
     * Returns the value of field 'importThreads'.
     * 
     * @return the value of field 'ImportThreads'.
     */
    public Long getImportThreads() {
        return this.importThreads != null ? this.importThreads : Long.valueOf("8");
    }

    /**
     * Method getRequisitionDef.
     * 
     * @param index
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     * @return the value of the
     * RequisitionDef at the given index
     */
    public RequisitionDef getRequisitionDef(final int index) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.requisitionDefList.size()) {
            throw new IndexOutOfBoundsException("getRequisitionDef: Index value '" + index + "' not in range [0.." + (this.requisitionDefList.size() - 1) + "]");
        }
        
        return (RequisitionDef) requisitionDefList.get(index);
    }

    /**
     * Method getRequisitionDef.Returns the contents of the collection in an
     * Array.  <p>Note:  Just in case the collection contents are changing in
     * another thread, we pass a 0-length Array of the correct type into the API
     * call.  This way we <i>know</i> that the Array returned is of exactly the
     * correct length.
     * 
     * @return this collection as an Array
     */
    public RequisitionDef[] getRequisitionDef() {
        RequisitionDef[] array = new RequisitionDef[0];
        return (RequisitionDef[]) this.requisitionDefList.toArray(array);
    }

    /**
     * Method getRequisitionDefCollection.Returns a reference to
     * 'requisitionDefList'. No type checking is performed on any modifications to
     * the Vector.
     * 
     * @return a reference to the Vector backing this class
     */
    public List<RequisitionDef> getRequisitionDefCollection() {
        return this.requisitionDefList;
    }

    /**
     * Method getRequisitionDefCount.
     * 
     * @return the size of this collection
     */
    public int getRequisitionDefCount() {
        return this.requisitionDefList.size();
    }

    /**
     * Returns the value of field 'requistionDir'.
     * 
     * @return the value of field 'RequistionDir'.
     */
    public String getRequistionDir() {
        return this.requistionDir != null ? this.requistionDir : DEFAULT_REQUISITION_DIR;
    }

    /**
     * Returns the value of field 'rescanThreads'.
     * 
     * @return the value of field 'RescanThreads'.
     */
    public Long getRescanThreads() {
        return this.rescanThreads != null ? this.rescanThreads : Long.valueOf("10");
    }

    /**
     * Returns the value of field 'scanThreads'.
     * 
     * @return the value of field 'ScanThreads'.
     */
    public Long getScanThreads() {
        return this.scanThreads != null ? this.scanThreads : Long.valueOf("10");
    }

    /**
     * Returns the value of field 'writeThreads'.
     * 
     * @return the value of field 'WriteThreads'.
     */
    public Long getWriteThreads() {
        return this.writeThreads != null ? this.writeThreads : Long.valueOf("8");
    }

    /**
     * Method hasImportThreads.
     * 
     * @return true if at least one ImportThreads has been added
     */
    public boolean hasImportThreads() {
        return this.importThreads != null;
    }

    /**
     * Method hasRescanThreads.
     * 
     * @return true if at least one RescanThreads has been added
     */
    public boolean hasRescanThreads() {
        return this.rescanThreads != null;
    }

    /**
     * Method hasScanThreads.
     * 
     * @return true if at least one ScanThreads has been added
     */
    public boolean hasScanThreads() {
        return this.scanThreads != null;
    }

    /**
     * Method hasWriteThreads.
     * 
     * @return true if at least one WriteThreads has been added
     */
    public boolean hasWriteThreads() {
        return this.writeThreads != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            importThreads, 
            scanThreads, 
            rescanThreads, 
            writeThreads, 
            requistionDir, 
            foreignSourceDir, 
            requisitionDefList);
        return hash;
    }

    /**
     * Method iterateRequisitionDef.
     * 
     * @return an Iterator over all possible elements in this collection
     */
    public Iterator<RequisitionDef> iterateRequisitionDef() {
        return this.requisitionDefList.iterator();
    }

    /**
     */
    public void removeAllRequisitionDef() {
        this.requisitionDefList.clear();
    }

    /**
     * Method removeRequisitionDef.
     * 
     * @param vRequisitionDef
     * @return true if the object was removed from the collection.
     */
    public boolean removeRequisitionDef(final RequisitionDef vRequisitionDef) {
        boolean removed = requisitionDefList.remove(vRequisitionDef);
        return removed;
    }

    /**
     * Method removeRequisitionDefAt.
     * 
     * @param index
     * @return the element removed from the collection
     */
    public RequisitionDef removeRequisitionDefAt(final int index) {
        Object obj = this.requisitionDefList.remove(index);
        return (RequisitionDef) obj;
    }

    /**
     * Sets the value of field 'foreignSourceDir'.
     * 
     * @param foreignSourceDir the value of field 'foreignSourceDir'.
     */
    public void setForeignSourceDir(final String foreignSourceDir) {
        this.foreignSourceDir = foreignSourceDir;
    }

    /**
     * Sets the value of field 'importThreads'.
     * 
     * @param importThreads the value of field 'importThreads'.
     */
    public void setImportThreads(final Long importThreads) {
        this.importThreads = importThreads;
    }

    /**
     * 
     * 
     * @param index
     * @param vRequisitionDef
     * @throws IndexOutOfBoundsException if the index given is outside
     * the bounds of the collection
     */
    public void setRequisitionDef(final int index, final RequisitionDef vRequisitionDef) throws IndexOutOfBoundsException {
        // check bounds for index
        if (index < 0 || index >= this.requisitionDefList.size()) {
            throw new IndexOutOfBoundsException("setRequisitionDef: Index value '" + index + "' not in range [0.." + (this.requisitionDefList.size() - 1) + "]");
        }
        
        this.requisitionDefList.set(index, vRequisitionDef);
    }

    /**
     * 
     * 
     * @param vRequisitionDefArray
     */
    public void setRequisitionDef(final RequisitionDef[] vRequisitionDefArray) {
        //-- copy array
        requisitionDefList.clear();
        
        for (int i = 0; i < vRequisitionDefArray.length; i++) {
                this.requisitionDefList.add(vRequisitionDefArray[i]);
        }
    }

    /**
     * Sets the value of 'requisitionDefList' by copying the given Vector. All
     * elements will be checked for type safety.
     * 
     * @param vRequisitionDefList the Vector to copy.
     */
    public void setRequisitionDef(final List<RequisitionDef> vRequisitionDefList) {
        // copy vector
        this.requisitionDefList.clear();
        
        this.requisitionDefList.addAll(vRequisitionDefList);
    }

    /**
     * Sets the value of 'requisitionDefList' by setting it to the given Vector.
     * No type checking is performed.
     * @deprecated
     * 
     * @param requisitionDefList the Vector to set.
     */
    public void setRequisitionDefCollection(final List<RequisitionDef> requisitionDefList) {
        this.requisitionDefList = requisitionDefList;
    }

    /**
     * Sets the value of field 'requistionDir'.
     * 
     * @param requistionDir the value of field 'requistionDir'.
     */
    public void setRequistionDir(final String requistionDir) {
        this.requistionDir = requistionDir;
    }

    /**
     * Sets the value of field 'rescanThreads'.
     * 
     * @param rescanThreads the value of field 'rescanThreads'.
     */
    public void setRescanThreads(final Long rescanThreads) {
        this.rescanThreads = rescanThreads;
    }

    /**
     * Sets the value of field 'scanThreads'.
     * 
     * @param scanThreads the value of field 'scanThreads'.
     */
    public void setScanThreads(final Long scanThreads) {
        this.scanThreads = scanThreads;
    }

    /**
     * Sets the value of field 'writeThreads'.
     * 
     * @param writeThreads the value of field 'writeThreads'.
     */
    public void setWriteThreads(final Long writeThreads) {
        this.writeThreads = writeThreads;
    }

}
