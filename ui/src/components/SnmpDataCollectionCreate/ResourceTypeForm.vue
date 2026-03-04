<template>
  <TableCard
    class="resource-type-form-card"
    v-if="store.resourceTypeDrawerState.isEditMode !== CreateEditMode.None"
  >
    <div class="header">
      <div class="title-container">
        <h3 class="title">
          {{ store.resourceTypeDrawerState.isEditMode === CreateEditMode.Edit ? 'Update Resource Type'
            : 'Create Resource Type' }}
        </h3>
      </div>
    </div>
    <div class="content">
      <div class="spacer"></div>
      <div>
        <FeatherInput
          label="Name"
          data-test="resource-type-name-input"
          v-model.trim="name"
          :error="errors.name"
        />
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div>
        <FeatherInput
          label="Label"
          data-test="resource-type-label-input"
          v-model.trim="label"
          :error="errors.label"
        />
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div>
        <FeatherInput
          label="Resource Label"
          data-test="resource-type-resource-label-input"
          v-model.trim="resourceLabel"
          :error="errors.resourceLabel"
        />
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div>
        <FeatherRadioGroup
          :label="'Status'"
          v-model="status"
          data-test="system-def-status-input"
          :error="errors.status"
        >
          <FeatherRadio
            v-for="item in STATUS_OPTIONS"
            :value="item.value"
            :key="item.name"
          >
            {{ item.name }}
          </FeatherRadio>
        </FeatherRadioGroup>
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div>
        <FeatherAutocomplete
          class="my-autocomplete"
          label="Storage Strategy"
          type="single"
          text-prop="_text"
          v-model="storageStrategy"
          :loading="storageStrategyLoading"
          :results="storageStrategyResults"
          @search="onSearchStorageStrategy"
          :error="errors.storageStrategy"
        ></FeatherAutocomplete>
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div class="storage-strategy-table-container">
        <div class="header">
          <div class="title">
            <h3>Storage Strategy Parameters</h3>
          </div>
          <div class="action">
            <FeatherButton
              primary
              data-test="add-storage-strategy-button"
              @click="openStorageStrategyDrawer(CreateEditMode.Create)"
            >
              Add Storage Strategy Parameter
            </FeatherButton>
          </div>
        </div>
        <table
          class="storage-strategy-data-table"
          aria-label="Storage Strategy Data Table"
        >
          <thead>
            <tr>
              <th>Key</th>
              <th>Value</th>
              <th>Action</th>
            </tr>
          </thead>
          <TransitionGroup
            name="data-table"
            tag="tbody"
          >
            <tr
              v-for="(param, index) in storageStrategyParams"
              :key="index"
            >
              <td>{{ param.key }}</td>
              <td>{{ param.value }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit Storage Strategy Parameter"
                    data-test="edit-storage-strategy-button"
                    @click="openStorageStrategyDrawer(CreateEditMode.Edit, index, param)"
                  >
                    <FeatherIcon :icon="Edit"> </FeatherIcon>
                  </FeatherButton>
                  <FeatherButton
                    icon="Delete Storage Strategy Parameter"
                    data-test="delete-storage-strategy-button"
                    @click="deleteStorageStrategy(index)"
                  >
                    <FeatherIcon :icon="Delete"> </FeatherIcon>
                  </FeatherButton>
                </div>
              </td>
            </tr>
          </TransitionGroup>
        </table>
        <div v-if="!storageStrategyParams.length">
          <EmptyList :content="{ msg: 'No Storage Strategy parameters added yet.' }" />
        </div>
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div>
        <FeatherAutocomplete
          class="my-autocomplete"
          label="Persistence Selector Strategy"
          type="single"
          text-prop="_text"
          v-model="persistenceSelectorStrategy"
          :loading="persistenceSelectorStrategyLoading"
          :results="persistenceSelectorStrategyResults"
          @search="onSearchPersistenceSelectorStrategy"
          :error="errors.persistenceSelectorStrategy"
        >
        </FeatherAutocomplete>
      </div>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <div class="persistence-selector-strategy-table-container">
        <div class="header">
          <div class="title">
            <h3>Persistence Selector Strategy Parameters</h3>
          </div>
          <div class="action">
            <FeatherButton
              primary
              data-test="add-persistence-selector-strategy-button"
              @click="openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)"
            >
              Add Persistence Selector Strategy Parameter
            </FeatherButton>
          </div>
        </div>
        <table
          class="persistence-selector-strategy-data-table"
          aria-label="Persistence Selector Strategy Data Table"
        >
          <thead>
            <tr>
              <th>Key</th>
              <th>Value</th>
              <th>Action</th>
            </tr>
          </thead>
          <TransitionGroup
            name="data-table"
            tag="tbody"
          >
            <tr
              v-for="(param, index) in persistenceSelectorStrategyParams"
              :key="index"
            >
              <td>{{ param.key }}</td>
              <td>{{ param.value }}</td>
              <td>
                <div class="action-container">
                  <FeatherButton
                    icon="Edit Persistence Selector Strategy Parameter"
                    data-test="edit-persistence-selector-strategy-button"
                    @click="openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, index, param)"
                  >
                    <FeatherIcon :icon="Edit"> </FeatherIcon>
                  </FeatherButton>
                  <FeatherButton
                    icon="Delete Persistence Selector Strategy Parameter"
                    data-test="delete-persistence-selector-strategy-button"
                    @click="deletePersistenceSelectorStrategy(index)"
                  >
                    <FeatherIcon :icon="Delete"> </FeatherIcon>
                  </FeatherButton>
                </div>
              </td>
            </tr>
          </TransitionGroup>
        </table>
        <div v-if="!persistenceSelectorStrategyParams.length">
          <EmptyList :content="{ msg: 'No Persistence Selector Strategy parameters added yet.' }" />
        </div>
      </div>
    </div>
    <div class="footer">
      <FeatherButton
        data-test="cancel-resource-type"
        @click="handleCancel"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="save-resource-type"
        @click="saveResourceType"
        :disabled="isSaveDisabled"
      >
        Save
      </FeatherButton>
    </div>
    <ResourceTypeParameterDrawer
      :state="resourceTypeDrawerState"
      @cancel="closeStrategyDrawer"
      @save="saveParameters"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import { PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STATUS_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionResourceTypePayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, ResourceTypeErrors, SnmpCollectionResourceTypePayload, StorageStrategyForm } from '@/types/snmpDataCollection'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import ResourceTypeParameterDrawer from './Drawer/ResourceTypeParameterDrawer.vue'

const store = useSnmpDataCollectionCreationStore()
const name = ref('')
const resourceLabel = ref('')
const label = ref('')
const status = ref(true)
const storageStrategy = ref(undefined as unknown as IAutocompleteItemType)
const storageStrategyParams = ref<StorageStrategyForm[]>([])
const persistenceSelectorStrategy = ref(undefined as unknown as IAutocompleteItemType)
const persistenceSelectorStrategyParams = ref<PersistSelectorStrategyForm[]>([])
const persistenceSelectorStrategyLoading = ref(false)
const persistenceSelectorStrategyTimeout = ref(-1)
const persistenceSelectorStrategyResults = ref([] as IAutocompleteItemType[])
const storageStrategyLoading = ref(false)
const storageStrategyTimeout = ref(-1)
const storageStrategyResults = ref([] as IAutocompleteItemType[])
const errors = ref<ResourceTypeErrors>({})
const isSaveDisabled = ref(true)
const resourceTypeDrawerState = ref<{
  type: 'storageStrategy' | 'persistenceSelectorStrategy' | null
  visible: boolean
  isEditMode: CreateEditMode
  persistenceSelectorStrategyIndex: number
  storageStrategyIndex: number
  persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null
  storageStrategyObject: StorageStrategyForm | null
}>({
  type: null,
  visible: false,
  isEditMode: CreateEditMode.Create,
  persistenceSelectorStrategyIndex: -1,
  storageStrategyIndex: -1,
  persistenceSelectorStrategyObject: null,
  storageStrategyObject: null
})

const onSearchStorageStrategy = async (q: string) => {
  storageStrategyLoading.value = true
  if (storageStrategyTimeout.value !== -1) {
    clearTimeout(storageStrategyTimeout.value)
  }
  storageStrategyTimeout.value = window.setTimeout(() => {
    const filteredOptions = STORAGE_STRATEGY_OPTIONS
      .filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map((x) => ({
        _text: x,
        _value: x
      }))

    // If no matches found and query is not empty, add custom option
    if (filteredOptions.length === 0 && q.trim()) {
      filteredOptions.push({
        _text: q,
        _value: q
      })
    }

    storageStrategyResults.value = filteredOptions
    storageStrategyLoading.value = false
  }, 500)
}

const onSearchPersistenceSelectorStrategy = async (q: string) => {
  persistenceSelectorStrategyLoading.value = true
  if (persistenceSelectorStrategyTimeout.value !== -1) {
    clearTimeout(persistenceSelectorStrategyTimeout.value)
  }
  persistenceSelectorStrategyTimeout.value = window.setTimeout(() => {
    const filteredOptions = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS
      .filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map((x) => ({
        _text: x,
        _value: x
      }))

    // If no matches found and query is not empty, add custom option
    if (filteredOptions.length === 0 && q.trim()) {
      filteredOptions.push({
        _text: q,
        _value: q
      })
    }

    persistenceSelectorStrategyResults.value = filteredOptions
    persistenceSelectorStrategyLoading.value = false
  }, 500)
}

const deleteStorageStrategy = (index: number) => {
  storageStrategyParams.value.splice(index, 1)
}

const deletePersistenceSelectorStrategy = (index: number) => {
  persistenceSelectorStrategyParams.value.splice(index, 1)
}

const openStorageStrategyDrawer = (
  isEditMode: CreateEditMode,
  storageStrategyIndex = -1,
  storageStrategyObject: StorageStrategyForm | null = null
) => {
  resourceTypeDrawerState.value.visible = true
  resourceTypeDrawerState.value.type = 'storageStrategy'
  resourceTypeDrawerState.value.isEditMode = isEditMode
  resourceTypeDrawerState.value.storageStrategyIndex = storageStrategyIndex
  resourceTypeDrawerState.value.storageStrategyObject = storageStrategyObject
}

const openPersistenceSelectorStrategyDrawer = (
  isEditMode: CreateEditMode,
  persistenceSelectorStrategyIndex = -1,
  persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null = null
) => {
  resourceTypeDrawerState.value.visible = true
  resourceTypeDrawerState.value.type = 'persistenceSelectorStrategy'
  resourceTypeDrawerState.value.isEditMode = isEditMode
  resourceTypeDrawerState.value.persistenceSelectorStrategyIndex =
    persistenceSelectorStrategyIndex
  resourceTypeDrawerState.value.persistenceSelectorStrategyObject = persistenceSelectorStrategyObject
}

const saveParameters = (type: 'storageStrategy' | 'persistenceSelectorStrategy', key: string, value: string) => {
  if (type === 'storageStrategy') {
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Edit && resourceTypeDrawerState.value.storageStrategyIndex > -1) {
      storageStrategyParams.value[resourceTypeDrawerState.value.storageStrategyIndex] = { key, value }
    }
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Create) {
      storageStrategyParams.value.push({ key, value })
    }
  }
  if (type === 'persistenceSelectorStrategy') {
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Edit && resourceTypeDrawerState.value.persistenceSelectorStrategyIndex > -1) {
      persistenceSelectorStrategyParams.value[resourceTypeDrawerState.value.persistenceSelectorStrategyIndex] = { key, value }
    }
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Create) {
      persistenceSelectorStrategyParams.value.push({ key, value })
    }
  }
  closeStrategyDrawer()
}

