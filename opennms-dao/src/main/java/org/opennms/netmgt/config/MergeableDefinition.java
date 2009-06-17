//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.config.snmp.Definition;

/**
 * This is a wrapper class for the Definition class from the config pacakge.  Has the logic for 
 * comparing definitions, sorting child elements, etc.
 * 
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 *
 */
final class MergeableDefinition {

    /**
     * This field should remaing encapsulated for there is
     * synchronization in the getter.
     * 
     */
    private final Definition m_snmpConfigDef;
    
    public MergeableDefinition(Definition def) {
        m_snmpConfigDef = def;
    }

    /**
     * This compares the attributes (ignoring IP specifics and ranges
     * in the definitions) such as port, version, read-community, etc. to
     * determine if the definitions match.
     * 
     * @param def
     * @return a definition in the config matching the attributes of the 
     *   specified def or null if one is not found.
     */
    public boolean equals(Definition def) {
        boolean compares = false;
        Definition matchingDef = (Definition)def;
        
        if (compareStrings(getConfigDef().getReadCommunity(), matchingDef.getReadCommunity())
                && compareStrings(getConfigDef().getVersion(), matchingDef.getVersion())
                && getConfigDef().getPort() == matchingDef.getPort() 
                && getConfigDef().getRetry() == matchingDef.getRetry()
                && getConfigDef().getTimeout() == matchingDef.getTimeout()
                && compareStrings(getConfigDef().getVersion(), matchingDef.getVersion())) {
            compares = true;
        }
        return compares;
    }
    
    /**
     * simple little override
     */
    public boolean equals(Object obj) {
        boolean equals = false;
        
        if (obj == null) {
            equals = false;
        } else if (obj instanceof Definition) {
            equals = equals((Definition)obj);
        }
        return equals;
    }

    /**
     * dirty little hashcode impl
     */
    public int hashCode() {
        return 0;
    }
    
    /**
     * This method is called when a definition is found in the config and
     * that has the same attributes as the params in the configureSNMP event and 
     * the IP specific/range needs to be merged into the definition.
     * 
     * @param eventDef
     */
    protected void mergeMatchingAttributeDef(final Definition eventDef)  {
        if (defIsSpecific(eventDef)) {
            handleSpecificMerge(eventDef);
        } else {
            handleRangeMerge(eventDef);
        }
    }
    
    /**
     * Little helper method for determining if this definition
     * from a configureSNMP event containts a specific IP address vs.
     * a range.
     * 
     * @return true if the number of ranges in the def is 0.
     */
    public boolean isSpecific() {
        
        boolean specific = false;
        if (getConfigDef().getRangeCount() == 0) {
            specific = true;
        }
        return specific;
    }
    
    /**
     * Little helper method for determining if the definition created
     * from a configureSNMP event containts a specific IP address vs.
     * a range.
     * 
     * @param eventDef
     * @return true if the number of ranges in the def is 0.
     */
    public boolean defIsSpecific(Definition eventDef) {
        boolean specificDef = false;
        
        if (eventDef.getRangeCount() == 0) {
            specificDef = true;
        }
        return specificDef;
    }
    
