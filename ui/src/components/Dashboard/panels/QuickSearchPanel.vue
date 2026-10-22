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
  Dashboard panel replicating the legacy Quick Search box. element/nodeList.htm
  is gone; searches route to the Vue nodes page with the query keys it tracks
  (nodename / iplike / monitoredService, plus listInterfaces), and a node id
  goes through that page's nodeId redirect to the node detail page.
-->
<template>
  <div class="quick-search">
    <form
      v-for="field in fields"
      :key="field.name"
      class="quick-search__row"
      :data-test="`quick-search-${field.name}`"
      @submit.prevent="submitField(field)"
    >
      <label class="quick-search__label">{{ field.label }}</label>
      <div class="quick-search__input">
        <input
          v-model="values[field.name]"
          class="quick-search__text"
          :name="field.name"
          :placeholder="field.placeholder"
        >
        <button
          class="quick-search__btn"
          type="submit"
          :title="`Search by ${field.label}`"
        >
          <i class="pi pi-search" />
        </button>
      </div>
    </form>

    <form
      v-if="services.length"
      class="quick-search__row"
      data-test="quick-search-service"
      @submit.prevent="submitService"
    >
      <label class="quick-search__label">Providing service</label>
      <div class="quick-search__input">
        <select
          v-model="serviceName"
          class="quick-search__text"
          name="monitoredService"
        >
          <option
            v-for="svc in services"
            :key="svc.id"
            :value="svc.name"
          >{{ svc.name }}</option>
        </select>
        <button
          class="quick-search__btn"
          type="submit"
          title="Search by providing service"
        >
          <i class="pi pi-search" />
        </button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { PanelComponentProps } from '@/types/dashboard'
import { getServiceTypes, type ServiceType } from '@/services/dashboardService'

defineProps<PanelComponentProps>()

const router = useRouter()

interface QuickSearchField {
  label: string
  name: 'nodeId' | 'nodename' | 'iplike'
  placeholder: string
  listInterfaces: 'true' | 'false'
}

const fields: QuickSearchField[] = [
  { label: 'Node ID', name: 'nodeId', placeholder: 'Node ID', listInterfaces: 'false' },
  { label: 'Node label', name: 'nodename', placeholder: 'localhost', listInterfaces: 'true' },
  { label: 'TCP/IP Address', name: 'iplike', placeholder: '*.*.*.* or *:*:*:*:*:*:*:*', listInterfaces: 'false' }
]

const values = reactive<Record<QuickSearchField['name'], string>>({ nodeId: '', nodename: '', iplike: '' })
const services = ref<ServiceType[]>([])
const serviceName = ref('')

onMounted(async () => {
  services.value = await getServiceTypes()
  serviceName.value = services.value[0]?.name ?? ''
})

const submitField = (field: QuickSearchField) => {
  const value = values[field.name].trim()
  if (!value) {
    return
  }
  router.push({ path: '/nodes', query: { [field.name]: value, listInterfaces: field.listInterfaces }})
}

// the nodes page resolves monitoredService by name; a numeric service id is
// not a query it understands
const submitService = () => {
  if (!serviceName.value) {
    return
  }
  router.push({ path: '/nodes', query: { monitoredService: serviceName.value, listInterfaces: 'false' }})
}
</script>

<style scoped lang="scss">
.quick-search {
  font-size: 0.875rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;

  &__label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.15rem;
  }

  &__input {
    display: flex;
    gap: 0.25rem;
  }

  &__text {
    flex: 1 1 auto;
    min-width: 0;
    padding: 0.3rem 0.5rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
  }

  &__btn {
    flex: 0 0 auto;
    padding: 0.3rem 0.6rem;
    border: 1px solid var(--p-content-border-color, #ccc);
    border-radius: 4px;
    background: var(--p-content-background, #fff);
    cursor: pointer;
  }
}
</style>
