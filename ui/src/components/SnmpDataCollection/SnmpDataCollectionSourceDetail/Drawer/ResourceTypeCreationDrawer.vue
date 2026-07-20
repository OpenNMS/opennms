<template>
  <Drawer
    id="drawer"
    data-test="resource-type-drawer"
    v-model:visible="store.resourceTypeDrawerState.visible"
    position="right"
    :header="drawerTitle"
    :style="{ width: '80rem' }"
    @hide="closeResourceTypeDrawer"
    class="resource-type-drawer"
  >
    <div class="container">
      <div class="content">
        <div class="switch-row">
          <ToggleSwitch
            v-model="status"
            data-test="system-def-status-input"
          />
          <label class="switch-label">{{ status ? 'Enabled' : 'Disabled' }}</label>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Name"
          :for="nameId"
          :error="errors.name"
        >
          <InputText
            :id="nameId"
            v-model.trim="name"
            :invalid="!!errors.name"
            data-test="resource-type-name-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Label"
          :for="labelId"
          :error="errors.label"
        >
          <InputText
            :id="labelId"
            v-model.trim="label"
            :invalid="!!errors.label"
            data-test="resource-type-label-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Resource Label"
          :for="resourceLabelId"
          :error="errors.resourceLabel"
        >
          <InputText
            :id="resourceLabelId"
            v-model.trim="resourceLabel"
            :invalid="!!errors.resourceLabel"
            data-test="resource-type-resource-label-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Storage Strategy"
          :for="storageStrategyId"
          :error="errors.storageStrategy"
        >
          <AutoComplete
            :inputId="storageStrategyId"
            v-model="storageStrategy"
            :suggestions="storageStrategyResults"
            optionLabel="_text"
            @complete="onSearchStorageStrategy"
            :invalid="!!errors.storageStrategy"
            dropdown
            fluid
            class="my-autocomplete"
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="storage-strategy-table-container">
          <div class="header">
            <div class="title">
              <h3>Storage Strategy Parameters</h3>
            </div>
            <div class="action">
              <Button
                outlined
                label="Add Storage Strategy Parameter"
                data-test="add-storage-strategy-button"
                @click="openStorageStrategyDrawer(CreateEditMode.Create)"
              />
            </div>
          </div>
          <DataTable
            :value="storageStrategyParams"
            paginator
            :rows="3"
            :rowsPerPageOptions="[3, 6, 9]"
            data-test="storage-strategy-table"
          >
            <Column
              field="key"
              header="Key"
            />
            <Column
              field="value"
              header="Value"
            />
            <Column header="Action">
              <template #body="{ data }">
                <div class="action-container">
                  <Button
                    text
                    title="Edit Storage Strategy Parameter"
                    data-test="edit-storage-strategy-button"
                    @click="openStorageStrategyDrawer(CreateEditMode.Edit, storageStrategyParams.indexOf(data), data)"
                  >
                    <FeatherIcon :icon="Edit" />
                  </Button>
                  <Button
                    text
                    title="Delete Storage Strategy Parameter"
                    data-test="delete-storage-strategy-button"
                    @click="deleteStorageStrategy(storageStrategyParams.indexOf(data))"
                  >
                    <FeatherIcon :icon="Delete" />
                  </Button>
                </div>
              </template>
            </Column>
            <template #empty>
              <EmptyList :content="{ msg: 'No Storage Strategy parameters added yet.' }" />
            </template>
          </DataTable>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Persistence Selector Strategy"
          :for="persistenceSelectorStrategyId"
          :error="errors.persistenceSelectorStrategy"
        >
          <AutoComplete
            :inputId="persistenceSelectorStrategyId"
            v-model="persistenceSelectorStrategy"
            :suggestions="persistenceSelectorStrategyResults"
            optionLabel="_text"
            @complete="onSearchPersistenceSelectorStrategy"
            :invalid="!!errors.persistenceSelectorStrategy"
            dropdown
            fluid
            class="my-autocomplete"
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="persistence-selector-strategy-table-container">
          <div class="header">
            <div class="title">
              <h3>Persistence Selector Strategy Parameters</h3>
            </div>
            <div class="action">
              <Button
                outlined
                label="Add Persistence Selector Strategy Parameter"
                data-test="add-persistence-selector-strategy-button"
                @click="openPersistenceSelectorStrategyDrawer(CreateEditMode.Create)"
              />
            </div>
          </div>
          <DataTable
            :value="persistenceSelectorStrategyParams"
            paginator
            :rows="3"
            :rowsPerPageOptions="[3, 6, 9]"
            data-test="persistence-selector-strategy-table"
          >
            <Column
              field="key"
              header="Key"
            />
            <Column
              field="value"
              header="Value"
            />
            <Column header="Action">
              <template #body="{ data }">
                <div class="action-container">
                  <Button
                    text
                    title="Edit Persistence Selector Strategy Parameter"
                    data-test="edit-persistence-selector-strategy-button"
                    @click="openPersistenceSelectorStrategyDrawer(CreateEditMode.Edit, persistenceSelectorStrategyParams.indexOf(data), data)"
                  >
                    <FeatherIcon :icon="Edit" />
                  </Button>
                  <Button
                    text
                    title="Delete Persistence Selector Strategy Parameter"
                    data-test="delete-persistence-selector-strategy-button"
                    @click="deletePersistenceSelectorStrategy(persistenceSelectorStrategyParams.indexOf(data))"
                  >
                    <FeatherIcon :icon="Delete" />
                  </Button>
                </div>
              </template>
            </Column>
            <template #empty>
              <EmptyList :content="{ msg: 'No Persistence Selector Strategy parameters added yet.' }" />
            </template>
          </DataTable>
        </div>
      </div>
      <div
        class="sub-container"
        v-if="resourceTypeDrawerState.visible"
      >
        <div class="header">
          <h4>{{ parameterDrawerTitle }}</h4>
        </div>
        <div class="spacer"></div>
        <div class="content">
          <FormField
            label="Key"
            :for="keyId"
            :error="parameterErrors.key"
          >
            <InputText
              :id="keyId"
              v-model.trim="key"
              :invalid="!!parameterErrors.key"
              data-test="resource-type-parameter-key-input"
              fluid
            />
          </FormField>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FormField
            label="Value"
            :for="valueId"
            :error="parameterErrors.value"
          >
            <InputText
              :id="valueId"
              v-model.trim="value"
              :invalid="!!parameterErrors.value"
              data-test="resource-type-parameter-value-input"
              fluid
            />
          </FormField>
        </div>
        <div class="spacer"></div>
        <div class="footer">
          <Button
            text
            label="Cancel"
            data-test="cancel-resource-type-parameter-button"
            @click="closeParameterDrawer"
          />
          <Button
            label="Save"
            data-test="save-resource-type-parameter-button"
            @click="saveResourceTypeParameter"
            :disabled="isParameterSaveDisabled"
          />
        </div>
      </div>
      <div
        class="footer"
        v-if="!resourceTypeDrawerState.visible"
      >
        <Button
          text
          label="Cancel"
          data-test="cancel-resource-type"
          @click="closeResourceTypeDrawer"
        />
        <Button
          label="Save"
          data-test="save-resource-type"
          @click="saveResourceType"
          :disabled="isSaveDisabled"
        />
      </div>
    </div>
  </Drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, useId, watch, watchEffect } from 'vue'

