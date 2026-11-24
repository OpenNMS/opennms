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
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

import SplitDateTimePicker from '@/components/Common/SplitDateTimePicker.vue'
import { datePicker, pickDate } from '../../utils'

const mountPicker = (modelValue: string) => mount(SplitDateTimePicker, {
  props: { modelValue, idPrefix: 'begin-1', label: 'Begin', dataTest: 'begin' },
  global: { plugins: [PrimeVue] }
})

const lastValue = (w: ReturnType<typeof mountPicker>) => w.emitted('update:modelValue')!.at(-1)![0]

describe('SplitDateTimePicker', () => {
  const zone = process.env.TZ
  afterEach(() => {
    process.env.TZ = zone
  })

  it('shows the date and the 12-hour time of the value in two pickers, each labelled', () => {
    const w = mountPicker('2026-03-05T17:30')
    expect(datePicker(w, 'begin-date').props()).toMatchObject({ showTime: false, timeOnly: false, modelValue: new Date(2026, 2, 5) })
    expect(datePicker(w, 'begin-time').props()).toMatchObject({ timeOnly: true, hourFormat: '12' })
    expect((w.find('[data-test="begin-time"] input').element as HTMLInputElement).value).toBe('05:30 pm')
    expect(w.find('label[for="begin-1-date"]').text()).toBe('Begin date')
    expect(w.find('label[for="begin-1-time"]').text()).toBe('Begin time')
  })

  it('changes only the part that was picked', async () => {
    const w = mountPicker('2026-03-05T17:30')
    await pickDate(w, 'begin-date', new Date(2026, 2, 6))
    expect(lastValue(w)).toBe('2026-03-06T17:30')
    await pickDate(w, 'begin-time', new Date(2000, 0, 10, 8, 15))
    expect(lastValue(w)).toBe('2026-03-05T08:15')
  })

  it('keeps a time picked before the date, and the date when the time is cleared', async () => {
    const w = mountPicker('T08:00')
    expect((w.find('[data-test="begin-time"] input').element as HTMLInputElement).value).toBe('08:00 am')
    await pickDate(w, 'begin-date', new Date(2026, 2, 6))
    expect(lastValue(w)).toBe('2026-03-06T08:00')
    await w.setProps({ modelValue: '2026-03-06T08:00' })
    await pickDate(w, 'begin-time', null)
    expect(lastValue(w)).toBe('2026-03-06T')
  })

  it('takes a time the browser skips on that day for daylight saving', async () => {
    process.env.TZ = 'America/New_York'
    // 2026-03-08 02:30 does not exist in New York; the server's zone may well have it
    const w = mountPicker('2026-03-08T')
    await pickDate(w, 'begin-time', new Date(2000, 0, 10, 2, 30))
    expect(lastValue(w)).toBe('2026-03-08T02:30')
    await w.setProps({ modelValue: '2026-03-08T02:30' })
    expect((w.find('[data-test="begin-time"] input').element as HTMLInputElement).value).toBe('02:30 am')
  })
})