    /**
     * This method handles the updating of the SnmpConfig when
     * the definition to be merged contains a specific IP address.
     * 
     * @param eventDef a definition created to represent the values
     *  passed in the configureSNMP event params
     */
    private void handleSpecificMerge(final Definition eventDef)  {
        
        if (hasRangeMatchingSpecific(eventDef.getSpecific(0))) {
            log().error("handleSpecificMerge: definition already contains a range that matches requested SNMP specific change: " + this);
        } else if (hasMatchingSpecific(eventDef.getSpecific(0))) {
            log().error("handleSpecificMerge: definition already contains a specific that matches requested SNMP specific change: " + this);
        } else {
            getConfigDef().addSpecific(eventDef.getSpecific(0));
            sortSpecifics();
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    /**
     * This method handles the updating of the SnmpConfig when
     * the definition to be merged contains a range.
     * 
     * @param eventDef a definition created to represent the values
     *  passed in the configureSNMP event params
     */
    private void handleRangeMerge(final Definition eventDef) {
        
        //first remove any specifics that would be eclipsed by this new range
        while (hasSpecificMatchingNewRange(eventDef.getRange(0))) {
            String spec = findSpecificMatchingNewRange(eventDef.getRange(0));
            getConfigDef().removeSpecific(spec);
        }
        mergeNewRangeIntoDef(eventDef.getRange(0));
    }
    
    /**
     * Enumerates over the list of specifics in a definition
     * looking for a matching specific element.  Assumes isSpecific()
     * returns true.
     * 
     * @param specific IP address as a string
     * @return true if def has a matching specific IP address already
     */
    public boolean hasMatchingSpecific(final String specific) {
        boolean specificMatches = false;
        for (String defSpecific : getConfigDef().getSpecificCollection()) {
            if (defSpecific.equals(specific)) {
                specificMatches = true;
                break;
            }
        }
        return specificMatches;
    }
    
    /**
     * Simple method to add readability
     * 
     * @param specific IP address
     * @return true if the definition has a range covering
     *  the specified specific IP address
     */
    public boolean hasRangeMatchingSpecific(final String specific) {
        return (findRangeMatchingSpecific(specific) != null);
    }
    
    /**
     * Analyzes the definition to determine if one of its ranges
     * matches the specified specific IP address
     * 
     * @param specific
     * @return the matching range
     */
    public Range findRangeMatchingSpecific(final String specific) {
        Range matchingRange = null;
        for (Range erange : getConfigDef().getRangeCollection()) {
            MergeableRange range = new MergeableRange(erange);
            if (range.coversSpecific(specific)) {
                matchingRange = range.getRange();
                break;
            }
        }
        return matchingRange;
    }
    
    /**
     * Responsible for merging new ranges in to this definition.
     * 
     * @param newRange
     */
    public void mergeNewRangeIntoDef(final Range newRange) {
        purgeEclipsedRangesInDef(newRange);
        
        if (getConfigDef().getRangeCount() == 0) {
            getConfigDef().addRange(newRange);
        } else {
            boolean overlapped = mergeOverlappingRanges(newRange);
            if(!overlapped) {
                getConfigDef().addRange(newRange);
            }
        }
    }
    
    /**
     * Removes in ranges in the defintion that are eclipsed
     * by the new range.
     * 
     * @param range
     */
    public void purgeEclipsedRangesInDef(final Range range) {
        Range[] ranges = getConfigDef().getRange();
        MergeableRange newRange = new MergeableRange(range);
        
        for (int i = 0; i < ranges.length; i++) {
            Range rng = ranges[i];
            
            if (newRange.eclipses(rng)) {
                getConfigDef().removeRange(rng);
            }
        }
    }
    
    /**
     * The passed range is evaluated against the existing ranges in the
     * definition and updates the def range if it overlaps or eclipses
     * a def range.
     * 
     * @param range
     * @return the state of having updated any ranges in the definition 
     *   due to being effected by the new range.
     */
    public boolean mergeOverlappingRanges(final Range range) {
        boolean overlapped = false;
        sortRanges();
        MergeableRange newRange = new MergeableRange(range);
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            Range defRange = ranges[i];
            
            if (newRange.equals(defRange)) {
                overlapped = true;
            } else if (newRange.overlapsBegin(defRange)) {
                defRange.setBegin(newRange.getRange().getBegin());
                overlapped = true;
            } else if (newRange.overlapsEnd(defRange)) {
                defRange.setEnd(newRange.getRange().getEnd());
                overlapped = true;
            } else if (newRange.eclipses(defRange)) {
                defRange.setBegin(newRange.getRange().getBegin());
                defRange.setEnd(newRange.getRange().getEnd());
                overlapped = true;
            }
        }
        sortRanges();
        return overlapped;
    }
    
    /**
     * Sorts the specifics in the current wrapped definition.
     *
     */
    public void sortSpecifics() {
        String[] specifics = getConfigDef().getSpecific();
        Arrays.sort(specifics, new SpecificComparator());
        getConfigDef().setSpecific(specifics);
    }
    
    /**
     * Sorts ranges with the current wrapped definition.
     */
    public void sortRanges() {
        Range[] ranges = getConfigDef().getRange();
        Arrays.sort(ranges, new RangeComparator());
        getConfigDef().setRange(ranges);
    }
    
    /**
     * @param def
     * @return
     */
    public boolean hasSpecificMatchingNewRange(final Range eventRange) {
        return (findSpecificMatchingNewRange(eventRange) != null);
    }
    
    /**
     * @param def
     * @return
     */
    public String findSpecificMatchingNewRange(final Range eventRange) {
        String matchingSpecific = null;
        for (String spec : getConfigDef().getSpecificCollection()) {
            MergeableRange range = new MergeableRange(eventRange);
            
            if (range.coversSpecific(spec)) {
                matchingSpecific = spec;
                break;
            }
        }
        return matchingSpecific;
    }
    
    final public Definition getConfigDef() {
        synchronized (m_snmpConfigDef) {
            return m_snmpConfigDef;
        }        
    }
    
    final private boolean compareStrings(String str1, String str2) {
        boolean match = false;
        if (str1 == null && str2 == null) {
            match = true;
        } else if (str1 == null || str2 == null) {
            match = false;
        } else if (str1.equals(str2)) {
            match = true;
        }
        return match;
    }

    /**
     * Removes the specified specific from the wrapped definition.
     * @param specific
     */
    public void purgeSpecificFromDef(final String specific) {
        String[] specs = getConfigDef().getSpecific();
        for (int i = 0; i < specs.length; i++) {
            String spec = specs[i];

            if (spec.equals(specific)) {
                getConfigDef().removeSpecific(spec);
            }
        }
        
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            MergeableRange range = new MergeableRange(ranges[i]);
            
            if (range.coversSpecific(specific)) {
                Range newRange = range.removeSpecificFromRange(specific);
                if (newRange != null) {
                    getConfigDef().addRange(newRange);
                }
            }
        }
    }
    
