<template>
  <FeatherDrawer
    id="drawer"
    data-test="resource-type-drawer"
    v-model="store.resourceTypeDrawerState.visible"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    @hidden="closeResourceTypeDrawer"
    width="80rem"
    class="resource-type-drawer"
  >
    <div class="container">
      <div class="header">
        <div class="title-container">
          <h2 class="title">{{ drawerTitle }}</h2>
        </div>
      </div>
      <div class="content">
        <div class="switch-row">
          <SwitchRender
            :checked="status"
            @click="onChangeStatus"
            data-test="system-def-status-input"
          />
          <label class="switch-label">{{ status ? 'Enabled' : 'Disabled' }}</label>
        </div>
        <div class="spacer"></div>
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
                secondary
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
                v-for="(param, index) in storageStrategyParamsObjects"
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
          <div
            class="alerts-pagination"
            v-if="storageStrategyParamsObjects.length"
          >
            <FeatherPagination
              :modelValue="storageStrategyParamsPage"
              :pageSize="storageStrategyParamsPageSize"
              :total="storageStrategyParamsTotal"
              :pageSizes="[3, 6, 9]"
              @update:modelValue="onStorageStrategyParamsPageChange"
              @update:pageSize="onStorageStrategyParamsPageSizeChange"
              data-test="FeatherPagination"
            />
          </div>
          <div v-if="!storageStrategyParamsObjects.length">
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
                secondary
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
                v-for="(param, index) in persistenceSelectorStrategyParamsObjects"
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
          <div
            class="alerts-pagination"
            v-if="persistenceSelectorStrategyParamsObjects.length"
          >
            <FeatherPagination
              :modelValue="persistenceSelectorStrategyParamsPage"
              :pageSize="persistenceSelectorStrategyParamsPageSize"
              :total="persistenceSelectorStrategyParamsTotal"
              :pageSizes="[3, 6, 9]"
              @update:modelValue="onPersistenceSelectorStrategyParamsPageChange"
              @update:pageSize="onPersistenceSelectorStrategyParamsPageSizeChange"
              data-test="FeatherPagination"
            />
          </div>
          <div v-if="!persistenceSelectorStrategyParamsObjects.length">
            <EmptyList :content="{ msg: 'No Persistence Selector Strategy parameters added yet.' }" />
          </div>
        </div>
      </div>
      <div
        class="sub-container"
        v-if="resourceTypeDrawerState.visible"
      >
        <div class="header">
          <h2>{{ parameterDrawerTitle }}</h2>
        </div>
        <div class="spacer"></div>
        <div class="content">
          <FeatherInput
            label="Key"
            v-model.trim="key"
            data-test="resource-type-parameter-key-input"
            :error="parameterErrors.key"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Value"
            v-model.trim="value"
            data-test="resource-type-parameter-value-input"
            :error="parameterErrors.value"
          />
        </div>
        <div class="spacer"></div>
        <div class="footer">
          <FeatherButton
            data-test="cancel-resource-type-parameter-button"
            @click="closeParameterDrawer"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            data-test="save-resource-type-parameter-button"
            @click="saveResourceTypeParameter"
            :disabled="isParameterSaveDisabled"
          >
            Save
          </FeatherButton>
        </div>
      </div>
      <div
        class="footer"
        v-if="!resourceTypeDrawerState.visible"
      >
        <FeatherButton
          data-test="cancel-resource-type"
          @click="closeResourceTypeDrawer"
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
    </div>
  </FeatherDrawer>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch, watchEffect } from 'vue'

import EmptyList from '@/components/Common/EmptyList.vue'
import useSnackbar from '@/composables/useSnackbar'
import { KEY_PATTERN, PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionResourceTypePayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createResourceType, updateResourceType } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, ResourceTypeErrors, StorageStrategyForm } from '@/types/snmpDataCollection'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { SwitchRender } from '@featherds/switch'

const storageStrategyLoading = ref(false)
const storageStrategyTimeout = ref(-1)
const persistenceSelectorStrategyLoading = ref(false)
const persistenceSelectorStrategyTimeout = ref(-1)
const persistenceSelectorStrategyResults = ref([] as IAutocompleteItemType[])
const storageStrategyResults = ref([] as IAutocompleteItemType[])
const store = useSnmpDataCollectionDetailStore()
const name = ref('')
const resourceLabel = ref('')
const label = ref('')
const status = ref(true)
const storageStrategy = ref(undefined as unknown as IAutocompleteItemType)
const storageStrategyParams = ref<StorageStrategyForm[]>([])
const storageStrategyParamsPage = ref(1)
const storageStrategyParamsPageSize = ref(3)
const storageStrategyParamsTotal = ref(0)
const storageStrategyParamsObjects = ref<StorageStrategyForm[]>([])
const persistenceSelectorStrategy = ref(undefined as unknown as IAutocompleteItemType)
const persistenceSelectorStrategyParams = ref<PersistSelectorStrategyForm[]>([])
const persistenceSelectorStrategyParamsPage = ref(1)
const persistenceSelectorStrategyParamsPageSize = ref(3)
const persistenceSelectorStrategyParamsTotal = ref(0)
const persistenceSelectorStrategyParamsObjects = ref<PersistSelectorStrategyForm[]>([])
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

