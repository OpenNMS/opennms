--
-- Licensed to The OpenNMS Group, Inc (TOG) under one or more
-- contributor license agreements.  See the LICENSE.md file
-- distributed with this work for additional information
-- regarding copyright ownership.
--
-- TOG licenses this file to You under the GNU Affero General
-- Public License Version 3 (the "License") or (at your option)
-- any later version.  You may not use this file except in
-- compliance with the License.  You may obtain a copy of the
-- License at:
--
--      https://www.gnu.org/licenses/agpl-3.0.txt
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
-- either express or implied.  See the License for the specific
-- language governing permissions and limitations under the
-- License.
--

INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (1, 'opennms.snmp.trap.translator.events', '', 'opennms', 23, true, 8, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (2, 'opennms.ackd.events', '', 'opennms', 22, true, 1, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (3, 'opennms.alarm.events', '', 'opennms', 21, true, 3, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (4, 'opennms.bmp.events', '', 'opennms', 20, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (5, 'opennms.bsm.events', '', 'opennms', 19, true, 5, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (6, 'opennms.capsd.events', '', 'opennms', 18, true, 8, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (7, 'opennms.collectd.events', '', 'opennms', 17, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (8, 'opennms.config.events', '', 'opennms', 16, true, 12, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (9, 'opennms.correlation.events', '', 'opennms', 15, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (10, 'opennms.default.threshold.events', '', 'opennms', 14, true, 6, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (11, 'opennms.discovery.events', '', 'opennms', 13, true, 4, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (12, 'opennms.internal.events', '', 'opennms', 12, true, 27, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (13, 'opennms.linkd.events', '', 'opennms', 11, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (14, 'opennms.mib.events', '', 'opennms', 10, true, 7, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (15, 'opennms.pollerd.events', '', 'opennms', 9, true, 43, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (16, 'opennms.provisioning.events', '', 'opennms', 8, true, 8, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (17, 'opennms.minion.events', '', 'opennms', 7, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (18, 'opennms.perspective.poller.events', '', 'opennms', 6, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (19, 'opennms.reportd.events', '', 'opennms', 5, true, 2, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (20, 'opennms.syslogd.events', '', 'opennms', 4, true, 1, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (21, 'opennms.ticketd.events', '', 'opennms', 3, true, 5, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (22, 'opennms.tl1d.events', '', 'opennms', 2, true, 1, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');
INSERT INTO eventconf_sources(id, name, description, vendor, file_order, enabled, event_count, created_time, last_modified, uploaded_by) VALUES (23, 'opennms.catch-all.events', '', 'opennms', 1, true, 4, CURRENT_TIMESTAMP , CURRENT_TIMESTAMP , 'system-migration');

ALTER SEQUENCE eventconf_sources_id_seq RESTART WITH 24;
