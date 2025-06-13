* This file is part of OpenNMS(R).
*
* Copyright (C) 2025 The OpenNMS Group, Inc.
* OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.measurements.filters.impl;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LoadData {

    protected final static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES,true);
        mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS,true);
    }

    private static final String firstArrayOfValue=
            "[{Load: NaN, timestamp: 1.732623E12}, {Load: 2.3, timestamp: 1.7326233E12}, {Load: 1.8334154666666667, timestamp: 1.7326236E12}, {Load: 1.6969653333333334, timestamp: 1.7326239E12}, {Load: 1.5994372000000001, timestamp: 1.7326242E12}, {Load: 1.4341295333333333, timestamp: 1.7326245E12}, {Load: 1.4953480666666668, timestamp: 1.7326248E12}, {Load: 1.4459626666666665, timestamp: 1.7326251E12}, {Load: 1.4572672333333334, timestamp: 1.7326254E12}, " +
                    "{Load: 1.6113477666666667, timestamp: 1.7326257E12}, {Load: 1.504247, timestamp: 1.732626E12}, {Load: 1.3507988333333334, timestamp: 1.7326263E12}, {Load: 1.2941548, timestamp: 1.7326266E12}, {Load: 1.477585, timestamp: 1.7326269E12}, {Load: 1.7080315000000001, timestamp: 1.7326272E12}, {Load: 2.36085, timestamp: 1.7326275E12}, {Load: 2.9174625, timestamp: 1.7326278E12}, {Load: 2.732814266666667, timestamp: 1.7326281E12}, " +
                    "{Load: 2.2755045000000003, timestamp: 1.7326284E12}, {Load: 1.6708403, timestamp: 1.7326287E12}, {Load: 1.4709091666666667, timestamp: 1.732629E12}, {Load: 1.5814612333333335, timestamp: 1.7326293E12}, {Load: 1.345355, timestamp: 1.7326296E12}, {Load: 1.5043948, timestamp: 1.7326299E12}, {Load: 2.0734238333333335, timestamp: 1.7326302E12}, {Load: 1.8433515000000003, timestamp: 1.7326305E12}, {Load: 1.4480430666666666, timestamp: 1.7326308E12}, " +
                    "{Load: 1.5140407, timestamp: 1.7326311E12}, {Load: 1.6470922666666667, timestamp: 1.7326314E12}, {Load: 1.5975026666666667, timestamp: 1.7326317E12}, {Load: 1.5934968999999999, timestamp: 1.732632E12}, {Load: 1.4603045666666667, timestamp: 1.7326323E12}, {Load: 1.7643385999999999, timestamp: 1.7326326E12}, {Load: 1.657308533333333, timestamp: 1.7326329E12}, {Load: 1.6436730000000002, timestamp: 1.7326332E12}, {Load: 1.8063498666666669, timestamp: 1.7326335E12}, " +
                    "{Load: 1.5891790000000001, timestamp: 1.7326338E12}, {Load: 1.3490829, timestamp: 1.7326341E12}, {Load: 1.4693056, timestamp: 1.7326344E12}, {Load: 1.5882341333333334, timestamp: 1.7326347E12}, {Load: 1.8092199, timestamp: 1.732635E12}, {Load: 1.7346240000000002, timestamp: 1.7326353E12}, {Load: 1.5345828000000001, timestamp: 1.7326356E12}, {Load: 1.6975562666666666, timestamp: 1.7326359E12}, {Load: 1.9960614666666667, timestamp: 1.7326362E12}, " +
                    "{Load: 1.8785454000000001, timestamp: 1.7326365E12}, {Load: 1.690535, timestamp: 1.7326368E12}, {Load: 2.0739177333333334, timestamp: 1.7326371E12}, {Load: 2.249542466666667, timestamp: 1.7326374E12}, {Load: 1.9728408333333336, timestamp: 1.7326377E12}, {Load: 1.7000496666666667, timestamp: 1.732638E12}, {Load: 1.7387256666666668, timestamp: 1.7326383E12}, {Load: 1.8121746, timestamp: 1.7326386E12}, {Load: 1.5660853333333333, timestamp: 1.7326389E12}, " +
                    "{Load: 1.4647701999999998, timestamp: 1.7326392E12}, {Load: 1.8838901333333333, timestamp: 1.7326395E12}, {Load: 1.6907516, timestamp: 1.7326398E12}, {Load: 1.4694273333333334, timestamp: 1.7326401E12}, {Load: 1.4158627, timestamp: 1.7326404E12}, {Load: 1.2887408333333332, timestamp: 1.7326407E12}, {Load: 1.4300443999999999, timestamp: 1.732641E12}, {Load: 1.4444888000000002, timestamp: 1.7326413E12}, {Load: 1.4054076, timestamp: 1.7326416E12}, " +
                    "{Load: 1.7406400666666668, timestamp: 1.7326419E12}, {Load: 1.9189550333333334, timestamp: 1.7326422E12}, {Load: 1.7359551999999998, timestamp: 1.7326425E12}, {Load: 1.7357926, timestamp: 1.7326428E12}, {Load: 2.0664267333333335, timestamp: 1.7326431E12}, {Load: 1.7626766666666667, timestamp: 1.7326434E12}, {Load: 1.64, timestamp: 1.7326437E12}, {Load: 1.6250301, timestamp: 1.732644E12}, {Load: 1.7646332666666666, timestamp: 1.7326443E12}, " +
                    "{Load: 1.4316430000000002, timestamp: 1.7326446E12}, {Load: 1.37, timestamp: 1.7326449E12}]";

    private final static String secondArrayOfValue=
            "[{Load: 1.5100000000000002, timestamp: 1.7326236E12}, {Load: 1.6969653333333334, timestamp: 1.7326239E12}, {Load: 1.5994372000000001, timestamp: 1.7326242E12}, {Load: 1.4341295333333333, timestamp: 1.7326245E12}, {Load: 1.4953480666666668, timestamp: 1.7326248E12}, {Load: 1.4459626666666665, timestamp: 1.7326251E12}, {Load: 1.4572672333333334, timestamp: 1.7326254E12}, {Load: 1.6113477666666667, timestamp: 1.7326257E12}, {Load: 1.504247, timestamp: 1.732626E12}, " +
                    "{Load: 1.3507988333333334, timestamp: 1.7326263E12}, {Load: 1.2941548, timestamp: 1.7326266E12}, {Load: 1.477585, timestamp: 1.7326269E12}, {Load: 1.7080315000000001, timestamp: 1.7326272E12}, {Load: 2.36085, timestamp: 1.7326275E12}, {Load: 2.9174625, timestamp: 1.7326278E12}, {Load: 2.732814266666667, timestamp: 1.7326281E12}, {Load: 2.2755045000000003, timestamp: 1.7326284E12}, {Load: 1.6708403, timestamp: 1.7326287E12}, {Load: 1.4709091666666667, timestamp: 1.732629E12}, " +
                    "{Load: 1.5814612333333335, timestamp: 1.7326293E12}, {Load: 1.345355, timestamp: 1.7326296E12}, {Load: 1.5043948, timestamp: 1.7326299E12}, {Load: 2.0734238333333335, timestamp: 1.7326302E12}, {Load: 1.8433515000000003, timestamp: 1.7326305E12}, {Load: 1.4480430666666666, timestamp: 1.7326308E12}, {Load: 1.5140407, timestamp: 1.7326311E12}, {Load: 1.6470922666666667, timestamp: 1.7326314E12}, {Load: 1.5975026666666667, timestamp: 1.7326317E12}, {Load: 1.5934968999999999, timestamp: 1.732632E12}, " +
                    "{Load: 1.4603045666666667, timestamp: 1.7326323E12}, {Load: 1.7643385999999999, timestamp: 1.7326326E12}, {Load: 1.657308533333333, timestamp: 1.7326329E12}, {Load: 1.6436730000000002, timestamp: 1.7326332E12}, {Load: 1.8063498666666669, timestamp: 1.7326335E12}, {Load: 1.5891790000000001, timestamp: 1.7326338E12}, {Load: 1.3490829, timestamp: 1.7326341E12}, {Load: 1.4693056, timestamp: 1.7326344E12}, {Load: 1.5882341333333334, timestamp: 1.7326347E12}, {Load: 1.8092199, timestamp: 1.732635E12}, " +
                    "{Load: 1.7346240000000002, timestamp: 1.7326353E12}, {Load: 1.5345828000000001, timestamp: 1.7326356E12}, {Load: 1.6975562666666666, timestamp: 1.7326359E12}, {Load: 1.9960614666666667, timestamp: 1.7326362E12}, {Load: 1.8785454000000001, timestamp: 1.7326365E12}, {Load: 1.690535, timestamp: 1.7326368E12}, {Load: 2.0739177333333334, timestamp: 1.7326371E12}, {Load: 2.249542466666667, timestamp: 1.7326374E12}, {Load: 1.9728408333333336, timestamp: 1.7326377E12}, {Load: 1.7000496666666667, timestamp: 1.732638E12}, " +
                    "{Load: 1.7387256666666668, timestamp: 1.7326383E12}, {Load: 1.8121746, timestamp: 1.7326386E12}, {Load: 1.5660853333333333, timestamp: 1.7326389E12}, {Load: 1.4647701999999998, timestamp: 1.7326392E12}, {Load: 1.8838901333333333, timestamp: 1.7326395E12}, {Load: 1.6907516, timestamp: 1.7326398E12}, {Load: 1.4694273333333334, timestamp: 1.7326401E12}, {Load: 1.4158627, timestamp: 1.7326404E12}, {Load: 1.2887408333333332, timestamp: 1.7326407E12}, {Load: 1.4300443999999999, timestamp: 1.732641E12}, " +
                    "{Load: 1.4444888000000002, timestamp: 1.7326413E12}, {Load: 1.4054076, timestamp: 1.7326416E12}, {Load: 1.7406400666666668, timestamp: 1.7326419E12}, {Load: 1.9189550333333334, timestamp: 1.7326422E12}, {Load: 1.7359551999999998, timestamp: 1.7326425E12}, {Load: 1.7357926, timestamp: 1.7326428E12}, {Load: 2.0664267333333335, timestamp: 1.7326431E12}, {Load: 1.7626766666666667, timestamp: 1.7326434E12}, {Load: 1.64, timestamp: 1.7326437E12}, {Load: 1.6250301, timestamp: 1.732644E12}, {Load: 1.7646332666666666, timestamp: 1.7326443E12}, " +
                    "{Load: 1.4316430000000002, timestamp: 1.7326446E12}, {Load: 1.5742506666666667, timestamp: 1.7326449E12}, {Load: 1.72, timestamp: 1.7326452E12}, {Load: NaN, timestamp: 1.7326455E12}]";

    protected static Entry[] getEntries(String json) throws IOException {
        return mapper.readValue(json, Entry[].class);
    }


    protected static Entry[] getFirst() throws IOException {
        return getEntries(firstArrayOfValue);
    }

    protected static Entry[] getSecond() throws IOException {
        return getEntries(secondArrayOfValue);
    }

    protected static Entry[] getThird() throws IOException {
        FileInputStream fis = new FileInputStream("src/test/resources/trendline.txt");
        return getEntries(IOUtils.toString(fis, StandardCharsets.UTF_8));
    }

    protected static class Entry {
        @JsonProperty("Load")
        public Double load;
        @JsonProperty("timestamp")
        public Double timestamp;
    }

}
