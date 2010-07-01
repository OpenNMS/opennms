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

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;

/**
 * This class handles merging a new Definition into the current running SNMP Configuration.
 *
 * @author <a href="mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public class SnmpConfigManager {

    private SnmpConfig m_config;
    
    /**
     * <p>Constructor for SnmpConfigManager.</p>
     *
     * @param config a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    public SnmpConfigManager(SnmpConfig config) {
        m_config = config;
    }

    /**
     * <p>getConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.snmp.SnmpConfig} object.
     */
    public SnmpConfig getConfig() {
        return m_config;
    }
    
    /**
     * This is the exposed method for moving the data from a configureSNMP event
     * into the SnmpConfig from SnmpPeerFactory.
     *
     * @param eventDef a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public void mergeIntoConfig(final Definition eventDef)  {
        
        synchronized (m_config) {
            MergeableDefinition matchingDef = findDefMatchingAttributes(eventDef);
            if (matchingDef != null) {
                matchingDef.mergeMatchingAttributeDef(eventDef);
            } else {
                matchingDef = new MergeableDefinition(eventDef);
                getConfig().addDefinition(matchingDef.getConfigDef());
            }
            purgeOtherDefs(matchingDef, eventDef);
            optimizeAllDefs();
        }        
    }
    
    
    /**
     * This method is used to find a definition with then current config that matches the
     * attributes of a Definition.
     *
     * @param eventDef a {@link org.opennms.netmgt.config.snmp.Definition} object.
     * @return a {@link org.opennms.netmgt.config.MergeableDefinition} object.
     */
    protected MergeableDefinition findDefMatchingAttributes(final Definition eventDef) {
        
        synchronized (m_config) {
            MergeableDefinition matchingDef = null;
            for (Definition def : getConfig().getDefinitionCollection()) {
                MergeableDefinition definition = new MergeableDefinition(def);

                if (definition.equals(eventDef)) {
                    matchingDef = new MergeableDefinition(def);
                    break;
                }
            }
            return matchingDef;
        }        
    }
    
    /**
     * This method purges specifics and ranges from definitions that don't
     * match the attributes specified in the event (the updateDef)
     * 
     * @param updatedDef
     * @param eventDef
     */
    private void purgeOtherDefs(final MergeableDefinition updatedDef, final Definition eventDef) {
        
        synchronized (m_config) {
            MergeableDefinition eventDefinition = new MergeableDefinition(eventDef);
            Definition[] defs = getConfig().getDefinition();
            for (int i = 0; i < defs.length; i++) {
                MergeableDefinition def = new MergeableDefinition(defs[i]);

                //don't mess with current updated def
                if (def.getConfigDef() == updatedDef.getConfigDef()) continue;

                if (eventDefinition.isSpecific()) {
                    def.purgeSpecificFromDef(eventDefinition.getConfigDef().getSpecific(0));
                } else {
                    def.purgeRangeFromDef(eventDefinition.getConfigDef().getRange(0));
                }
                
                //remove empty defintion
                if (def.getConfigDef().getRangeCount() < 1 && def.getConfigDef().getSpecificCount() < 1) {
                    getConfig().removeDefinition(def.getConfigDef());
                }
            }
        }
    }
    
    /**
     * Optimize all definitions in the current configuration.
     */
    public void optimizeAllDefs() {
        synchronized (m_config) {
            Definition[] defs = getConfig().getDefinition();
            for (int i = 0; i < defs.length; i++) {
                MergeableDefinition definition = new MergeableDefinition(defs[i]);
                if (definition.getConfigDef().getSpecificCount() > 0) {
                    definition.optimizeSpecifics();
                }
                if (definition.getConfigDef().getRangeCount() > 1) {
                    definition.optimizeRanges();
                }
            }
            getConfig().setDefinition(defs);
        }
    }
}
