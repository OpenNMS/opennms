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
import { MenuItem, TopMenuItem } from '@/types/mainMenu'

const createMenuItem = (id: string, name: string) => {
  return {
    id,
    name,
    url: null,
    className: null,
    locationMatch: null,
    icon: null,
    iconType: null,
    isIconOnly: null,
    isVueLink: null,
    roles: null
  } as MenuItem
}

const createTopMenuItem = (id: string, name: string, items: MenuItem[]) => {
  return {
    id,
    name,
    items,
    url: null,
    className: null,
    locationMatch: null,
    icon: null,
    iconType: null,
    isIconOnly: null,
    isVueLink: null,
    roles: null
  } as TopMenuItem
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

const TimeRegex = new RegExp(/(\d{2}:\d{2}:\d{2})(-|\+)(\d{2}):(\d{2})/, 'i')

interface FormattedDateTime {
  date: string
  time: string
  utcOp: string
  utc: string
  utcMinutes: string
}

const getFormattedDateTime = (value: string) => {
  // 2025-05-24T14:32:24-04:00 ->
  // {
  //   date: '2025-05-24',
  //   time: '14:32:24',
  //   utcOp: '-',
  //   utc: '04'
  //   utcMinutes: '00'
  // }

  const dateTime = value.split('T')
  const fullTime = dateTime[1]

  const matches = TimeRegex.exec(fullTime)

  const time = matches?.[1] ?? ''
  const utcOp = matches?.[2] ?? '+'
  const utc = matches?.[3] ?? ''
  const utcMinutes = matches?.[4] ?? ''

  return {
    date: dateTime[0],
    time,
    utcOp,
    utc,
    utcMinutes
  } as FormattedDateTime
}

export {
  computePluginRelLink,
  createFakePlugin,
  createMenuItem,
  createTopMenuItem,
  getFormattedDateTime,
  FormattedDateTime
}