const onChangeStatus = () => {
  status.value = !status.value
}

const onSearchStorageStrategy = async (q: string) => {
  storageStrategyLoading.value = true
  if (storageStrategyTimeout.value !== -1) {
    clearTimeout(storageStrategyTimeout.value)
  }
  storageStrategyTimeout.value = window.setTimeout(() => {
    const filteredOptions = STORAGE_STRATEGY_OPTIONS
      .filter(x => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map(x => ({
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

const onStorageStrategyParamsPageChange = (page: number) => {
  storageStrategyParamsPage.value = page
  storageStrategyParamsObjects.value = storageStrategyParams.value.slice((page - 1) * storageStrategyParamsPageSize.value, page * storageStrategyParamsPageSize.value)
}

const onStorageStrategyParamsPageSizeChange = (pageSize: number) => {
  storageStrategyParamsPageSize.value = pageSize
  storageStrategyParamsPage.value = 1
  storageStrategyParamsObjects.value = storageStrategyParams.value.slice(0, pageSize)
}

const onPersistenceSelectorStrategyParamsPageChange = (page: number) => {
  persistenceSelectorStrategyParamsPage.value = page
  persistenceSelectorStrategyParamsObjects.value = persistenceSelectorStrategyParams.value.slice((page - 1) * persistenceSelectorStrategyParamsPageSize.value, page * persistenceSelectorStrategyParamsPageSize.value)
}

const onPersistenceSelectorStrategyParamsPageSizeChange = (pageSize: number) => {
  persistenceSelectorStrategyParamsPageSize.value = pageSize
  persistenceSelectorStrategyParamsPage.value = 1
  persistenceSelectorStrategyParamsObjects.value = persistenceSelectorStrategyParams.value.slice(0, pageSize)
}

const openStorageStrategyDrawer = (
  isEditMode: CreateEditMode,
  storageStrategyIndex = -1,
  storageStrategyObject: StorageStrategyForm | null = null
) => {
  const actualIndex = isEditMode === CreateEditMode.Edit ? (storageStrategyParamsPage.value - 1) * storageStrategyParamsPageSize.value + storageStrategyIndex : storageStrategyIndex
  resourceTypeDrawerState.value.visible = true
  resourceTypeDrawerState.value.type = 'storageStrategy'
  resourceTypeDrawerState.value.isEditMode = isEditMode
  resourceTypeDrawerState.value.storageStrategyIndex = actualIndex
  resourceTypeDrawerState.value.storageStrategyObject = storageStrategyObject
}

const deleteStorageStrategy = (index: number) => {
  const actualIndex = (storageStrategyParamsPage.value - 1) * storageStrategyParamsPageSize.value + index
  storageStrategyParams.value.splice(actualIndex, 1)
  storageStrategyParamsTotal.value = storageStrategyParams.value.length

  // If current page is now empty and we're not on the first page, go back one page
  const maxPage = Math.max(1, Math.ceil(storageStrategyParamsTotal.value / storageStrategyParamsPageSize.value))
  if (storageStrategyParamsPage.value > maxPage) {
    storageStrategyParamsPage.value = maxPage
  }

  storageStrategyParamsObjects.value = storageStrategyParams.value.slice((storageStrategyParamsPage.value - 1) * storageStrategyParamsPageSize.value, storageStrategyParamsPage.value * storageStrategyParamsPageSize.value)
}

const onSearchPersistenceSelectorStrategy = async (q: string) => {
  persistenceSelectorStrategyLoading.value = true
  if (persistenceSelectorStrategyTimeout.value !== -1) {
    clearTimeout(persistenceSelectorStrategyTimeout.value)
  }
  persistenceSelectorStrategyTimeout.value = window.setTimeout(() => {
    const filteredOptions = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS
      .filter(x => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map(x => ({
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

const openPersistenceSelectorStrategyDrawer = (
  isEditMode: CreateEditMode,
  persistenceSelectorStrategyIndex = -1,
  persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null = null
) => {
  const actualIndex = isEditMode === CreateEditMode.Edit ? (persistenceSelectorStrategyParamsPage.value - 1) * persistenceSelectorStrategyParamsPageSize.value + persistenceSelectorStrategyIndex : persistenceSelectorStrategyIndex
  resourceTypeDrawerState.value.visible = true
  resourceTypeDrawerState.value.type = 'persistenceSelectorStrategy'
  resourceTypeDrawerState.value.isEditMode = isEditMode
  resourceTypeDrawerState.value.persistenceSelectorStrategyIndex =
    actualIndex
  resourceTypeDrawerState.value.persistenceSelectorStrategyObject = persistenceSelectorStrategyObject
}

const deletePersistenceSelectorStrategy = (index: number) => {
  const actualIndex = (persistenceSelectorStrategyParamsPage.value - 1) * persistenceSelectorStrategyParamsPageSize.value + index
  persistenceSelectorStrategyParams.value.splice(actualIndex, 1)
  persistenceSelectorStrategyParamsTotal.value = persistenceSelectorStrategyParams.value.length

  // If current page is now empty and we're not on the first page, go back one page
  const maxPage = Math.max(1, Math.ceil(persistenceSelectorStrategyParamsTotal.value / persistenceSelectorStrategyParamsPageSize.value))
  if (persistenceSelectorStrategyParamsPage.value > maxPage) {
    persistenceSelectorStrategyParamsPage.value = maxPage
  }

  persistenceSelectorStrategyParamsObjects.value = persistenceSelectorStrategyParams.value.slice((persistenceSelectorStrategyParamsPage.value - 1) * persistenceSelectorStrategyParamsPageSize.value, persistenceSelectorStrategyParamsPage.value * persistenceSelectorStrategyParamsPageSize.value)
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
  storageStrategyParamsObjects.value = []
  persistenceSelectorStrategyParamsObjects.value = []
  storageStrategyParamsPage.value = 1
  persistenceSelectorStrategyParamsPage.value = 1
  storageStrategyParamsPageSize.value = 3
  persistenceSelectorStrategyParamsPageSize.value = 3
  storageStrategyParamsTotal.value = 0
  persistenceSelectorStrategyParamsTotal.value = 0
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
  resourceTypeDrawerState.value.visible = false
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
    storageStrategyParamsTotal.value = storageStrategyParams.value.length
    storageStrategyParamsObjects.value = storageStrategyParams.value.slice((storageStrategyParamsPage.value - 1) * storageStrategyParamsPageSize.value, storageStrategyParamsPage.value * storageStrategyParamsPageSize.value)
  }
  if (type === 'persistenceSelectorStrategy') {
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Edit && resourceTypeDrawerState.value.persistenceSelectorStrategyIndex > -1) {
      persistenceSelectorStrategyParams.value[resourceTypeDrawerState.value.persistenceSelectorStrategyIndex] = { key, value }
    }
    if (resourceTypeDrawerState.value.isEditMode === CreateEditMode.Create) {
      persistenceSelectorStrategyParams.value.push({ key, value })
    }
    persistenceSelectorStrategyParamsTotal.value = persistenceSelectorStrategyParams.value.length
    persistenceSelectorStrategyParamsObjects.value = persistenceSelectorStrategyParams.value.slice((persistenceSelectorStrategyParamsPage.value - 1) * persistenceSelectorStrategyParamsPageSize.value, persistenceSelectorStrategyParamsPage.value * persistenceSelectorStrategyParamsPageSize.value)
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
  storageStrategyParamsTotal.value = storageStrategyParams.value.length
  persistenceSelectorStrategyParamsTotal.value = persistenceSelectorStrategyParams.value.length
  storageStrategyParamsObjects.value = storageStrategyParams.value.slice((storageStrategyParamsPage.value - 1) * storageStrategyParamsPageSize.value, storageStrategyParamsPage.value * storageStrategyParamsPageSize.value)
  persistenceSelectorStrategyParamsObjects.value = persistenceSelectorStrategyParams.value.slice((persistenceSelectorStrategyParamsPage.value - 1) * persistenceSelectorStrategyParamsPageSize.value, persistenceSelectorStrategyParamsPage.value * persistenceSelectorStrategyParamsPageSize.value)
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
      storageStrategyParamsObjects.value = []
      persistenceSelectorStrategyParamsObjects.value = []
      storageStrategyParamsPage.value = 1
      persistenceSelectorStrategyParamsPage.value = 1
      storageStrategyParamsPageSize.value = 3
      persistenceSelectorStrategyParamsPageSize.value = 3
      storageStrategyParamsTotal.value = 0
      persistenceSelectorStrategyParamsTotal.value = 0
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@import '@featherds/styles/themes/variables';
@import '@featherds/styles/mixins/typography';
@import '@featherds/table/scss/table';
@import '@/styles/_transitionDataTable';

.container {
  margin-top: 10px;
  padding: 25px;
  height: 100vh;
  overflow-y: scroll;

  .header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .title-container {
      display: flex;
      align-items: center;

      .title {
        @include headline3;
      }
    }
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

  .sub-container {
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
