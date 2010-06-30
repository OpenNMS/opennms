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
// Modifications
// 2006 Aug 15: Formatting - dj@opennms.org
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.model.RrdRepository;


/**
 * <p>OneToOnePersister class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OneToOnePersister extends BasePersister {

    /**
     * <p>Constructor for OneToOnePersister.</p>
     *
     * @param params a {@link org.opennms.netmgt.collectd.ServiceParameters} object.
     * @param repository a {@link org.opennms.netmgt.model.RrdRepository} object.
     */
    public OneToOnePersister(ServiceParameters params,  RrdRepository repository) {
        super(params, repository);
    }
    
    /** {@inheritDoc} */
    public void visitAttribute(CollectionAttribute attribute) {
        pushShouldPersist(attribute);
        if (shouldPersist()) {
            createBuilder(attribute.getResource(), attribute.getName(), attribute.getAttributeType());
            storeAttribute(attribute);
        }
    }

    /** {@inheritDoc} */
    public void completeAttribute(CollectionAttribute attribute) {
        if (shouldPersist()) {
        	commitBuilder();
        }
        popShouldPersist();
    }



}
