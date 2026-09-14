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
  Reference panel for the dashboard framework. It does no real data fetching;
  it simply renders the cross-cutting contracts (filter, timeframe, refresh)
  it receives, which is enough to verify the framework wiring end-to-end.
  Real panels (Notifications, Availability, Status Overview, NMS-4433 threshold
  alarms, ...) follow this same prop shape — see PanelComponentProps.
-->
<template>
  <div class="sample-panel">
    <p class="sample-panel__lead">Sample dashboard panel — framework wiring check.</p>
    <dl class="sample-panel__facts">
      <dt>Timeframe</dt>
      <dd>{{ timeframe.preset }}</dd>
      <dt>Surveillance categories</dt>
      <dd>{{ categoriesLabel }}</dd>
      <dt>IP match</dt>
      <dd>{{ filter.ipMatch || 'any' }}</dd>
      <dt>Refresh ticks</dt>
      <dd>{{ refreshTick }}</dd>
    </dl>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PanelComponentProps } from '@/types/dashboard'

const props = defineProps<PanelComponentProps>()

const categoriesLabel = computed(() =>
  props.filter.surveillanceCategories.length ? props.filter.surveillanceCategories.join(', ') : 'all'
)
</script>

<style scoped lang="scss">
.sample-panel {
  font-size: 0.875rem;

  &__lead {
    margin: 0 0 0.75rem;
  }

  &__facts {
    display: grid;
    grid-template-columns: auto 1fr;
    gap: 0.25rem 1rem;
    margin: 0;

    dt {
      font-weight: 600;
    }

    dd {
      margin: 0;
    }
  }
}
</style>
