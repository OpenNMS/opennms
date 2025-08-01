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

import { Plugin } from '@/types'
import { MenuItem } from '@/types/mainMenu'

const createMenuItem = (id: string, name: string) => {
  return {
    id,
    name,
    url: null,
    locationMatch: null,
    roles: null
  } as MenuItem
}

const createTopMenuItem = (id: string, name: string, items: MenuItem[]) => {
  return {
    id,
    name,
    items,
    url: null,
    locationMatch: null,
    roles: null
  } as MenuItem
}

const computePluginRelLink = (plugin: Plugin) => {
  return `ui/#/plugins/${plugin.extensionId}/${plugin.resourceRootPath}/${plugin.moduleFileName}`
}

// Create a fake ALEC plugin for demo purposes
const createFakePlugin = () => {
  // Example: https://github.com/OpenNMS/alec/blob/develop/features/ui/src/main/resources/OSGI-INF/blueprint/blueprint.xml
  // <property name="id" value="alecUiExtension"/>
  // <property name="menuEntry" value="ALEC"/>
  // <property name="resourceRoot" value="ui-ext"/>
  // <property name="moduleFileName" value="alecUiExtension.es.js"/>

  const fakePlugin = {
    extensionId: 'alecUiExtension',
    menuEntry: 'ALEC',
    moduleFileName: 'alecUiExtension.es.js',
    resourceRootPath: 'ui-ext'
  } as Plugin

  return fakePlugin
}

export {
  computePluginRelLink,
  createFakePlugin,
  createMenuItem,
  createTopMenuItem
}
