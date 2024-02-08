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
package org.opennms.netmgt.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.MarshalAndUnmarshalTest;

import com.google.common.collect.Lists;

public class OnmsMetaDataListTest extends MarshalAndUnmarshalTest<OnmsMetaDataList> {
    public OnmsMetaDataListTest(Class<OnmsMetaDataList> type, OnmsMetaDataList sampleObject, String expectedJson, String expectedXml) {
        super(type, sampleObject, expectedJson, expectedXml);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws IOException {
        OnmsMetaDataList listDTO = new OnmsMetaDataList();
        listDTO.setObjects(Lists.newArrayList(new OnmsMetaData("c1","k1","v1"), new OnmsMetaData("c2","k2","v2")));

        return Arrays.asList(new Object[][]{{
                OnmsMetaDataList.class,
                listDTO,
                "{\"offset\" : 0, \"count\" : 2, \"totalCount\" : 2, \"metaData\" : [ { \"context\" : \"c1\", \"key\" : \"k1\", \"value\" : \"v1\" }, { \"context\" : \"c2\", \"key\" : \"k2\", \"value\" : \"v2\" }] }",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><meta-data-list count=\"2\" offset=\"0\" totalCount=\"2\"><meta-data><context>c1</context><key>k1</key><value>v1</value></meta-data><meta-data><context>c2</context><key>k2</key><value>v2</value></meta-data></meta-data-list>"
        }});
    }
}
