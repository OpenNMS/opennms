package org.opennms.netmgt.collection.api;

/**
 * Used by the {@link AbstractPersister} to persist numeric attributes.
 *
 * @author jwhite
 */
public interface PersistOperationBuilder {

    /**
     * Used to identify the builder in log messages in an exception is thrown during a call to commit().
     */
    public String getName();

    public void setNumericAttributeValue(CollectionAttributeType attributeType, Number value);

    public void setStringAttributeValue(CollectionAttributeType attributeType, String value);

    public void setAttributeMetadata(String metricIdentifier, String name);

    /**
     * Persists the attribute values and meta-data.
     *
     * @throws PersistException if an error occurs while persisting the attribute
     */
    public void commit() throws PersistException;

}
