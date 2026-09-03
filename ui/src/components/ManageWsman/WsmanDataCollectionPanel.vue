<template>
  <div class="data-collection" data-test="wsman-data-collection">
    <p class="intro">
      Merged from <code>wsman-datacollection-config.xml</code> and every file in
      <code>wsman-datacollection.d/</code>, in the order the collector reads them. Existing objects can be
      edited or removed and are saved back to the file they live in; rewriting a file drops any XML comments
      it held. Adding new collections, system definitions or groups is not offered here until these files
      move into the database.
    </p>
    <dl class="summary">
      <dt>RRD repository</dt>
      <dd data-test="rrd-repository">{{ dataCollection.rrdRepository || NOT_SET }}</dd>
      <dt>Source files</dt>
      <dd data-test="sources">{{ dataCollection.sources.join(', ') }}</dd>
    </dl>

    <OnmsCard class="section" data-test="collections-card">
      <template #title>
        <div class="card-header">
          <span class="card-title">Collections ({{ dataCollection.collections.length }})</span>
          <!--
            Creating objects from this page is held back until the data collection
            files move into the database in a future effort: today every new object
            would land in an XML file on disk with no way to manage the files
            themselves. Editing and deleting what exists stays available.
          -->
          <!-- <OnmsButton label="Add Collection" data-test="add-collection" @click="emit('add', 'collection')" /> -->
        </div>
      </template>
      <template #content>
        <OnmsTable :value="dataCollection.collections" dataKey="name" data-test="collections-table">
          <template #empty><span>No collections.</span></template>
          <OnmsColumn field="name" header="Name" sortable />
          <OnmsColumn field="source" header="Source" sortable />
          <OnmsColumn header="RRD step">
            <template #body="{ data }">{{ data.rrdStep ?? NOT_SET }}</template>
          </OnmsColumn>
          <OnmsColumn header="RRAs">
            <template #body="{ data }"><span :title="data.rras.join('\n')">{{ data.rras.length }}</span></template>
          </OnmsColumn>
          <OnmsColumn header="System definitions">
            <template #body="{ data }">
              <span v-if="data.includeAllSystemDefinitions">All</span>
              <span v-else>{{ data.includedSystemDefinitions.join(', ') || NOT_SET }}</span>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :title="`Edit ${data.name}`" :aria-label="`Edit ${data.name}`" data-test="edit-collection" @click="emit('edit', 'collection', data)" />
                <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete ${data.name}`" :aria-label="`Delete ${data.name}`" data-test="delete-collection" @click="emit('delete', 'collection', data)" />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>
      </template>
    </OnmsCard>

    <OnmsCard class="section" data-test="system-definitions-card">
      <template #title>
        <div class="card-header">
          <span class="card-title">System Definitions ({{ dataCollection.systemDefinitions.length }})</span>
          <!-- held back with the other Add buttons; see the note on the Collections card -->
          <!-- <OnmsButton label="Add System Definition" data-test="add-system-definition" @click="emit('add', 'systemDefinition')" /> -->
        </div>
      </template>
      <template #content>
        <OnmsTable :value="dataCollection.systemDefinitions" dataKey="name" data-test="system-definitions-table">
          <template #empty><span>No system definitions.</span></template>
          <OnmsColumn field="name" header="Name" sortable />
          <OnmsColumn field="source" header="Source" sortable />
          <OnmsColumn header="Rules">
            <template #body="{ data }">
              <ul class="plain-list"><li v-for="(r, i) in data.rules" :key="i"><code>{{ r }}</code></li></ul>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Groups">
            <template #body="{ data }">{{ data.includedGroups.join(', ') }}</template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :title="`Edit ${data.name}`" :aria-label="`Edit ${data.name}`" data-test="edit-system-definition" @click="emit('edit', 'systemDefinition', data)" />
                <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete ${data.name}`" :aria-label="`Delete ${data.name}`" data-test="delete-system-definition" @click="emit('delete', 'systemDefinition', data)" />
              </div>
            </template>
          </OnmsColumn>
        </OnmsTable>
      </template>
    </OnmsCard>

    <OnmsCard class="section" data-test="groups-card">
      <template #title>
        <div class="card-header">
          <span class="card-title">Groups ({{ filteredGroups.length }}<template v-if="groupFilter"> of {{ dataCollection.groups.length }}</template>)</span>
          <div class="card-tools">
            <OnmsInputText v-model="groupFilter" placeholder="Filter by name, source or resource type" data-test="group-filter" />
            <!-- held back with the other Add buttons; see the note on the Collections card -->
            <!-- <OnmsButton label="Add Group" data-test="add-group" @click="emit('add', 'group')" /> -->
          </div>
        </div>
      </template>
      <template #content>
        <OnmsTable
          v-model:expandedRows="expandedGroups"
          :value="filteredGroups"
          dataKey="name"
          paginator
          :rows="20"
          :rowsPerPageOptions="[20, 50, 100]"
          data-test="groups-table"
        >
          <template #empty><span data-test="no-groups">No groups match.</span></template>
          <OnmsColumn expander style="width: 3rem" />
          <OnmsColumn field="name" header="Name" sortable />
          <OnmsColumn field="source" header="Source" sortable />
          <OnmsColumn field="resourceType" header="Resource type" sortable />
          <OnmsColumn header="Attributes">
            <template #body="{ data }">{{ data.attributes.length }}</template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data }">
              <div class="action-container">
                <OnmsIconButton :icon="Edit" :title="`Edit ${data.name}`" :aria-label="`Edit ${data.name}`" data-test="edit-group" @click="emit('edit', 'group', data)" />
                <OnmsIconButton :icon="Delete" severity="danger" :title="`Delete ${data.name}`" :aria-label="`Delete ${data.name}`" data-test="delete-group" @click="emit('delete', 'group', data)" />
              </div>
            </template>
          </OnmsColumn>
          <template #expansion="{ data }">
            <div class="group-details" :data-test="`group-details-${data.name}`">
              <dl class="summary">
                <dt>Resource URI</dt><dd><code>{{ data.resourceUri }}</code></dd>
                <dt>Dialect</dt><dd>{{ data.dialect || 'default (CQL)' }}</dd>
                <dt>Filter</dt><dd><code>{{ data.filter || NOT_SET }}</code></dd>
              </dl>
              <table class="attributes">
                <thead><tr><th>Name</th><th>Alias</th><th>Type</th><th>Index of</th><th>Filter</th></tr></thead>
                <tbody>
                  <tr v-for="(a, i) in data.attributes" :key="i">
                    <td>{{ a.name }}</td><td>{{ a.alias }}</td><td>{{ a.type ?? NOT_SET }}</td>
                    <td><code>{{ a.indexOf || NOT_SET }}</code></td><td><code>{{ a.filter || NOT_SET }}</code></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
        </OnmsTable>
      </template>
    </OnmsCard>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { OnmsCard, OnmsColumn, OnmsIconButton, OnmsInputText, OnmsTable } from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import { WsmanDataCollection, WsmanGroupInfo } from '@/types/wsmanAdmin'
