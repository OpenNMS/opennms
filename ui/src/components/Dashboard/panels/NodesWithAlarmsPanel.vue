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
  Dashboard panel replicating the legacy alarm summary-box: nodes that currently
  have alarms, with their alarm count and highest severity. Alarms are fetched
  and grouped by node client-side.
-->
<template>
  <div class="nodes-alarms">
    <p
      v-if="loading"
      class="nodes-alarms__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!rows.length"
      class="nodes-alarms__muted"
    >
      There are no nodes with pending alarms.
    </p>
    <ul
      v-else
      class="nodes-alarms__list"
    >
      <li
        v-for="row in rows"
        :key="row.nodeId"
        class="nodes-alarms__row"
        :style="shade ? { backgroundColor: severityTint(row.maxSeverity) } : undefined"
      >
        <span
          class="nodes-alarms__sev"
          :style="{ backgroundColor: severityColor(row.maxSeverity) }"
          :title="severityLabel(row.maxSeverity)"
        />
        <a
          class="nodes-alarms__node"
          :href="`/opennms/element/node.jsp?node=${row.nodeId}`"
        >{{ row.nodeLabel }}</a>
        <span class="nodes-alarms__count">{{ row.count }} alarm{{ row.count === 1 ? '' : 's' }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { SORT } from '@/types'
import type { Alarm } from '@/types'
import { getAlarms } from '@/services/alarmService'
import { buildFilterClauses } from '../filter'
import { isActionableSeverity, maxSeverity, severityColor, severityLabel, severityTint } from '../severity'

interface NodeAlarmRow {
  nodeId: number
  nodeLabel: string
  count: number
  maxSeverity: string
}

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const rows = ref<NodeAlarmRow[]>([])
const shade = computed(() => !!props.options?.shade)

const MAX_ROWS = 12

let loadSeq = 0
const load = async () => {
  // seq synchronously at call time (buildFilterClauses awaits) so call order wins
  const seq = ++loadSeq
  loading.value = true
  // Parity with the legacy node-alarm-summary box (AlarmDao.getNodeAlarmSummaries):
  // only UNACKNOWLEDGED alarms of WARNING severity or higher count as "pending".
  // Filter server-side (FIQL) so acknowledged/cleared/normal alarms neither
  // inflate the counts nor consume the fetch budget; order by severity so a cap
  // keeps the most severe. The client guard re-applies the severity floor
  // defensively in case the server comparison is looser than expected.
  const clauses = await buildFilterClauses(props.filter)
  const resp = await getAlarms({
    _s: ['alarmAckTime==\u0000', 'severity=ge=WARNING', ...clauses].join(';'),
    limit: 500,
    orderBy: 'severity',
    order: SORT.DESCENDING
  })
  if (seq !== loadSeq) {
    return
  }
  const alarms: Alarm[] = resp ? resp.alarm : []

  const byNode = new Map<number, NodeAlarmRow>()
  for (const a of alarms) {
    if (a.nodeId == null || !isActionableSeverity(a.severity)) {
      continue
    }
    const existing = byNode.get(a.nodeId)
    if (existing) {
      existing.count += 1
      existing.maxSeverity = maxSeverity([existing.maxSeverity, a.severity])
    } else {
      byNode.set(a.nodeId, {
        nodeId: a.nodeId,
        nodeLabel: a.nodeLabel ?? `Node ${a.nodeId}`,
        count: 1,
        maxSeverity: a.severity
      })
    }
  }

  rows.value = [...byNode.values()].slice(0, MAX_ROWS)
  loading.value = false
}

onMounted(load)
watch([() => props.refreshTick, () => props.filter], load, { deep: true })
</script>

<style scoped lang="scss">
.nodes-alarms {
  font-size: 0.875rem;

  &__muted {
    color: var(--p-text-muted-color, #666);
  }

  &__list {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  &__row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.25rem 0;
    border-bottom: 1px solid var(--p-content-border-color, #e0e0e0);
  }

  &__sev {
    flex: 0 0 auto;
    width: 0.5rem;
    height: 1.1rem;
    border-radius: 2px;
  }

  &__node {
    flex: 1 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__count {
    flex: 0 0 auto;
    color: var(--p-text-muted-color, #666);
  }
}
</style>
