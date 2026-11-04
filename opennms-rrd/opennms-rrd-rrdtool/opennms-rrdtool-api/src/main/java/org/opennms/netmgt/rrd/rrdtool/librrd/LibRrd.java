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
package org.opennms.netmgt.rrd.rrdtool.librrd;

/**
 * Thin Java view of the thread-safe librrd functions used by OpenNMS.
 *
 * <p>Obtain an instance from {@link LibRrdProvider}.
 */
public interface LibRrd {

    /** {@code rrd_create_r()} */
    void create(String filename, long step, long start, String[] argv) throws LibRrdException;

    /** {@code rrd_update_r()}; {@code template} may be {@code null}. */
    void update(String filename, String template, String[] argv) throws LibRrdException;

    /** {@code rrd_fetch_r()}; results start at the first row after {@code start}. */
    FetchResults fetch(String filename, String cf, long start, long end, long step) throws LibRrdException;

    /** {@code rrd_xport()} with {@code --start/--end/--step} (and {@code --maxrows} when &gt; 0) prepended to {@code argv}. */
    FetchResults xport(long start, long end, long step, long maxrows, String[] argv) throws LibRrdException;
}