import { NOT_SET } from './wsmanDisplay'
import { DataCollectionKind, EditableObject } from './wsmanDataCollectionForm'

const props = defineProps<{
  dataCollection: WsmanDataCollection
}>()

const emit = defineEmits<{
  (e: 'add', kind: DataCollectionKind): void
  (e: 'edit', kind: DataCollectionKind, item: EditableObject): void
  (e: 'delete', kind: DataCollectionKind, item: EditableObject): void
}>()

const groupFilter = ref('')
const expandedGroups = ref<WsmanGroupInfo[]>([])

const filteredGroups = computed(() => {
  const term = groupFilter.value.trim().toLowerCase()
  if (!term) {
    return props.dataCollection.groups
  }
  return props.dataCollection.groups.filter(g =>
    [g.name, g.source, g.resourceType].some(v => v?.toLowerCase().includes(term)))
})
</script>

<style lang="scss" scoped>
.data-collection {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.intro {
  margin: 0;
  font-size: 0.9rem;
  color: var(--p-text-muted-color);
}

.summary {
  display: grid;
  grid-template-columns: max-content 1fr;
  column-gap: 1.5rem;
  row-gap: 0.3rem;
  margin: 0;
  font-size: 0.9rem;

  dt {
    font-weight: 600;
  }

  dd {
    margin: 0;
    word-break: break-all;
  }
}

.section {
  padding: 25px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.card-tools {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.action-container {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.plain-list {
  margin: 0;
  padding-left: 1rem;
}

.group-details {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 0.5rem 1rem 0.75rem 3rem;
}

.attributes {
  border-collapse: collapse;
  font-size: 0.9rem;

  th,
  td {
    text-align: left;
    padding: 0.25rem 0.75rem 0.25rem 0;
    border-bottom: 1px solid var(--p-content-border-color, #ddd);
  }
}
</style>
