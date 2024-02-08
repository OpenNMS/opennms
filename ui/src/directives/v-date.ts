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

import { format as fnsFormat } from 'date-fns-tz'
import { parseISO } from 'date-fns'
import { AppInfo } from '@/types'
import { useInfoStore } from '@/stores/infoStore'

const infoStore = computed(() => useInfoStore())
const appInfo = computed<AppInfo>(() => infoStore.value.info)

const timeZone = computed<string>(
  () => appInfo.value.datetimeformatConfig?.zoneId || Intl.DateTimeFormat().resolvedOptions().timeZone
)

const formatString = computed<string>(
  // eslint-disable-next-line quotes
  () => appInfo.value.datetimeformatConfig?.datetimeformat || "yyyy-MM-dd'T'HH:mm:ssxxx"
)

const dateFormatDirective = {
  mounted(el: Element) {
    if (!el.innerHTML) {
      return
    }

    const date = Number(el.innerHTML) || parseISO(el.innerHTML)

    if (!date) {
      return
    }

    const formattedDate = fnsFormat(date, formatString.value, { timeZone: timeZone.value })
    el.innerHTML = formattedDate
  }
}

export default dateFormatDirective
