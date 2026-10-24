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
  The "Node and Domain Interface Reports" of the legacy index page: pick a node or a domain to
  see a collection built on the fly from its SNMP interfaces. Nothing is stored until the user
  saves it as a collection from the view page.
-->
<template>
  <TableCard>
    <div class="header">
      <div class="title-block">
        <div class="card-title">Node and Domain Collections</div>
        <div class="subtitle">A collection built on the fly from the SNMP interfaces of a node or a domain. Open one and save it to customize it.</div>
      </div>
    </div>

    <p v-if="loadError" class="error" data-test="ncp-load-error">{{ loadError }}</p>
    <template v-else>
      <OnmsSearchInput
        :modelValue="filter"
        placeholder="Filter by name"
        dataTest="ncp-filter"
        @update:modelValue="(value) => filter = String(value ?? '')"
      />
      <p v-if="loading" class="muted" data-test="ncp-loading">Loading nodes and domains...</p>
      <p v-else-if="!visibleResources.length" class="muted" data-test="ncp-empty">
        {{ resources.length ? 'No node or domain matches the filter.' : 'No node or domain has performance data yet.' }}
      </p>
      <OnmsListbox
        v-else
        :options="visibleResources"
        :modelValue="null"
        dataKey="id"
        optionLabel="label"
        scrollHeight="18rem"
        aria-label="Nodes and domains"
        data-test="ncp-resources"
        @update:modelValue="(value) => open(value as TopLevelResource | null)"
      >
        <template #option="{ option }">
          <span class="option">
            <span>{{ asResource(option).label }}</span>
            <span class="muted">{{ asResource(option).typeLabel }}</span>
          </span>
        </template>
      </OnmsListbox>
    </template>
  </TableCard>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { OnmsListbox, OnmsSearchInput } from '@opennms/onms-ui'
import TableCard from '@/components/Common/TableCard.vue'
import { getResources } from '@/services/resourceService'
import { syntheticCollectionRoute } from './syntheticCollectionRoute'

interface TopLevelResource { id: string, label: string, typeLabel: string, route: string }

const router = useRouter()

const resources = ref<TopLevelResource[]>([])
const loading = ref(true)
const loadError = ref('')
const filter = ref('')

// the listbox option slot is untyped
const asResource = (option: unknown) => option as TopLevelResource

const visibleResources = computed(() => {
  const term = filter.value.trim().toLowerCase()
  return term ? resources.value.filter(r => r.label.toLowerCase().includes(term)) : resources.value
})

const load = async () => {
  loading.value = true
  loadError.value = ''
  const response = await getResources()
  loading.value = false
  if (response === null) {
    loadError.value = 'Failed to load the nodes and domains.'
    return
  }
  // only the top-level resources that have an on-the-fly collection, nodes by label then domains
  resources.value = (response.resource ?? [])
    .map(r => ({ id: r.id, label: r.label, typeLabel: r.typeLabel, route: syntheticCollectionRoute(r.id) }))
    .filter((r): r is TopLevelResource => r.route !== null)
    .sort((a, b) => a.typeLabel.localeCompare(b.typeLabel) || a.label.localeCompare(b.label))
}

const open = (resource: TopLevelResource | null) => {
  if (resource) {
    router.push(resource.route)
  }
}

onMounted(load)
</script>

<style scoped lang="scss">
.header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1rem;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 600;
}

.subtitle,
.muted {
  color: var(--p-text-muted-color);
}

.error {
  color: var(--p-red-500, #d32f2f);
}

.option {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  width: 100%;
}
</style>
