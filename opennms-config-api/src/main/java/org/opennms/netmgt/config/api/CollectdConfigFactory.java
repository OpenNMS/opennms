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
package org.opennms.netmgt.config.api;

import java.io.IOException;
import java.util.List;

import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;

/**
 * @author <a href="mailto:jamesz@opennms.com">James Zuo</a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 */
public interface CollectdConfigFactory {
	void reload() throws IOException;
	void saveCurrent() throws IOException;
	CollectdConfiguration getLocalCollectdConfig();
	public Integer getThreads();
	boolean packageExists(String name);
	Package getPackage(final String name);
	public List<Package> getPackages();
	public List<Collector> getCollectors();
	boolean domainExists(final String name);
	boolean isServiceCollectionEnabled(final OnmsMonitoredService service);
	boolean isServiceCollectionEnabled(final OnmsIpInterface iface, final String svcName);
	boolean isServiceCollectionEnabled(final String ipAddr, final String svcName);
	boolean interfaceInFilter(String iface, Package pkg);
	boolean interfaceInPackage(final String iface, Package pkg);
	boolean interfaceInPackage(final OnmsIpInterface iface, Package pkg);
	void setExternalData(final List<Package> packages, final List<Collector> collectors);
}
