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

<!--
  Dashboard panel replicating the legacy Status Overview, on Chart.js: two
  doughnuts — nodes grouped by unacknowledged alarms, and nodes grouped by
  current outages — from /api/v2/status/summary/nodes/{alarms,outages}.
-->
<template>
  <div class="status-overview">
    <div class="status-overview__donuts">
      <div class="status-overview__donut">
        <canvas ref="alarmsCanvas" />
        <div class="status-overview__center">
          <span class="status-overview__total">{{ alarmsTotal }}</span>
          <span class="status-overview__cap">Alarms</span>
        </div>
      </div>
      <div class="status-overview__donut">
        <canvas ref="outagesCanvas" />
        <div class="status-overview__center">
          <span class="status-overview__total">{{ outagesTotal }}</span>
          <span class="status-overview__cap">Outages</span>
        </div>
      </div>
    </div>
    <p
      v-if="loading"
      class="status-overview__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="alarmsTotal === 0 && outagesTotal === 0"
      class="status-overview__muted"
    >
      No alarms or outages.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Chart from 'chart.js/auto'
import type { PanelComponentProps } from '@/types/dashboard'
import { getNodesByAlarms, getNodesByOutages, type StatusSummaryEntry } from '@/services/statusService'

const props = defineProps<PanelComponentProps>()

// Legacy status-box palette (title-case labels from the summary endpoints).
const STATUS_COLORS: Record<string, string> = {
  Normal: '#336600',
  Warning: '#ffcc00',
  Minor: '#ff9900',
  Major: '#ff3300',
  Critical: '#cc0000',
  Indeterminate: '#808080',
  Cleared: '#9e9e9e'
}
const colorFor = (label: string) => STATUS_COLORS[label] ?? '#999999'

const alarmsCanvas = ref<HTMLCanvasElement | null>(null)
const outagesCanvas = ref<HTMLCanvasElement | null>(null)
const alarmsEntries = ref<StatusSummaryEntry[]>([])
const outagesEntries = ref<StatusSummaryEntry[]>([])
const loading = ref(true)

let alarmsChart: Chart<'doughnut', number[], string> | null = null
let outagesChart: Chart<'doughnut', number[], string> | null = null

const sum = (entries: StatusSummaryEntry[]) => entries.reduce((a, e) => a + e.count, 0)
const alarmsTotal = computed(() => sum(alarmsEntries.value))
const outagesTotal = computed(() => sum(outagesEntries.value))

const renderDonut = (
  canvas: HTMLCanvasElement | null,
  entries: StatusSummaryEntry[],
  existing: Chart<'doughnut', number[], string> | null
): Chart<'doughnut', number[], string> | null => {
  if (!canvas) return existing
  const filtered = entries.filter((e) => e.count > 0)
  const labels = filtered.map((e) => e.label)
  const data = filtered.map((e) => e.count)
  const colors = filtered.map((e) => colorFor(e.label))

  if (existing) {
    existing.data.labels = labels
    existing.data.datasets[0].data = data
    existing.data.datasets[0].backgroundColor = colors
    existing.update()
    return existing
  }

  return new Chart(canvas, {
    type: 'doughnut',
    data: { labels, datasets: [{ data, backgroundColor: colors, borderWidth: 1 }] },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '55%',
      plugins: { legend: { position: 'bottom' } }
    }
  })
}

const load = async () => {
  loading.value = true
  const [alarms, outages] = await Promise.all([getNodesByAlarms(), getNodesByOutages()])
  alarmsEntries.value = alarms
  outagesEntries.value = outages
  loading.value = false
  await nextTick()
  alarmsChart = renderDonut(alarmsCanvas.value, alarms, alarmsChart)
  outagesChart = renderDonut(outagesCanvas.value, outages, outagesChart)
}

onMounted(load)
watch(() => props.refreshTick, load)
onBeforeUnmount(() => {
  alarmsChart?.destroy()
  outagesChart?.destroy()
  alarmsChart = null
  outagesChart = null
})
</script>

<style scoped lang="scss">
.status-overview {
  height: 100%;
  display: flex;
  flex-direction: column;

  &__donuts {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    gap: 0.5rem;
  }

  &__donut {
    position: relative;
    flex: 1 1 0;
    min-width: 0;
    min-height: 0;
  }

  &__center {
    position: absolute;
    top: 42%;
    left: 0;
    right: 0;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    align-items: center;
    pointer-events: none;
  }

  &__total {
    font-size: 1.5rem;
    font-weight: 700;
    line-height: 1;
  }

  &__cap {
    font-size: 0.7rem;
    color: var(--feather-secondary-text-on-surface, #666);
  }

  &__muted {
    text-align: center;
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
