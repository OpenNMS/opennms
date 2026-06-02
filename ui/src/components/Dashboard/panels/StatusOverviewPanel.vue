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
  Dashboard panel replicating the legacy Status Overview "Alarms" doughnut, on
  Chart.js. Counts are read per-severity from the v2 alarms endpoint's
  totalCount (cheap; one filtered HEAD-like query per severity).
-->
<template>
  <div class="status-overview">
    <div class="status-overview__chart">
      <canvas ref="canvasRef" />
      <div
        v-if="!loading"
        class="status-overview__center"
      >
        <span class="status-overview__total">{{ total }}</span>
        <span class="status-overview__label">Alarms</span>
      </div>
    </div>
    <p
      v-if="loading"
      class="status-overview__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="total === 0"
      class="status-overview__muted"
    >
      No alarms.
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import Chart from 'chart.js/auto'
import type { PanelComponentProps } from '@/types/dashboard'
import { getAlarms } from '@/services/alarmService'
import { ALARM_CHART_SEVERITIES, severityColor, severityLabel } from '../severity'

const props = defineProps<PanelComponentProps>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const loading = ref(true)
const counts = ref<Record<string, number>>({})
let chart: Chart<'doughnut', number[], string> | null = null

const total = computed(() => Object.values(counts.value).reduce((a, b) => a + b, 0))

const severityCount = async (severity: string): Promise<number> => {
  const resp = await getAlarms({ _s: `alarm.severity==${severity}`, limit: 1 })
  return resp ? resp.totalCount : 0
}

const load = async () => {
  loading.value = true
  const results = await Promise.all(ALARM_CHART_SEVERITIES.map((s) => severityCount(s).then((c) => [s, c] as const)))
  counts.value = Object.fromEntries(results.filter(([, c]) => c > 0))
  loading.value = false
  renderChart()
}

const renderChart = () => {
  if (!canvasRef.value) return
  const labels = Object.keys(counts.value)
  const data = labels.map((s) => counts.value[s])
  const colors = labels.map((s) => severityColor(s))

  if (chart) {
    chart.data.labels = labels.map(severityLabel)
    chart.data.datasets[0].data = data
    chart.data.datasets[0].backgroundColor = colors
    chart.update()
    return
  }

  chart = new Chart(canvasRef.value, {
    type: 'doughnut',
    data: {
      labels: labels.map(severityLabel),
      datasets: [{ data, backgroundColor: colors, borderWidth: 1 }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '68%',
      plugins: {
        legend: { position: 'bottom' }
      }
    }
  })
}

onMounted(load)
watch(() => props.refreshTick, load)
onBeforeUnmount(() => {
  chart?.destroy()
  chart = null
})
</script>

<style scoped lang="scss">
.status-overview {
  height: 100%;
  display: flex;
  flex-direction: column;

  &__chart {
    position: relative;
    flex: 1 1 auto;
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
    font-size: 1.75rem;
    font-weight: 700;
    line-height: 1;
  }

  &__label {
    font-size: 0.75rem;
    color: var(--feather-secondary-text-on-surface, #666);
  }

  &__muted {
    text-align: center;
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
