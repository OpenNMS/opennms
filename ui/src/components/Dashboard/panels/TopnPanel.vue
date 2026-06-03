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
  Top-N panel: ranks entities by a chosen KPI over the (resolved) timeframe.
  KPI, N, and sort direction are set via the panel options (gear). Defaults:
  Node Response Time, descending, N=5.
-->
<template>
  <div class="topn">
    <p
      v-if="loading"
      class="topn__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!rows.length"
      class="topn__muted"
    >
      No data for {{ kpiLabel }} in this timeframe.
    </p>
    <ol
      v-else
      class="topn__list"
    >
      <li
        v-for="(row, idx) in rows"
        :key="idx"
        class="topn__row"
      >
        <span class="topn__rank">{{ idx + 1 }}</span>
        <span class="topn__label">{{ row.label }}</span>
        <span class="topn__value">{{ format(row.value) }} {{ row.unit }}</span>
      </li>
    </ol>
    <p
      v-if="!loading"
      class="topn__caption"
    >
      {{ kpiLabel }} · {{ direction === 'desc' ? 'highest' : 'lowest' }} {{ n }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'
import {
  DEFAULT_TOPN_KPI,
  DEFAULT_TOPN_N,
  TOPN_KPIS,
  queryTopn,
  type TopnRow
} from '@/services/topnService'

const props = defineProps<PanelComponentProps>()

const loading = ref(true)
const rows = ref<TopnRow[]>([])

const kpiId = computed(() => String(props.options?.kpi ?? DEFAULT_TOPN_KPI))
const n = computed(() => Number(props.options?.n ?? DEFAULT_TOPN_N))
const direction = computed<'asc' | 'desc'>(() => (props.options?.direction === 'asc' ? 'asc' : 'desc'))
const kpiLabel = computed(() => TOPN_KPIS.find((k) => k.id === kpiId.value)?.label ?? kpiId.value)

const format = (v: number) => (Math.abs(v) >= 100 ? v.toFixed(0) : v.toFixed(2))

const load = async () => {
  loading.value = true
  rows.value = await queryTopn(kpiId.value, props.timeframe, n.value, direction.value)
  loading.value = false
}

onMounted(load)
watch([() => props.refreshTick, kpiId, n, direction, () => props.timeframe.preset], load)
</script>

<style scoped lang="scss">
.topn {
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

  &__rank {
    flex: 0 0 1.25rem;
    text-align: right;
    font-weight: 700;
    color: var(--feather-secondary-text-on-surface, #888);
  }

  &__label {
    flex: 1 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__value {
    flex: 0 0 auto;
    font-variant-numeric: tabular-nums;
  }

  &__caption {
    margin: 0.5rem 0 0;
    font-size: 0.75rem;
    color: var(--feather-secondary-text-on-surface, #888);
  }
}
</style>
