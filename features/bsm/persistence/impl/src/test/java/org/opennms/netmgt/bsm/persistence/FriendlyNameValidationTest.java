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