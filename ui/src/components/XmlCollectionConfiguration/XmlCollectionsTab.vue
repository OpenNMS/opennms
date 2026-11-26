<template>
  <div class="xml-collections-tab">
    <p class="intro">
      An XML data collection fetches documents from each node and stores the values its groups select. A collectd
      service uses it through its <code>collection</code> parameter.
      <OnmsIconButton
        :icon="InfoIcon"
        iconSize="1.2em"
        class="info-icon-button"
        title="About XML data collections"
        aria-label="About XML data collections"
        data-test="xml-collections-info-icon"
        @click="isHelpVisible = true"
      />
    </p>

    <TableCard class="xml-table-card">
      <div class="table-header">
        <OnmsSearchInput
          class="search"
          :modelValue="search"
          placeholder="Search collections"
          ariaLabel="Search XML data collections"
          dataTest="xml-collection-search"
          @update:modelValue="search = $event ?? ''"
        />
        <OnmsButton class="create-button" data-test="xml-collection-create" @click="router.push(COLLECTION_CREATE_PATH)">
          New collection
        </OnmsButton>
      </div>

      <OnmsTable :value="filtered" data-test="xml-collections-table">
        <OnmsColumn header="Name">
          <template #body="{ data }">
            <router-link :to="collectionPath(data.name)" data-test="xml-collection-link">{{ data.name }}</router-link>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Step (s)" field="step" />
        <OnmsColumn header="Sources" field="sourceCount" />
        <OnmsColumn header="Groups" field="groupCount" />
        <OnmsColumn header="Imported group files">
          <template #body="{ data }">
            <span data-test="xml-collection-imports">{{ data.importGroups.length ? data.importGroups.join(', ') : '--' }}</span>
          </template>
        </OnmsColumn>
        <OnmsColumn header="Actions">
          <template #body="{ data }">
            <OnmsIconButton
              :icon="DeleteIcon"
              severity="danger"
              tooltip="Delete collection"
              aria-label="Delete collection"
              data-test="xml-collection-delete"
              @click="pendingDelete = data.name"
            />
          </template>
        </OnmsColumn>
      </OnmsTable>

      <EmptyList
        v-if="!filtered.length"
        :content="{ msg: search ? 'No XML data collections match your search.' : 'No XML data collections configured yet.' }"
        data-test="xml-collections-empty"
      />
    </TableCard>

    <OnmsConfirmationDialog
      v-if="pendingDelete"
      :visible="true"
      title="Delete this XML data collection?"
      actionButtonText="Delete"
      data-test="xml-collection-delete-confirm"
      @ok="onConfirmDelete"
      @cancel="pendingDelete = ''"
    >
      <template #content>
        <p>
          '{{ pendingDelete }}' and all of its sources will be removed. Collectd services that use it stop
          collecting data.
        </p>
      </template>
    </OnmsConfirmationDialog>

    <ThresholdHelpDialog
      :visible="isHelpVisible"
      title="XML data collections"
      :text="COLLECTIONS_HELP"
      @close="isHelpVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import ThresholdHelpDialog from '@/components/ThresholdConfiguration/Common/ThresholdHelpDialog.vue'
import useActionFeedback from '@/composables/useActionFeedback'
import { COLLECTIONS_HELP } from '@/lib/xmlCollectionHelpText'
import { COLLECTION_CREATE_PATH, collectionPath } from '@/lib/xmlCollectionRoutes'
import { useXmlCollectionConfigStore } from '@/stores/xmlCollectionConfigStore'
import {
  OnmsButton,
  OnmsColumn,
  OnmsConfirmationDialog,
  OnmsIconButton,
  OnmsSearchInput,
  OnmsTable
} from '@opennms/onms-ui'
import DeleteIcon from '@opennms/onms-ui/icons/action/Delete.vue'
import InfoIcon from '@opennms/onms-ui/icons/action/Info.vue'

const store = useXmlCollectionConfigStore()
const router = useRouter()
const { report } = useActionFeedback()

const search = ref('')
const pendingDelete = ref('')
const isHelpVisible = ref(false)

const filtered = computed(() => {
  const term = search.value.trim().toLowerCase()
  return term ? store.collections.filter(collection => collection.name.toLowerCase().includes(term)) : store.collections
})

const onConfirmDelete = async () => {
  const name = pendingDelete.value
  pendingDelete.value = ''

  report(await store.deleteCollection(name), 'XML data collection deleted.')
}
</script>

<style lang="scss" scoped>
@use '@/components/ThresholdConfiguration/Common/info-icon-button' as *;
@use '@/components/XmlCollectionConfiguration/xml-table' as *;

.info-icon-button {
  @include info-icon-button;
}

.xml-collections-tab {
  @include xml-tab;
}
</style>
