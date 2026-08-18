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

import ExampleApp from './ExampleApp.vue'

// OpenNMS plugin contract: the host's externalComponent() injects this module
// via <script type="module"> and then mounts window[extensionId] as the root
// component (see ui/src/components/Plugin/utils.ts). The extensionId is
// derived from the module URL's second-to-last path segment.
;(window as unknown as Record<string, unknown>).exampleUiExtension = ExampleApp

export default ExampleApp
