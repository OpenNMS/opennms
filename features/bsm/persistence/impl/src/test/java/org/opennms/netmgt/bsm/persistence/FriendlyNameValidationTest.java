/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.persistence;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;

public class FriendlyNameValidationTest {
    private static String FRIENDLYNAME_EMPTY="";
    private static String FRIENDLYNAME_THIRTY_CHARS ="012345678901234567890123456789";
    private static String FRIENDLYNAME_TOO_LONG = FRIENDLYNAME_THIRTY_CHARS + "X";

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void friendlyNameZero() {
        IdentityEntity identityEntity = new IdentityEntity();

        IPServiceEdgeEntity edge = new IPServiceEdgeEntity();
        edge.setFriendlyName(FRIENDLYNAME_EMPTY);
        edge.setMapFunction(identityEntity);

        Set<ConstraintViolation<IPServiceEdgeEntity>> constraintViolations = validator.validate( edge );

        Assert.assertEquals( 0, constraintViolations.size() );
    }

    @Test
    public void friendlyNameTooLong() {
        IdentityEntity identityEntity = new IdentityEntity();

        IPServiceEdgeEntity edge = new IPServiceEdgeEntity();
        edge.setFriendlyName(FRIENDLYNAME_TOO_LONG);
        edge.setMapFunction(identityEntity);

        validator.validate(edge);
        Set<ConstraintViolation<IPServiceEdgeEntity>> constraintViolations = validator.validate( edge );

        Assert.assertEquals( 1, constraintViolations.size() );
    }

    @Test
    public void friendlyNameMaximum() {
        IdentityEntity identityEntity = new IdentityEntity();

        IPServiceEdgeEntity edge = new IPServiceEdgeEntity();
        edge.setFriendlyName(FRIENDLYNAME_THIRTY_CHARS);
        edge.setMapFunction(identityEntity);

        validator.validate(edge);
        Set<ConstraintViolation<IPServiceEdgeEntity>> constraintViolations = validator.validate( edge );

        Assert.assertEquals( 0, constraintViolations.size() );
    }
}