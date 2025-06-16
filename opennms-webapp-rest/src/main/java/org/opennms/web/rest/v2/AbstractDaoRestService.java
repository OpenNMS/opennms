/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.rest.v2;

import java.io.Serializable;

/**
 * Abstract class for use by V2 endpoints that do not support DTOs.
 *
 * See {@link AbstractDaoRestServiceWithDTO} for more details.
 *
 */
public abstract class AbstractDaoRestService<T,Q,K extends Serializable,I extends Serializable> extends AbstractDaoRestServiceWithDTO<T,T,Q,K,I> {

    public T mapEntityToDTO(T entity) {
        return entity;
    }

    public T mapDTOToEntity(T dto) {
        return dto;
    }

}