const closeStrategyDrawer = () => {
  resourceTypeDrawerState.value = {
    type: null,
    visible: false,
    isEditMode: CreateEditMode.None,
    persistenceSelectorStrategyIndex: -1,
    storageStrategyIndex: -1,
    persistenceSelectorStrategyObject: null,
    storageStrategyObject: null
  }
}

const handleCancel = () => {
  name.value = ''
  label.value = ''
  resourceLabel.value = ''
  status.value = true
  storageStrategy.value = undefined as unknown as IAutocompleteItemType
  storageStrategyParams.value = []
  persistenceSelectorStrategy.value = undefined as unknown as IAutocompleteItemType
  persistenceSelectorStrategyParams.value = []
  errors.value = {}
  store.resourceTypeDrawerState = {
    visible: false,
    isEditMode: CreateEditMode.None,
    resourceTypeIndex: -1
  }
  closeStrategyDrawer()
}

const validateResourceType = () => {
  const validationErrors: ResourceTypeErrors = {}
  if (!name.value.trim()) {
    validationErrors.name = 'Name is required'
  }
  if (!label.value.trim()) {
    validationErrors.label = 'Label is required'
  }
  if (!storageStrategy.value?._value) {
    validationErrors.storageStrategy = 'Storage Strategy is required'
  }
  if (!persistenceSelectorStrategy.value?._value) {
    validationErrors.persistenceSelectorStrategy = 'Persistence Selector Strategy is required'
  }
  return validationErrors
}

