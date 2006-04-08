package org.opennms.netmgt.dao;

public class CollectionTest extends AbstractDaoTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
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
