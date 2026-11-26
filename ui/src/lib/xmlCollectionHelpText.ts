///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

// Help texts of the XML collector configuration pages.

export const COLLECTIONS_HELP = `An XML data collection fetches one or more documents (XML, JSON or HTML, depending on the collection handler)
from each node and stores the values its groups select. A collectd service uses a collection through its
"collection" parameter, e.g. <parameter key="collection" value="xml-elasticsearch-cluster-stats"/> together with
the handler class of the XmlCollector. Changes take effect with the next collection; no restart is needed.`

export const GROUP_FILES_HELP = `A group file holds groups that several sources share. A source collects the groups of every group file it
imports in addition to its own. Group files used to live in etc/xml-datacollection/; their id is the former file
name without ".xml". A group file that a source still imports cannot be deleted.`

export const XML_FIELD_HINTS = {
  rrdRepository: 'Directory the collected values are written to when RRD storage is used.',
  collectionName: 'Referenced by the "collection" parameter of a collectd service. Renaming does not update collectd services that use the old name.',
  step: 'Collection interval in seconds. Should match the interval of the collectd service.',
  rras: 'Round robin archives of the RRD files, RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>.',
  url: 'URL of the document. {ipaddr}, {nodeId}, {nodeLabel}, {foreignSource}, {foreignId}, {step} (the collection interval), {parameter:<key>} (a collectd service parameter) and node asset fields like {building} are replaced at collection time.',
  method: 'HTTP method of the request.',
  parameters: 'Request parameters understood by the collection handler, e.g. timeout, retries, disable-ssl-verification, use-system-proxy, pre-parse-html, xslt-source-file.',
  headers: 'HTTP headers sent with the request.',
  content: 'Body sent with a POST or PUT request.',
  importGroups: 'Group files whose groups are collected from this source in addition to its own groups.',
  groupFileId: 'Unique id of the group file, as used by the import of a source. Cannot be changed later.',
  resourceType: '"node" stores the values on the node itself; any other value must be a resource type defined in the data collection configuration.',
  resourceXpath: 'XPath (or JSONPath for JSON documents) selecting one element per resource.',
  keyXpath: 'XPath, relative to the resource element, of the resource instance name. Not needed for node-level groups.',
  timestampXpath: 'XPath of the time the values were measured. Without it the collection time is used.',
  timestampFormat: 'Format of the timestamp in java.text.SimpleDateFormat syntax, e.g. yyyy-MM-dd HH:mm:ss.',
  resourceKeys: 'XPaths whose values are joined to build the resource instance name; an alternative to a single key XPath.',
  objects: 'Values collected for each resource. The name is used as the data source name; keep it at 19 characters or less for RRD storage.',
  mappings: 'Maps collected strings to other values, e.g. "green" to 0. A mapping without "from" applies to every other value.'
}