    /**
     * Call this method to optimize the specifics in the wrapped definition.
     */
    public void optimizeSpecifics() {
        adjustRangesWithAdjacentSpecifics();
        removeSpecificsEclipsedByRanges();
        convertAdjacentSpecficsIntoRange();
    }
    
    /**
     * This method moves specifics that are adjacent to ranges in to the range.
     */
    private void adjustRangesWithAdjacentSpecifics() {
        sortSpecifics();
        sortRanges();
        String[] specifics = getConfigDef().getSpecific();
        
        for (int i = 0; i < specifics.length; i++) {
            MergeableSpecific specific = new MergeableSpecific(specifics[i]);
            for (Range range : getConfigDef().getRangeCollection()) {
                if (specific.compareTo(range.getBegin()) == -1) {
                    getConfigDef().removeSpecific(specific.getSpecific());
                    range.setBegin(specific.getSpecific());
                } else if (specific.compareTo(range.getEnd()) == 1) {
                    getConfigDef().removeSpecific(specific.getSpecific());
                    range.setEnd(specific.getSpecific());
                }
            }
        }
        sortSpecifics();
        sortRanges();
    }
    
    /**
     * This method removes any specifics from the wrapped definition that are covered one of its ranges.
     *
     */
    private void removeSpecificsEclipsedByRanges() {
        sortSpecifics();
        String[] specifics = getConfigDef().getSpecific();
        for (int i = 0; i < specifics.length; i++) {
            String specific = specifics[i];
            if (hasRangeMatchingSpecific(specific)) {
                getConfigDef().removeSpecific(specific);
            }
        }
        sortSpecifics();
    }
    
    /**
     * Converts specifics into ranges when their diffs are abs() 1
     */
    private void convertAdjacentSpecficsIntoRange() {
        sortSpecifics();
        List<String> specificList = new ArrayList<String>(new LinkedHashSet<String>(getConfigDef().getSpecificCollection()));
        List<Range> definedRanges = Arrays.asList(getConfigDef().getRange());
        ArrayList<Range> newRanges = new ArrayList<Range>();
        
        if (specificList.size() > 1) {
            for (ListIterator<String> it = specificList.listIterator(); it.hasNext();) {
                final MergeableSpecific specific = new MergeableSpecific(it.next());
                final Range newRange = new Range();
                newRange.setBegin(specific.getSpecific());
                while (it.hasNext()) {
                    String nextSpecific = (String)it.next();
                    if (specific.compareTo(nextSpecific) == -1) {
                        newRange.setEnd(nextSpecific);
                        getConfigDef().removeSpecific(specific.getSpecific());
                        getConfigDef().removeSpecific(nextSpecific);
                        specific.setSpecific(nextSpecific);
                    } else {
                        it.previous();
                        break;
                    }
                }
                
                if (newRange.getEnd() != null) {
                    newRanges.add(newRange);
                }
            }
            newRanges.addAll(definedRanges);
            getConfigDef().setRange(newRanges);
            sortRanges();
        }
    }
    
