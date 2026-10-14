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
<template>
  <div class="topology-appearance" role="group" aria-label="Appearance">
    <div class="topology-appearance__row">
      <span class="topology-appearance__label" aria-hidden="true">Node size</span>
      <OnmsSlider
        v-model="nodeSize"
        :min="store.NODE_SIZE_MIN"
        :max="store.NODE_SIZE_MAX"
        class="topology-appearance__slider"
        aria-label="Node size"
      />
      <span class="topology-appearance__value">{{ store.nodeSize }}</span>
    </div>
    <div class="topology-appearance__row">
      <span class="topology-appearance__label" aria-hidden="true">Link width</span>
      <OnmsSlider
        v-model="linkWidth"
        :min="store.LINK_WIDTH_MIN"
        :max="store.LINK_WIDTH_MAX"
        class="topology-appearance__slider"
        aria-label="Link width"
      />
      <span class="topology-appearance__value">{{ store.linkWidth }}</span>
    </div>
    <div class="topology-appearance__row">
      <span class="topology-appearance__label" aria-hidden="true">Labels</span>
      <OnmsSelectButton
        v-model="labelPlacement"
        :options="placements"
        option-label="label"
        option-value="value"
        :allow-empty="false"
        aria-label="Node label placement"
        class="topology-appearance__placement"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OnmsSelectButton, OnmsSlider } from '@opennms/onms-ui'
import { useTopologyStore } from '@/stores/topologyStore'
import type { LabelPlacement } from '@/types/topology'

// How the map reads: sizes that apply to every node and link. They save with
// a custom view through the store; a discovered graph keeps them for the session.
const store = useTopologyStore()

const nodeSize = computed<number>({
  get: () => store.nodeSize,
  set: n => store.setNodeSize(n)
})

const linkWidth = computed<number>({
  get: () => store.linkWidth,
  set: n => store.setLinkWidth(n)
})

const placements: { label: string; value: LabelPlacement }[] = [
  { label: 'Right', value: 'right' },
  { label: 'Below', value: 'bottom' },
  { label: 'Above', value: 'top' }
]

const labelPlacement = computed<LabelPlacement>({
  get: () => store.labelPlacement,
  set: p => store.setLabelPlacement(p)
})
</script>

<style scoped>
.topology-appearance {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  min-width: 16rem;
  padding: 0.25rem;
}

.topology-appearance__row {
  display: grid;
  grid-template-columns: 5.5rem 1fr 2rem;
  align-items: center;
  gap: 0.75rem;
}

.topology-appearance__placement {
  grid-column: 2 / span 2;
}

.topology-appearance__label {
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}

.topology-appearance__value {
  text-align: right;
  font-variant-numeric: tabular-nums;
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.875rem;
}
</style>
