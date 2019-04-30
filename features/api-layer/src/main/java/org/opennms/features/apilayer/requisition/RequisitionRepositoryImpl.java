/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.apilayer.requisition;

import java.util.Objects;

import org.mapstruct.factory.Mappers;
import org.opennms.features.apilayer.requisition.mappers.RequisitionMapper;
import org.opennms.integration.api.v1.config.requisition.Requisition;
import org.opennms.integration.api.v1.requisition.RequisitionRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;

public class RequisitionRepositoryImpl implements RequisitionRepository {
    private static final RequisitionMapper MAPPER = Mappers.getMapper(RequisitionMapper.class);

    private final ForeignSourceRepository deployedForeignSourceRepository;

    public RequisitionRepositoryImpl(ForeignSourceRepository deployedForeignSourceRepository) {
        this.deployedForeignSourceRepository = Objects.requireNonNull(deployedForeignSourceRepository);
    }

    @Override
    public Requisition getDeployedRequisition(String foreignSource) {
        return MAPPER.map(deployedForeignSourceRepository.getRequisition(foreignSource));
    }
}
