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

<!-- Dashboard panel replicating the legacy "Pending Situations" box. -->
<template>
  <div class="situations">
    <p
      v-if="loading"
      class="situations__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!situations.length"
      class="situations__muted"
    >
      There are no pending situations.
    </p>
    <ul
      v-else
      class="situations__list"
    >
      <li
        v-for="s in situations"
        :key="s.id"
        class="situations__row"
        :style="shade ? { backgroundColor: severityTint(s.severity) } : undefined"
      >
        <span
          class="situations__sev"
          :style="{ backgroundColor: severityColor(s.severity) }"
          :title="severityLabel(s.severity)"
        />
        <a
          class="situations__title"
          :href="`/opennms/alarm/detail.htm?id=${s.id}`"
        >Situation {{ s.id }}</a>
        <span class="situations__meta">{{ describe(s) }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import { getPendingSituations, type Situation } from '@/services/situationService'
import { buildFilterClauses } from '../filter'
import { severityColor, severityLabel, severityTint } from '../severity'

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const situations = ref<Situation[]>([])
const shade = computed(() => !!props.options?.shade)

const describe = (s: Situation): string => {
  const parts: string[] = []
  if (s.affectedNodeCount != null) {
    parts.push(`${s.affectedNodeCount} node${s.affectedNodeCount === 1 ? '' : 's'}`)
  }
  if (s.situationAlarmCount != null) {
    parts.push(`${s.situationAlarmCount} alarm${s.situationAlarmCount === 1 ? '' : 's'}`)
  }
  return parts.join(', ')
}

let loadSeq = 0
const load = async () => {
  // seq synchronously at call time (buildFilterClauses awaits) so call order wins
  const seq = ++loadSeq
  loading.value = true
  const clauses = await buildFilterClauses(props.filter)
  const result = await getPendingSituations(12, clauses)
  if (seq !== loadSeq) {
    return
  }
  situations.value = result
  loading.value = false
}

onMounted(load)
watch([() => props.refreshTick, () => props.filter], load, { deep: true })
</script>

<style scoped lang="scss">
.situations {
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

  &__title {
    flex: 0 0 auto;
  }

  &__meta {
    flex: 1 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    color: var(--p-text-muted-color, #666);
  }
}
</style>
