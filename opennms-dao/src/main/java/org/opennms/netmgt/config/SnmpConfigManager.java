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
public abstract class SnmpConfigManager {

    /**
     * This is the exposed method for moving the data from a configureSNMP event
     * into the SnmpConfig from SnmpPeerFactory.
     *
     * @param eventDef a {@link org.opennms.netmgt.config.snmp.Definition} object.
     */
    public static void mergeIntoConfig(final SnmpConfig config, final Definition eventDef)  {
        MergeableDefinition matchingDef = findDefMatchingAttributes(config, eventDef);
        if (matchingDef != null) {
            matchingDef.mergeMatchingAttributeDef(eventDef);
        } else {
            matchingDef = new MergeableDefinition(eventDef);
            config.addDefinition(matchingDef.getConfigDef());
        }
        purgeOtherDefs(config, matchingDef, eventDef);
        optimizeAllDefs(config);
    }

    /**
     * This method is used to find a definition with then current config that matches the
     * attributes of a Definition.
     *
     * @param eventDef a {@link org.opennms.netmgt.config.snmp.Definition} object.
     * @return a {@link org.opennms.netmgt.config.MergeableDefinition} object.
     */
    public static MergeableDefinition findDefMatchingAttributes(final SnmpConfig config, final Definition eventDef) {
        MergeableDefinition matchingDef = null;
        for (Definition def : config.getDefinition()) {
            MergeableDefinition definition = new MergeableDefinition(def);

            if (definition.equals(eventDef)) {
                matchingDef = new MergeableDefinition(def);
                break;
            }
        }
        return matchingDef;
    }

    /**
     * This method purges specifics and ranges from definitions that don't
     * match the attributes specified in the event (the updateDef)
     * 
     * @param updatedDef
     * @param eventDef
     */
    private static void purgeOtherDefs(final SnmpConfig config, final MergeableDefinition updatedDef, final Definition eventDef) {
        MergeableDefinition eventDefinition = new MergeableDefinition(eventDef);
        Definition[] defs = config.getDefinition();
        for (int i = 0; i < defs.length; i++) {
            MergeableDefinition def = new MergeableDefinition(defs[i]);

            //don't mess with current updated definition
            if (def.getConfigDef() == updatedDef.getConfigDef()) continue;

            if (eventDefinition.isSpecific()) {
                def.purgeSpecificFromDef(eventDefinition.getConfigDef().getSpecific(0));
            } else {
                def.purgeRangeFromDef(eventDefinition.getConfigDef().getRange(0));
            }

            //remove empty definition
            if (def.getConfigDef().getRangeCount() < 1 && def.getConfigDef().getSpecificCount() < 1) {
                config.removeDefinition(def.getConfigDef());
            }
        }
    }

    /**
     * Optimize all definitions in the current configuration.
     */
    public static void optimizeAllDefs(SnmpConfig config) {
        Definition[] defs = config.getDefinition();
        for (int i = 0; i < defs.length; i++) {
            MergeableDefinition definition = new MergeableDefinition(defs[i]);
            if (definition.getConfigDef().getSpecificCount() > 0) {
                definition.optimizeSpecifics();
            }
            if (definition.getConfigDef().getRangeCount() > 1) {
                definition.optimizeRanges();
            }
        }
        config.setDefinition(defs);
    }
}