import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import { KEY_PATTERN, PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionResourceTypePayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createResourceType, updateResourceType } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, ResourceTypeErrors, StorageStrategyForm } from '@/types/snmpDataCollection'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import AutoComplete from 'primevue/autocomplete'
import Button from 'primevue/button'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Drawer from 'primevue/drawer'
import InputText from 'primevue/inputtext'
import ToggleSwitch from 'primevue/toggleswitch'

const store = useSnmpDataCollectionDetailStore()
const nameId = useId()
const labelId = useId()
const resourceLabelId = useId()
const storageStrategyId = useId()
const persistenceSelectorStrategyId = useId()
const keyId = useId()
const valueId = useId()
const persistenceSelectorStrategyResults = ref([] as IAutocompleteItemType[])
const storageStrategyResults = ref([] as IAutocompleteItemType[])
const name = ref('')
const resourceLabel = ref('')
const label = ref('')
const status = ref(true)
const storageStrategy = ref(undefined as unknown as IAutocompleteItemType)
const storageStrategyParams = ref<StorageStrategyForm[]>([])
const persistenceSelectorStrategy = ref(undefined as unknown as IAutocompleteItemType)
const persistenceSelectorStrategyParams = ref<PersistSelectorStrategyForm[]>([])
const errors = ref<ResourceTypeErrors>({})
const snackbar = useSnackbar()
const isSaveDisabled = ref(true)
const drawerTitle = computed(() =>
  store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create ? 'Create Resource Type' : 'Edit Resource Type'
)
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
const key = ref('')
const value = ref('')
const parameterErrors = ref<{ key?: string, value?: string }>({})
const isParameterSaveDisabled = ref(true)
const parameterDrawerTitle = computed(() => {
  if (resourceTypeDrawerState.value.type === 'storageStrategy') {
    return resourceTypeDrawerState.value.isEditMode === CreateEditMode.Create ? 'Create Storage Strategy Parameter' : 'Edit Storage Strategy Parameter'
  }
  if (resourceTypeDrawerState.value.type === 'persistenceSelectorStrategy') {
    return resourceTypeDrawerState.value.isEditMode === CreateEditMode.Create ? 'Create Persistence Selector Strategy Parameter' : 'Edit Persistence Selector Strategy Parameter'
  }
  return 'Parameter'
})

