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
  Global dashboard filter (NMS-10507): surveillance categories + IP match applied
  to every panel that declares support for them. Per-panel overrides come with the
  options dialog in milestone 5; this is the dashboard-wide control.
-->
<template>
  <OnmsButton
    variant="text"
    icon="pi pi-filter"
    :label="filterLabel"
    @click="toggle"
  />
  <OnmsPopover ref="op">
    <div class="dashboard-filter">
      <p class="dashboard-filter__hint">
        Applies to the node-scoped panels (Situations, Nodes with Alarms, Service
        Outages, Availability). Panels without node data are unaffected.
      </p>
      <label class="dashboard-filter__label">Surveillance categories</label>
      <OnmsMultiSelect
        v-model="categories"
        :options="categoryOptions"
        option-label="label"
        option-value="value"
        placeholder="All categories"
        filter
        display="chip"
        class="dashboard-filter__field"
      />

      <label class="dashboard-filter__label">IP address match</label>
      <OnmsInputText
        v-model="ipMatch"
        placeholder="e.g. 10.1.*.*"
        class="dashboard-filter__field"
      />

      <div class="dashboard-filter__actions">
        <OnmsButton
          variant="text"
          label="Clear"
          @click="clear"
        />
        <OnmsButton
          label="Apply"
          @click="apply"
        />
      </div>
    </div>
  </OnmsPopover>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { OnmsButton, OnmsPopover, OnmsMultiSelect, OnmsInputText } from '@opennms/onms-ui'
import { getCategories } from '@/services/categoryService'
import { useDashboardStore } from '@/stores/dashboardStore'

const store = useDashboardStore()

const op = ref()
const categories = ref<string[]>([...store.layout.globalFilter.surveillanceCategories])
const ipMatch = ref<string>(store.layout.globalFilter.ipMatch ?? '')
const categoryOptions = ref<{ label: string; value: string }[]>([])

const activeCount = computed(
  () =>
    store.layout.globalFilter.surveillanceCategories.length + (store.layout.globalFilter.ipMatch ? 1 : 0)
)
const filterLabel = computed(() => (activeCount.value ? `Filter (${activeCount.value})` : 'Filter'))

const toggle = (event: Event) => op.value?.toggle(event)

onMounted(async () => {
  const resp = await getCategories()
  if (resp) {
    categoryOptions.value = resp.category.map(c => ({ label: c.name, value: c.name }))
  }
})

const apply = (event: Event) => {
  store.setGlobalFilter({
    surveillanceCategories: [...categories.value],
    ipMatch: ipMatch.value.trim() ? ipMatch.value.trim() : null
  })
  op.value?.toggle(event)
}

const clear = () => {
  categories.value = []
  ipMatch.value = ''
  store.setGlobalFilter({ surveillanceCategories: [], ipMatch: null })
}
</script>

<style scoped lang="scss">
.dashboard-filter {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  min-width: 18rem;
  padding: 0.5rem;

  &__label {
    font-size: 0.8125rem;
    font-weight: 600;
  }

  &__field {
    width: 100%;
  }

  &__actions {
    display: flex;
    justify-content: flex-end;
    gap: 0.5rem;
    margin-top: 0.25rem;
  }
}
</style>
