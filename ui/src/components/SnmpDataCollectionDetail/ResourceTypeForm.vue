<template>
  <TableCard class="resource-type-form-container">
    <div class="header">
      <div class="title-container">
        <h2 class="title">{{ title }}</h2>
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
        <FeatherSelect
          label="Storage Strategy"
          data-test="resource-type-storage-strategy-input"
          :options="STORAGE_STRATEGY_OPTIONS"
          v-model="storageStrategy"
          :error="errors.storageStrategy"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
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
        <FeatherSelect
          label="Persistence Selector Strategy"
          data-test="resource-type-persistence-selector-strategy-input"
          :options="PERSISTENCE_SELECTOR_STRATEGY_OPTIONS"
          v-model="persistenceSelectorStrategy"
          :error="errors.persistenceSelectorStrategy"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
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
    <ResourceTypeParameterDrawer
      :state="resourceTypeDrawerState"
      @cancel="closeStrategyDrawer"
      @save="saveParameters"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { PERSISTENCE_SELECTOR_STRATEGY_OPTIONS, STATUS_OPTIONS, STORAGE_STRATEGY_OPTIONS } from '@/lib/constants'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, ResourceTypeErrors, StorageStrategyForm } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'
import ResourceTypeParameterDrawer from './Drawer/ResourceTypeParameterDrawer.vue'
import { mapSnmpDataCollectionResourceTypePayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createResourceType, updateResourceType } from '@/services/snmpDataCollectionService'

const store = useSnmpDataCollectionDetailStore()
const name = ref('')
const resourceLabel = ref('')
const label = ref('')
const status = ref(true)
const storageStrategy = ref<ISelectItemType>()
const storageStrategyParams = ref<StorageStrategyForm[]>([])
const persistenceSelectorStrategy = ref<ISelectItemType>()
const persistenceSelectorStrategyParams = ref<PersistSelectorStrategyForm[]>([])
const errors = ref<ResourceTypeErrors>({})
const snackbar = useSnackbar()
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
const title = computed(() =>
  store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create Resource Type'
    : 'Edit Resource Type'
)

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

const loadResourceTypeData = () => {
  if (store.resourceTypeDrawerState.isEditMode === CreateEditMode.Create) {
    storageStrategyParams.value = []
    persistenceSelectorStrategyParams.value = []
    storageStrategy.value = STORAGE_STRATEGY_OPTIONS[0]
    persistenceSelectorStrategy.value = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS[0]
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
      storageStrategy.value = STORAGE_STRATEGY_OPTIONS.find(option => option._value === resourceType.storageStrategy)
      persistenceSelectorStrategy.value = PERSISTENCE_SELECTOR_STRATEGY_OPTIONS.find(option => option._value === resourceType.persistenceSelectorStrategy)
      storageStrategyParams.value = JSON.parse(resourceType.storageStrategyParams || '[]')
      persistenceSelectorStrategyParams.value = JSON.parse(resourceType.persistenceSelectorParams || '[]')
    }
  }
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
  storageStrategyParams.value.splice(index, 1)
}

const deletePersistenceSelectorStrategy = (index: number) => {
  persistenceSelectorStrategyParams.value.splice(index, 1)
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

const closeResourceTypeDrawer = () => {
  closeStrategyDrawer()
  store.closeResourceTypeDrawer()
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

watchEffect(() => {
  errors.value = validateResourceType()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

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
      storageStrategy.value = undefined
      persistenceSelectorStrategy.value = undefined
      storageStrategyParams.value = []
      persistenceSelectorStrategyParams.value = []
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

.resource-type-form-container {
  margin-top: 10px;
  padding: 25px;

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

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
  }
}
</style>