const loadResourceType = (resourceType: SnmpCollectionResourceTypePayload | null) => {
  if (resourceType === null) {
    name.value = ''
    label.value = ''
    resourceLabel.value = ''
    status.value = true
    storageStrategy.value = undefined as unknown as IAutocompleteItemType
    storageStrategyParams.value = []
    persistenceSelectorStrategy.value = undefined as unknown as IAutocompleteItemType
    persistenceSelectorStrategyParams.value = []
  }
  if (resourceType !== null) {
    const resourceType = store.configForm.resourceType[store.resourceTypeDrawerState.resourceTypeIndex] || null
    if (resourceType) {
      name.value = resourceType.name
      label.value = resourceType.label
      resourceLabel.value = resourceType.resourceLabel
      status.value = resourceType.enabled
      storageStrategyParams.value = JSON.parse(resourceType.storageStrategyParams || '[]')
      persistenceSelectorStrategyParams.value = JSON.parse(resourceType.persistenceSelectorParams || '[]')
      nextTick(() => {
        storageStrategy.value = { _text: resourceType.storageStrategy, _value: resourceType.storageStrategy }
        persistenceSelectorStrategy.value = { _text: resourceType.persistenceSelectorStrategy, _value: resourceType.persistenceSelectorStrategy }
      })
    }
  }
}


