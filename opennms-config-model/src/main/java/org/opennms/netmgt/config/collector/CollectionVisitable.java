package org.opennms.netmgt.config.collector;

public interface CollectionVisitable {

	/**
	 * Provide a way to visit all the values in the CollectionSet, for any appropriate purposes (persisting, thresholding, or others)
	 * The expectation is that calling this method will ultimately call visitResource, visitGroup and visitAttribute (as appropriate)
	 *
	 * @param visitor a {@link org.opennms.netmgt.config.collector.CollectionSetVisitor} object.
	 */
	void visit(CollectionSetVisitor visitor);

}
