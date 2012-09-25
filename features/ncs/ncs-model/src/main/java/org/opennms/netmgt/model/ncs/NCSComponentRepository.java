/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