const saveResourceType = () => {
  if (Object.keys(errors.value).length > 0) {
    return
  }

  const payload = mapSnmpDataCollectionResourceTypePayloadToServer(
    name.value,
    label.value,
    resourceLabel.value,
    persistenceSelectorStrategy.value?._value as string,
    persistenceSelectorStrategyParams.value,
    storageStrategy.value?._value as string,
    storageStrategyParams.value,
    status.value,
    0,
    CreateEditMode.Create
  )

  if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Edit) {
    store.configForm.resourceType[store.resourceTypeDrawerState.resourceTypeIndex] = payload
  } else {
    store.configForm.resourceType.push(payload)
  }
  handleCancel()
}

watchEffect(() => {
  errors.value = validateResourceType()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => store.resourceTypeDrawerState.visible,
  (newVal) => {
    if (newVal) {
      const resourceType = store.configForm.resourceType[store.resourceTypeDrawerState.resourceTypeIndex] || null
      loadResourceType(resourceType)
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import '@featherds/table/scss/table';
@import '@/styles/_transitionDataTable';

.resource-type-form-card {
  padding: 20px;
  margin-bottom: 20px;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .title-container {
      .title {
        @include headline3;
        margin: 0;
      }
    }

    .action-container {
      display: flex;
      align-items: center;
      gap: 10px;

      button {
        margin: 0;
      }
    }
  }

  .spacer {
    min-height: 0.5em;
  }

  .content {
    .spacer {
      min-height: 0.5em;
    }

    .persistence-selector-strategy-table-container,
    .storage-strategy-table-container {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .title {
          h3 {
            @include headline3;
          }
        }
      }

      table {
        width: 100%;
        @include table;

        thead {
          background: var($background);
          text-transform: uppercase;
        }

        td {
          white-space: nowrap;
          box-shadow: none;
          border-bottom: 1px solid var($border-on-surface);

          .action-container {
            display: flex;
            align-items: center;
            gap: 5px;
          }
        }
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
  }
}
</style>

