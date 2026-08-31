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

<!-- Dashboard panel replicating the legacy "Nodes with Service Outages" box. -->
<template>
  <div class="outages">
    <p
      v-if="loading"
      class="outages__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="failed"
      class="outages__muted"
    >
      Unable to load outages.
    </p>
    <p
      v-else-if="!outages.length"
      class="outages__muted"
    >
      There are no current outages.
    </p>
    <ul
      v-else
      class="outages__list"
    >
      <li
        v-for="(o, idx) in outages"
        :key="o.id ?? idx"
        class="outages__row"
      >
        <span class="outages__dot" />
        <a
          class="outages__node"
          :href="`/opennms/element/node.jsp?node=${o.nodeId}`"
        >{{ o.nodeLabel ?? `Node ${o.nodeId}` }}</a>
        <span class="outages__svc">{{ serviceName(o) }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getCurrentOutages, outageServiceName, type CurrentOutage } from '@/services/outageService'
import { buildFilterClauses } from '../filter'

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const failed = ref(false)
const outages = ref<CurrentOutage[]>([])

const serviceName = outageServiceName

let loadSeq = 0
const load = async () => {
  // take the sequence synchronously at call time so it reflects call order even
  // though buildFilterClauses awaits (variable latency) — otherwise a slower older
  // filter can resolve last and overwrite a newer one
  const seq = ++loadSeq
  loading.value = true
  const clauses = await buildFilterClauses(props.filter)
  const result = await getCurrentOutages(12, clauses)
  if (seq !== loadSeq) {
    return
  }
  // null = fetch failure; keep it distinct from an all-clear empty list
  failed.value = result === null
  outages.value = result ?? []
  loading.value = false
}

onMounted(load)
watch([() => props.refreshTick, () => props.filter], load, { deep: true })
</script>

<style scoped lang="scss">
.outages {
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

  &__dot {
    flex: 0 0 auto;
    width: 0.5rem;
    height: 1.1rem;
    border-radius: 2px;
    background-color: #cc0000;
  }

  &__node {
    flex: 1 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__svc {
    flex: 0 0 auto;
    color: var(--p-text-muted-color, #666);
  }
}
</style>
