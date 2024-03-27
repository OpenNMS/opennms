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
package org.opennms.netmgt.provision.persist.rpc;

import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class RequisitionResponseDTOTest extends XmlTestNoCastor<RequisitionResponseDTO> {

    public RequisitionResponseDTOTest(RequisitionResponseDTO sampleObject, String sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        RequisitionResponseDTO response = new RequisitionResponseDTO();
        response.setErrorMessage("no!");

        // This causes problems due to the date formatting
        //Requisition requisition = new Requisition();
        //requisition.setDate(new Date(0));
        //response.setRequisition(requisition);
        
        return Arrays.asList(new Object[][] {
            {
                response,
                "<requisition-response error=\"no!\"></requisition-response>"
            }
        });
    }
}