const buildStrategyResults = (options: string[], query: string): IAutocompleteItemType[] => {
  const q = (query || '').toLowerCase()
  const filtered = options
    .filter(x => x.toLowerCase().indexOf(q) > -1)
    .map(x => ({ _text: x, _value: x }))

  // If no matches found and query is not empty, offer the typed value as a custom option
  if (filtered.length === 0 && query.trim()) {
    filtered.push({ _text: query, _value: query })
  }
  return filtered
}

const onSearchStorageStrategy = (event: { query: string }) => {
  storageStrategyResults.value = buildStrategyResults(STORAGE_STRATEGY_OPTIONS, event.query)
}

const onSearchPersistenceSelectorStrategy = (event: { query: string }) => {
  persistenceSelectorStrategyResults.value = buildStrategyResults(PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, event.query)
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

const deleteStorageStrategy = (index: number) => {
  if (index < 0) {
    return
  }
  storageStrategyParams.value.splice(index, 1)
}

const openPersistenceSelectorStrategyDrawer = (
  isEditMode: CreateEditMode,
  persistenceSelectorStrategyIndex = -1,
  persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null = null
) => {
  resourceTypeDrawerState.value.visible = true
  resourceTypeDrawerState.value.type = 'persistenceSelectorStrategy'
  resourceTypeDrawerState.value.isEditMode = isEditMode
  resourceTypeDrawerState.value.persistenceSelectorStrategyIndex = persistenceSelectorStrategyIndex
  resourceTypeDrawerState.value.persistenceSelectorStrategyObject = persistenceSelectorStrategyObject
}

const deletePersistenceSelectorStrategy = (index: number) => {
  if (index < 0) {
    return
  }
  persistenceSelectorStrategyParams.value.splice(index, 1)
}

const closeResourceTypeDrawer = async () => {
  name.value = ''
  label.value = ''
  resourceLabel.value = ''
  status.value = true
  storageStrategy.value = undefined as unknown as IAutocompleteItemType
  storageStrategyParams.value = []
  persistenceSelectorStrategy.value = undefined as unknown as IAutocompleteItemType
  persistenceSelectorStrategyParams.value = []
  errors.value = {}
  closeParameterDrawer()
  await store.closeResourceTypeDrawer()
}

const saveResourceType = async () => {
  errors.value = validateResourceType()
  if (Object.keys(errors.value).length > 0) {
    return
  }

  if (!store.selectedCollectionSource?.id) {
    snackbar.showSnackBar({ msg: 'Please select a Collection Source first.', error: true })
    return
  }

  try {
    const payload = mapSnmpDataCollectionResourceTypePayloadToServer(
      name.value,
      label.value,
      resourceLabel.value,
      persistenceSelectorStrategy.value?._value as string,
      persistenceSelectorStrategyParams.value,
      storageStrategy.value?._value as string,
      storageStrategyParams.value,
      status.value,
      store.selectedResourceType?.id || 0,
      store.resourceTypeDrawerState.isEditMode
    )

    let response
    if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createResourceType(payload, store.selectedCollectionSource.id)
    }
    if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateResourceType(payload, store.selectedCollectionSource.id)
    }

    if (response) {
      snackbar.showSnackBar({ msg: `Resource Type ${store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create ? 'created' : 'updated'} successfully.` })
      await store.fetchResourceTypes()
      closeResourceTypeDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'An error occurred while saving the Resource Type. Please try again.', error: true })
    }
  } catch (e) {
    console.error('Error saving Resource Type:', e)
    snackbar.showSnackBar({ msg: 'An error occurred while saving the Resource Type. Please try again.', error: true })
  }
}

