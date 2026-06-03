<!--
Licensed to The OpenNMS Group, Inc (TOG) under one or more
contributor license agreements.  See the LICENSE.md file
distributed with this work for additional information
regarding copyright ownership.

TOG licenses this file to You under the GNU Affero General
Public License Version 3 (the "License") or (at your option)
any later version.  You may not use this file except in
compliance with the License.  You may obtain a copy of the
License at:

     https://www.gnu.org/licenses/agpl-3.0.txt

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied.  See the License for the specific
language governing permissions and limitations under the
License.
-->

<!-- Dashboard panel replicating the legacy "Availability Over the Past 24 Hours" box. -->
<template>
  <div class="avail">
    <p
      v-if="loading"
      class="avail__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!sections.length"
      class="avail__muted"
    >
      No availability data.
    </p>
    <table
      v-else
      class="avail__table"
    >
      <thead>
        <tr>
          <th class="avail__cat">Categories</th>
          <th class="avail__num">Outages</th>
          <th class="avail__num">Availability</th>
        </tr>
      </thead>
      <tbody>
        <template
          v-for="section in sections"
          :key="section.name"
        >
          <tr
            v-for="cat in section.categories"
            :key="section.name + '/' + cat.name"
          >
            <td class="avail__cat">{{ cat.name }}</td>
            <td class="avail__num">{{ cat.outageText }}</td>
            <td
              class="avail__num"
              :style="{ color: severityColor(cat.availabilityClass) }"
            >{{ cat.availabilityText }}</td>
          </tr>
        </template>
        <tr class="avail__total">
          <td class="avail__cat">Overall Service Availability</td>
          <td class="avail__num">{{ overall.down }} of {{ overall.total }}</td>
          <td class="avail__num">{{ overall.pct.toFixed(3) }}%</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getAvailability, type AvailabilitySection } from '@/services/availabilityService'
import { severityColor } from '../severity'

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const sections = ref<AvailabilitySection[]>([])

const overall = computed(() => {
  let down = 0
  let total = 0
  for (const s of sections.value) {
    for (const c of s.categories) {
      const m = /(\d+)\s+of\s+(\d+)/.exec(c.outageText)
      if (m) {
        down += Number(m[1])
        total += Number(m[2])
      }
    }
  }
  return { down, total, pct: total > 0 ? ((total - down) / total) * 100 : 100 }
})

const load = async () => {
  loading.value = true
  sections.value = await getAvailability()
  loading.value = false
}

onMounted(load)
watch(() => props.refreshTick, load)
</script>

<style scoped lang="scss">
.avail {
  font-size: 0.875rem;

  &__muted {
    color: var(--feather-secondary-text-on-surface, #666);
  }

  &__table {
    width: 100%;
    border-collapse: collapse;
  }

  th {
    text-align: left;
    font-weight: 600;
    border-bottom: 2px solid var(--feather-border-on-surface, #ccc);
    padding: 0.25rem 0.5rem;
  }

  td {
    padding: 0.2rem 0.5rem;
    border-bottom: 1px solid var(--feather-border-on-surface, #eee);
  }

  &__num {
    text-align: right;
    white-space: nowrap;
  }

  &__cat {
    text-align: left;
  }

  &__total td {
    font-weight: 700;
    border-top: 2px solid var(--feather-border-on-surface, #ccc);
  }
}
</style>