    /**
     * Removes the specifics and ranges covered by the range specified in the parameter from the current wrapped definition.
     * @param eventRange
     */
    public void purgeRangeFromDef(final Range eventRange) {
        MergeableRange range = new MergeableRange(eventRange);
        
        sortSpecifics();
        String[] specs = getConfigDef().getSpecific();
        
        for (int i = 0; i < specs.length; i++) {
            String spec = specs[i];
            if (range.coversSpecific(spec)) {
                getConfigDef().removeSpecific(spec);
            }
        }
        
        sortRanges();
        Range[] ranges = getConfigDef().getRange();
        for (int i = 0; i < ranges.length; i++) {
            Range defRng = ranges[i];

            if (range.eclipses(defRng)) {
                getConfigDef().removeRange(defRng);
            } else if (range.withInRange(defRng)) {
                Range newRange = new Range();
                newRange.setBegin(InetAddressUtils.toIpAddrString((range.getLast().getValue()+1)));
                newRange.setEnd(defRng.getEnd());
                getConfigDef().addRange(newRange);
                defRng.setEnd(InetAddressUtils.toIpAddrString((range.getFirst().getValue()-1)));
            } else if (range.overlapsBegin(defRng)) {
                defRng.setBegin(InetAddressUtils.toIpAddrString((range.getLast().getValue()+1)));
            } else if (range.overlapsEnd(defRng)) {
                defRng.setEnd(InetAddressUtils.toIpAddrString((range.getFirst().getValue()-1)));
            }
        }
    }
    
    /**
     * Optimizes the ranges in the wrapped definition by making the definition as small and
     * as ordered as possible.
     */
    public void optimizeRanges() {
        sortRanges();
        Range[] ranges = getConfigDef().getRange();
        
        for (int i = 0; i < ranges.length-1; i++) {
            Range firstRange = ranges[i];
            Range nextRange = ranges[i+1];
            optimizeAdjacentRanges(firstRange, nextRange);
        }
        optimizeZeroLengthRanges();
    }
    
    /**
     * Converts ranges in the wrapped defintion that have equal begin and end addresses.
     */
    public void optimizeZeroLengthRanges() {
        Range[] ranges = getConfigDef().getRange();
        
        for (int i = 0; i < ranges.length; i++) {
            Range range = ranges[i];
            
            if (range.getBegin().equals(range.getEnd())) {
                if (!hasMatchingSpecific(range.getBegin())) {
                    getConfigDef().addSpecific(range.getBegin());
                }
                getConfigDef().removeRange(range);
            }
        }
    }
    
    /**
     * Adjusts 2 ordered ranges in the wrapped definition by determining if the nextRange is
     * either eclipsed, or overlapped by its previousRange.
     * 
     * @param previousRange
     * @param nextRange
     */
    private void optimizeAdjacentRanges(final Range previousRange, final Range nextRange) {
        MergeableRange range = new MergeableRange(previousRange);
        
        if (range.equals(nextRange)) {
            getConfigDef().removeRange(previousRange);
            
        } else if (range.eclipses(nextRange)) {
            
            //We have to do this because of the side effects of object references
            nextRange.setBegin(previousRange.getBegin());
            nextRange.setEnd(previousRange.getEnd());
            getConfigDef().removeRange(previousRange);
            
        } else if (range.isAdjacentToBegin(nextRange) || range.overlapsBegin(nextRange)){
            nextRange.setBegin(previousRange.getBegin());
            getConfigDef().removeRange(previousRange);
            
        } else if (range.isAdjacentToEnd(nextRange) || range.overlapsEnd(nextRange)) {
            
            //this "probably" should never happen
            nextRange.setEnd(previousRange.getEnd());
            getConfigDef().removeRange(previousRange);
        }
    }

}
