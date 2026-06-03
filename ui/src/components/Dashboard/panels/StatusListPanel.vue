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
  Shared renderer for "<things> with Pending Alarms" list panels (business
  services, applications): a list of name + severity badge, with an empty state.
  Parameterized by a loader returning StatusListItem[].
-->
<template>
  <div class="status-list">
    <p
      v-if="loading"
      class="status-list__muted"
    >
      Loading…
    </p>
    <p
      v-else-if="!items.length"
      class="status-list__muted"
    >
      {{ emptyText }}
    </p>
    <ul
      v-else
      class="status-list__list"
    >
      <li
        v-for="item in items"
        :key="item.id"
        class="status-list__row"
      >
        <span
          class="status-list__sev"
          :style="{ backgroundColor: severityColor(item.severity) }"
          :title="severityLabel(item.severity)"
        />
        <span class="status-list__name">{{ item.name }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { type StatusListItem } from '@/services/statusService'
import { severityColor, severityLabel } from '../severity'

const props = defineProps<{
  loader: () => Promise<StatusListItem[]>
  emptyText: string
  refreshTick: number
}>()

const loading = ref(true)
const items = ref<StatusListItem[]>([])

const load = async () => {
  loading.value = true
  items.value = await props.loader()
  loading.value = false
}

onMounted(load)
watch(() => props.refreshTick, load)
</script>

<style scoped lang="scss">
.status-list {
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

  &__name {
    flex: 1 1 auto;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
