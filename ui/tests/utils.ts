import { OnmsDatePicker } from '@opennms/onms-ui'
import { VueWrapper } from '@vue/test-utils'
import { nextTick } from 'vue'

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

export const findByText = (wrap: any, selector: string, text: string) => {
  return wrap.findAll(selector).filter((n: any) => n.text().match(text))[0]
}

// An OnmsDatePicker found by its data-test: read the text it shows, and pick a value the way
// a user does (null clears it).
export const datePicker = (wrap: VueWrapper, dataTest: string) => {
  const picker = wrap.findAllComponents(OnmsDatePicker).find(c => c.attributes('data-test') === dataTest)
  if (!picker) {
    throw new Error(`no date picker ${dataTest}`)
  }
  return picker
}

export const pickDate = async (wrap: VueWrapper, dataTest: string, value: Date | null) => {
  datePicker(wrap, dataTest).vm.$emit('update:modelValue', value)
  await nextTick()
}

// The duty schedule editor's time pickers (time only): the shown text, and a pick of 'HH:MM'
// ('' clears it).
export const dutyTime = (wrap: VueWrapper, dataTest: string): string =>
  (wrap.find(`[data-test="${dataTest}"] input`).element as HTMLInputElement).value

export const setDutyTime = async (wrap: VueWrapper, dataTest: string, hhmm: string) => {
  let value: Date | null = null
  if (hhmm) {
    const [hours, minutes] = hhmm.split(':')
    value = new Date()
    value.setHours(Number.parseInt(hours, 10), Number.parseInt(minutes, 10), 0, 0)
  }
  await pickDate(wrap, dataTest, value)
}
