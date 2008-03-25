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
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
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
package org.opennms.netmgt.dao;

public class CollectionTest extends AbstractTransactionalDaoTestCase {
	
	public void testCreate() {
		/* in order to create an RRD I need to do the following:
		 * 0. Begin a transaction
		 * 1. Obtain a repository
		 *   a. This can be obtained from the package we are using to collect the data
		 * 2. Obtain a resource
		 *   a. Currently resources are given to us and are only entities
		 *   b. for non entity resources their definitions will be defined
		 *      in datacollection config and they will be associated with appropriate entities
		 * 3. Obtain a attribute definition
		 *   a. Attribute definitions are defined in datacollection config for collectd
		 *   b. Attribute definition for responseTime is hardcoded
		 * 4. Create an attribute from the three above
		 *   a. I suppose we need an attribute Dao that will create the attribute when we write it
		 *   b. It needs to find the attribute for a given (repo, resource, attrDef) triple
		 * 5. Add a value to the attribute
		 *   a. How will we store the values?
		 * 6. Save the attribute
		 *   a. SaveOrUpdate to the Dao will create the rrd if we need to and write the datapoints to it
		 * 7. Commit the transaction.
		 *   a. The data will actually be written/queued at the commit so we can push datapoints into common files
		 */
	}

}
