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
package org.opennms.netmgt.bsm.service.internal;

import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.DecreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IncreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionEntityVisitor;
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;

public class MapFunctionMapper {

    private static final MapFunctionVisitor<AbstractMapFunctionEntity> serviceToPersistenceMapping = new MapFunctionVisitor<AbstractMapFunctionEntity>() {

        @Override
        public AbstractMapFunctionEntity visit(Decrease decrease) {
            return new DecreaseEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Identity identity) {
            return new IdentityEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Ignore ignore) {
            return new IgnoreEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Increase increase) {
            return new IncreaseEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(SetTo setTo) {
            SetToEntity entity = new SetToEntity();
            entity.setSeverity(SeverityMapper.toSeverity(setTo.getStatus()));
            return entity;
        }
    };

    private static final MapFunctionEntityVisitor<MapFunction> persistenceToServiceMapping = new MapFunctionEntityVisitor<MapFunction>() {
        @Override
        public MapFunction visit(DecreaseEntity decreaseEntity) {
            return new Decrease();
        }

        @Override
        public MapFunction visit(IdentityEntity identityEntity) {
            return new Identity();
        }

        @Override
        public MapFunction visit(IgnoreEntity ignoreEntity) {
            return new Ignore();
        }

        @Override
        public MapFunction visit(IncreaseEntity increaseEntity) {
            return new Increase();
        }

        @Override
        public MapFunction visit(SetToEntity setToEntity) {
            SetTo result = new SetTo();
            result.setStatus(SeverityMapper.toStatus(setToEntity.getSeverity()));
            return result;
        }
    };

    public AbstractMapFunctionEntity toPersistenceFunction(MapFunction mapFunction) {
        AbstractMapFunctionEntity mapFunctionEntity = mapFunction.accept(serviceToPersistenceMapping);
        if (mapFunctionEntity == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapFunctionEntity;
    }

    public MapFunction toServiceFunction(AbstractMapFunctionEntity mapFunctionEntity) {
        if (mapFunctionEntity == null) {
            return null;
        }
        MapFunction mapFunction = mapFunctionEntity.accept(persistenceToServiceMapping);
        if (mapFunction == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapFunction;
    }
}
