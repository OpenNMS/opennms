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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysis;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysisDao;

public class AsnPathTest {


    @Test
    public void testAsnPath() {

        BmpMessagePersister bmpMessagePersister = new BmpMessagePersister();
        BmpAsnPathAnalysisDao bmpAsnPathAnalysisDao = Mockito.mock(BmpAsnPathAnalysisDao.class);
        // Mock that no data returned from Dao
        Mockito.when(bmpAsnPathAnalysisDao.findByAsnPath(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(null);
        bmpMessagePersister.setBmpAsnPathAnalysisDao(bmpAsnPathAnalysisDao);

        List<BmpAsnPathAnalysis> bmpAsnPathAnalysisList = bmpMessagePersister.buildBmpAsnPath("8319 33891 1299 2914 2907 2907 ");
        assertThat(bmpAsnPathAnalysisList, Matchers.hasSize(5));

        BmpAsnPathAnalysis bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(0);
        assertEquals(8319L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(0L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(33891L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(true, bmpAsnPathAnalysis.isAsnLeftPeering());

        bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(2);
        assertEquals(1299L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(33891L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(2914L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(false, bmpAsnPathAnalysis.isAsnLeftPeering());

        bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(4);
        assertEquals(2907L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(2914L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(0L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(false, bmpAsnPathAnalysis.isAsnLeftPeering());
        // Check Invalid path
        bmpAsnPathAnalysisList = bmpMessagePersister.buildBmpAsnPath("8319 33891A 1299 2914 2907 2907 ");
        assertThat(bmpAsnPathAnalysisList, Matchers.empty());
    }


    @Test
    public void testStringArrayToLongArray() {
        String[] asnPaths = {"8319", "ABC", "1299"};
        long[] asnArray = BmpMessagePersister.getLongArrayFromStringArray(asnPaths);
        assertTrue(asnArray.length == 0);
        String[] asnPaths1 = {"8319", "33891", "1299", "2914"};
        long[] asnArray1 = BmpMessagePersister.getLongArrayFromStringArray(asnPaths1);
        assertEquals(4L, asnArray1.length);
        long[] expected = {8319, 33891, 1299, 2914};
        assertArrayEquals(expected, asnArray1);
    }
}
