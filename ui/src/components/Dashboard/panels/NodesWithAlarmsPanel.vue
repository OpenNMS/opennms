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
import { onMounted, ref, watch } from 'vue'
import { SORT } from '@featherds/table'
import type { PanelComponentProps } from '@/types/dashboard'
import type { Alarm } from '@/types'
import { getAlarms } from '@/services/alarmService'
import { maxSeverity, severityColor, severityLabel } from '../severity'

interface NodeAlarmRow {
  nodeId: number
  nodeLabel: string
  count: number
  maxSeverity: string
}

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const rows = ref<NodeAlarmRow[]>([])

const MAX_ROWS = 12

const load = async () => {
  loading.value = true
  const resp = await getAlarms({ limit: 250, orderBy: 'lastEventTime', order: SORT.DESCENDING })
  const alarms: Alarm[] = resp ? resp.alarm : []

  const byNode = new Map<number, NodeAlarmRow>()
  for (const a of alarms) {
    if (a.nodeId == null) continue
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
watch(() => props.refreshTick, load)
</script>

<style scoped lang="scss">
.nodes-alarms {
  font-size: 0.875rem;

  &__muted {
    color: var(--feather-secondary-text-on-surface, #666);
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
    border-bottom: 1px solid var(--feather-border-on-surface, #e0e0e0);
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
    color: var(--feather-secondary-text-on-surface, #666);
  }
}
</style>
