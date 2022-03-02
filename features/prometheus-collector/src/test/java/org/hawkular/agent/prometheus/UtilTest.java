/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus;

import org.junit.Assert;
import org.junit.Test;

public class UtilTest {

    @Test
    public void testConvertDoubleToString() {
        Assert.assertEquals("-Inf", Util.convertDoubleToString(Double.NEGATIVE_INFINITY));
        Assert.assertEquals("+Inf", Util.convertDoubleToString(Double.POSITIVE_INFINITY));
        Assert.assertEquals("NaN", Util.convertDoubleToString(Double.NaN));
    }

    @Test
    public void testConvertStringToDouble() {
        Assert.assertEquals(Double.NEGATIVE_INFINITY, Util.convertStringToDouble("-Inf"), 0.001);
        Assert.assertEquals(Double.POSITIVE_INFINITY, Util.convertStringToDouble("+Inf"), 0.001);
        Assert.assertEquals(Double.NaN, Util.convertStringToDouble("NaN"), 0.001);
    }
}
