package org.opennms.netmgt.model.ncs;

import java.util.List;

import org.opennms.netmgt.model.OnmsCriteria;

public interface NCSComponentRepository {

	   
    /**
     * This is used to lock the table in order to implement upsert type operations
     */
    void lock();

    
    /**
     * <p>initialize</p>
     *
     * @param obj a {@link java.lang.Object} object.
     * @param <T> a T object.
     * @param <K> a K object.
     */
    void initialize(Object obj);

    /**
     * <p>flush</p>
     */
    void flush();

    /**
     * <p>clear</p>
     */
    void clear();

    /**
     * <p>countAll</p>
     *
     * @return a int.
     */
    int countAll();

    /**
     * <p>delete</p>
     *
     * @param entity a T object.
     */
    void delete(NCSComponent component);

    /**
     * <p>findAll</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<NCSComponent> findAll();
    
    /**
     * <p>findMatching</p>
     *
     * @param criteria a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a {@link java.util.List} object.
     */
    List<NCSComponent> findMatching(OnmsCriteria criteria);

    /**
     * <p>countMatching</p>
     *
     * @param onmsCrit a {@link org.opennms.netmgt.model.OnmsCriteria} object.
     * @return a int.
     */
    int countMatching(final OnmsCriteria onmsCrit);
    
    /**
     * <p>get</p>
     *
     * @param id a K object.
     * @return a T object.
     */
    NCSComponent get(Long id);

    /**
     * <p>load</p>
     *
     * @param id a K object.
     * @return a T object.
     */
    NCSComponent load(Long id);

    /**
     * <p>save</p>
     *
     * @param entity a T object.
     */
    void save(NCSComponent component);

    /**
     * <p>saveOrUpdate</p>
     *
     * @param entity a T object.
     */
    void saveOrUpdate(NCSComponent component);

    /**
     * <p>update</p>
     *
     * @param entity a T object.
     */
    void update(NCSComponent component);
    
	NCSComponent findByTypeAndForeignIdentity(String type, String foreignSource, String foreignId);
	
	List<NCSComponent> findComponentsThatDependOn(NCSComponent component);
	
	List<NCSComponent> findComponentsWithAttribute(String attrKey, String attrValue);


	List<NCSComponent> findComponentsByNodeId(int nodeid);


}
