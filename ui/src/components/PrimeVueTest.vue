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
  <div class="primevue-test-page">
    <h2>PrimeVue + FeatherDS Coexistence Test</h2>

    <section>
      <h3>PrimeVue Components</h3>
      <div class="component-row">
        <PButton label="PrimeVue Button" />
        <PButton label="Secondary" severity="secondary" />
        <PButton label="Outlined" outlined />
      </div>

      <div class="component-row">
        <PInputText v-model="inputValue" placeholder="PrimeVue InputText" />
      </div>

      <div class="component-row">
        <PCheckbox v-model="checked" :binary="true" inputId="pv-check" />
        <label for="pv-check">PrimeVue Checkbox</label>
      </div>

      <div class="component-row">
        <PSelect v-model="selectedOption" :options="selectOptions" optionLabel="label" placeholder="PrimeVue Select" />
      </div>

      <div class="component-row">
        <PButton label="Open PrimeVue Dialog" @click="dialogVisible = true" />
        <PDialog v-model:visible="dialogVisible" header="PrimeVue Dialog" :style="{ width: '30rem' }">
          <p>This dialog is rendered by PrimeVue while FeatherDS components coexist on the same page.</p>
        </PDialog>
      </div>
    </section>

    <section>
      <h3>PrimeVue DataTable &amp; Chips (theme token check)</h3>
      <div class="component-row">
        <PChip label="Chip One" />
        <PChip label="Removable" removable />
      </div>
      <PDataTable :value="tableRows">
        <PColumn field="name" header="Name" />
        <PColumn field="type" header="Type" />
        <PColumn field="status" header="Status" />
      </PDataTable>
    </section>

    <section>
      <h3>FeatherDS Components (coexistence check)</h3>
      <div class="component-row">
        <FeatherButton primary>FeatherDS Button</FeatherButton>
        <FeatherButton secondary>Secondary</FeatherButton>
      </div>
    </section>

    <section>
      <h3>Side-by-Side Comparison</h3>
      <div class="side-by-side">
        <div>
          <h4>PrimeVue</h4>
          <PButton label="Click Me" />
          <PInputText v-model="inputValue" placeholder="Type here..." />
        </div>
        <div>
          <h4>FeatherDS</h4>
          <FeatherButton primary>Click Me</FeatherButton>
          <FeatherInput label="Type here..." v-model="featherInputValue" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Checkbox from 'primevue/checkbox'
import Select from 'primevue/select'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Chip from 'primevue/chip'
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'

const PButton = Button
const PInputText = InputText
const PCheckbox = Checkbox
const PSelect = Select
const PDialog = Dialog
const PDataTable = DataTable
const PColumn = Column
const PChip = Chip

const inputValue = ref('')
const featherInputValue = ref('')
const checked = ref(false)
const dialogVisible = ref(false)
const selectedOption = ref(null)
const selectOptions = ref([
  { label: 'Option A', value: 'a' },
  { label: 'Option B', value: 'b' },
  { label: 'Option C', value: 'c' }
])
const tableRows = ref([
  { name: 'router-01', type: 'Cisco', status: 'Up' },
  { name: 'switch-02', type: 'Juniper', status: 'Down' },
  { name: 'server-03', type: 'Linux', status: 'Up' }
])
</script>

<style scoped>
.primevue-test-page {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

section {
  margin-bottom: 2rem;
  padding: 1.5rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
}

.component-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.side-by-side {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.side-by-side > div {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

h3 {
  margin-bottom: 1rem;
}

h4 {
  margin-bottom: 0.5rem;
  color: #666;
}
</style>