const closeParameterDrawer = () => {
  errors.value = {}
  key.value = ''
  value.value = ''
  isParameterSaveDisabled.value = true
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

const saveResourceTypeParameter = () => {
  if (resourceTypeDrawerState.value.type === 'storageStrategy') {
    saveParameters('storageStrategy', key.value, value.value)
  }
  if (resourceTypeDrawerState.value.type === 'persistenceSelectorStrategy') {
    saveParameters('persistenceSelectorStrategy', key.value, value.value)
  }
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
  closeParameterDrawer()
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

const validateResourceTypeParameter = () => {
  const newErrors: { key?: string, value?: string } = {}
  if (!key.value.trim()) {
    newErrors.key = 'Key is required'
  }
  if (!KEY_PATTERN.test(key.value)) {
    newErrors.key = 'Key can only contain letters, numbers, underscores, and hyphens'
  }
  if (!value.value.trim()) {
    newErrors.value = 'Value is required'
  }
  return newErrors
}

const loadResourceTypeData = () => {
  if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create) {
    storageStrategyParams.value = []
    persistenceSelectorStrategyParams.value = []
    storageStrategy.value = undefined as unknown as IAutocompleteItemType
    persistenceSelectorStrategy.value = undefined as unknown as IAutocompleteItemType
    name.value = ''
    label.value = ''
    resourceLabel.value = ''
    status.value = true
  }
  if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Edit) {
    const resourceType = store.selectedResourceType
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

const loadResourceTypeParameterData = () => {
  if (resourceTypeDrawerState.value.type === 'storageStrategy' && resourceTypeDrawerState.value.storageStrategyObject) {
    const parameter = resourceTypeDrawerState.value.storageStrategyObject
    if (parameter) {
      key.value = parameter.key
      value.value = parameter.value
    }
  }
  if (resourceTypeDrawerState.value.type === 'persistenceSelectorStrategy' && resourceTypeDrawerState.value.persistenceSelectorStrategyObject) {
    const parameter = resourceTypeDrawerState.value.persistenceSelectorStrategyObject
    if (parameter) {
      key.value = parameter.key
      value.value = parameter.value
    }
  }
}

watchEffect(() => {
  errors.value = validateResourceType()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watchEffect(() => {
  parameterErrors.value = validateResourceTypeParameter()
  isParameterSaveDisabled.value = Object.keys(parameterErrors.value).length > 0
})

watch(
  () => resourceTypeDrawerState.value.visible,
  (visible) => {
    if (visible) {
      loadResourceTypeParameterData()
    } else {
      errors.value = {}
      key.value = ''
      value.value = ''
      isSaveDisabled.value = true
    }
  },
  { immediate: true }
)

watch(
  () => store.resourceTypeDrawerState.visible,
  (visible) => {
    if (visible) {
      loadResourceTypeData()
    } else {
      name.value = ''
      label.value = ''
      resourceLabel.value = ''
      status.value = true
      storageStrategy.value = undefined as unknown as IAutocompleteItemType
      storageStrategyParams.value = []
      persistenceSelectorStrategy.value = undefined as unknown as IAutocompleteItemType
      persistenceSelectorStrategyParams.value = []
      errors.value = {}
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@import '@featherds/styles/mixins/typography';

.container {
  margin-top: 10px;

  .content {
    .spacer {
      min-height: 0.5em;
    }

    .switch-row {
      display: flex;
      align-items: center;
      gap: 0.75rem;

      .switch-label {
        font-size: 12px;
        font-weight: 600;
      }
    }

    .my-autocomplete {
      width: 100%;
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
    }
  }

  .action-container {
    display: flex;
    align-items: center;
    gap: 5px;
  }

  .sub-container {
    border: 1px solid var(--p-content-border-color);
    border-radius: 6px;
    margin-top: 1rem;

    .header {
      padding: 20px;
      margin: 0;

      h4 {
        @include headline4;
      }
    }

    .spacer {
      min-height: 0.5em;
    }

    .content {
      padding: 0 20px;
    }

    .footer {
      padding: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
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
