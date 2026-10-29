<template>
  <OnmsDrawer
    id="drawer"
    data-test="mib-group-drawer"
    v-model:visible="store.mibGroupDrawerState.visible"
    :header="drawerTitle"
    width="80rem"
    @hide="closeMibGroupDrawer"
    class="mib-group-drawer"
  >
    <div class="container">
      <div class="content">
        <div class="switch-row">
          <OnmsToggleSwitch
            v-model="status"
            data-test="system-def-status-input"
          />
          <label class="switch-label">{{ status ? 'Enabled' : 'Disabled' }}</label>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="label">General Details</div>
        <FormField
          label="Name"
          :for="nameId"
          :error="errors.name"
        >
          <OnmsInputText
            :id="nameId"
            v-model.trim="name"
            :invalid="!!errors.name"
            data-test="mib-group-name-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="Interface Type"
          :for="ifTypeId"
          :error="errors.ifType"
        >
          <OnmsAutoComplete
            :inputId="ifTypeId"
            :modelValue="ifType"
            @update:modelValue="ifType = ($event as string | null) ?? ''"
            :suggestions="ifTypeSuggestions"
            @complete="searchIfTypes"
            :invalid="!!errors.ifType"
            placeholder="all, ignore, 6 or 6,22"
            data-test="mib-group-if-type-input"
            dropdown
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="table-container">
          <div class="header">
            <div class="title">
              <h3>MIB Objects</h3>
            </div>
            <div class="action">
              <OnmsButton
                variant="outlined"
                label="Add MIB Object"
                data-test="add-mib-object-button"
                @click="openMibObjectDrawer(-1, null, CreateEditMode.Create)"
              />
            </div>
          </div>
          <OnmsTable
            :value="mibObjects"
            paginator
            :rows="5"
            :rowsPerPageOptions="[5, 10, 15, 20]"
            data-test="mib-objects-table"
          >
            <OnmsColumn
              field="oid"
              header="OID"
            />
            <OnmsColumn
              field="instance"
              header="Instance"
            />
            <OnmsColumn
              field="alias"
              header="Alias"
            />
            <OnmsColumn
              field="type"
              header="Type"
            />
            <OnmsColumn header="Action">
              <template #body="{ data }">
                <div class="action-container">
                  <OnmsIconButton
                    title="Edit MIB Object"
                    data-test="edit-mib-object-button"
                    :icon="Edit"
                    @click="openMibObjectDrawer(mibObjects.indexOf(data), data, CreateEditMode.Edit)"
                  />
                  <OnmsIconButton
                    title="Delete MIB Object"
                    data-test="delete-mib-object-button"
                    :icon="Delete"
                    @click="deleteMibObject(mibObjects.indexOf(data))"
                  />
                </div>
              </template>
            </OnmsColumn>
            <template #empty>
              <EmptyList :content="{ msg: 'No MIB Objects added yet.' }" />
            </template>
          </OnmsTable>
        </div>
      </div>
      <div
        class="sub-container"
        v-if="mibObjectDrawerState.visible"
      >
        <div class="header">
          <h4>{{ mibObjectDrawerTitle }}</h4>
        </div>
        <div class="spacer"></div>
        <div class="content">
          <FormField
            label="OID"
            :for="oidId"
            :error="mibObjectErrors.oid"
          >
            <OnmsInputText
              :id="oidId"
              v-model.trim="oid"
              :invalid="!!mibObjectErrors.oid"
              data-test="mib-object-oid-input"
              fluid
            />
          </FormField>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FormField
            label="Instance"
            :for="instanceId"
            :error="mibObjectErrors.instance"
          >
            <OnmsAutoComplete
              :inputId="instanceId"
              :modelValue="instance"
              @update:modelValue="instance = ($event as string | null) ?? ''"
              :suggestions="instanceSuggestions"
              @complete="searchInstances"
              :invalid="!!mibObjectErrors.instance"
              placeholder="0, 1.5, ifIndex or a resource type"
              data-test="mib-object-instance-input"
              dropdown
              fluid
            />
          </FormField>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FormField
            label="Alias"
            :for="aliasId"
            :error="mibObjectErrors.alias"
          >
            <OnmsInputText
              :id="aliasId"
              v-model.trim="alias"
              :invalid="!!mibObjectErrors.alias"
              data-test="mib-object-alias-input"
              fluid
            />
          </FormField>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FormField
            label="Data Type"
            :for="dataTypeId"
            :error="mibObjectErrors.type"
          >
            <OnmsSelect
              :inputId="dataTypeId"
              :modelValue="dataType"
              @update:modelValue="dataType = $event as ISelectItemType"
              :options="MIB_OBJECT_DATA_TYPE_OPTIONS"
              optionLabel="_text"
              :invalid="!!mibObjectErrors.type"
              data-test="mib-object-data-type-input"
              fluid
            />
          </FormField>
        </div>
        <div class="spacer"></div>
        <div class="footer">
          <OnmsButton
            variant="ghost"
            label="Cancel"
            data-test="cancel-mib-object-button"
            @click="closeMibObjectDrawer"
          />
          <OnmsButton
            label="Save MIB Object"
            data-test="save-mib-object-button"
            @click="saveMibObject"
            :disabled="isMibObjectSaveDisabled"
          />
        </div>
      </div>
      <div
        class="footer"
        v-if="!mibObjectDrawerState.visible"
      >
        <OnmsButton
          variant="ghost"
          label="Cancel"
          data-test="cancel-mib-group"
          @click="closeMibGroupDrawer"
        />
        <OnmsButton
          label="Save MIB Group"
          data-test="save-mib-group"
          :disabled="isSaveDisabled"
          @click="saveMibGroup"
        />
      </div>
    </div>
  </OnmsDrawer>
</template>

<script lang="ts" setup>
import { computed, ref, useId, watch, watchEffect } from 'vue'

import EmptyList from '@/components/Common/EmptyList.vue'
import useSnackbar from '@/composables/useSnackbar'
import {
  DEFAULT_IF_TYPE,
  DEFAULT_MIB_OBJ_TYPE,
  DEFAULT_STATUS,
  IF_TYPE_ALL,
  IF_TYPE_IGNORE,
  IF_TYPE_LIST_PATTERN,
  IF_TYPE_SUGGESTIONS,
  INSTANCE_ID_PATTERN,
  MIB_OBJECT_DATA_TYPE_OPTIONS,
  OID_PATTERN
} from '@/lib/constants'
import { mapSnmpDataCollectionMibGroupPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createMibGroup, updateMibGroup } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { MibGroupErrors, MibGroupObjectForm, MibGroupObjectFormErrors } from '@/types/snmpDataCollection'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'
import Edit from '@opennms/onms-ui/icons/action/Edit.vue'
import { ISelectItemType } from '@/types'
import FormField from '@/components/Common/FormField.vue'
import { OnmsAutoComplete, OnmsButton, OnmsColumn, OnmsDrawer, OnmsIconButton, OnmsInputText, OnmsSelect, OnmsTable, OnmsToggleSwitch } from '@opennms/onms-ui'

const store = useSnmpDataCollectionDetailStore()
const nameId = useId()
const ifTypeId = useId()
const oidId = useId()
const instanceId = useId()
const aliasId = useId()
const dataTypeId = useId()
const DEFAULT_INSTANCE = '0'
const name = ref('')
const ifType = ref<string>(DEFAULT_IF_TYPE)
const ifTypeSuggestions = ref<string[]>([])
const mibObjects = ref<MibGroupObjectForm[]>([])
const status = ref(DEFAULT_STATUS)
const errors = ref<MibGroupErrors>({})
const isSaveDisabled = ref(true)
const snackbar = useSnackbar()
const oid = ref('')
const dataType = ref<ISelectItemType>(DEFAULT_MIB_OBJ_TYPE)
const instance = ref<string>(DEFAULT_INSTANCE)
const instanceSuggestions = ref<string[]>([])
const alias = ref('')
const mibObjectErrors = ref<MibGroupObjectFormErrors>({})
const isMibObjectSaveDisabled = ref(true)
const drawerTitle = computed(() =>
  store.mibGroupDrawerState.isEditMode === CreateEditMode.Create ? 'Create MIB Group' : 'Edit MIB Group'
)
const mibObjectDrawerState = ref<{
  visible: boolean
  isEditMode: CreateEditMode
  mibObjectIndex: number
  mibObject: MibGroupObjectForm | null
}>({
  visible: false,
  isEditMode: CreateEditMode.Create,
  mibObjectIndex: -1,
  mibObject: null
})
const mibObjectDrawerTitle = computed(() =>
  mibObjectDrawerState.value.isEditMode === CreateEditMode.Create
    ? 'Create MIB Object'
    : 'Edit MIB Object'
)

const closeMibGroupDrawer = async () => {
  oid.value = ''
  dataType.value = DEFAULT_MIB_OBJ_TYPE
  instance.value = DEFAULT_INSTANCE
  alias.value = ''
  name.value = ''
  ifType.value = DEFAULT_IF_TYPE
  status.value = true
  mibObjects.value = []
  errors.value = {}
  isSaveDisabled.value = true
  isMibObjectSaveDisabled.value = true
  closeMibObjectDrawer()
  await store.closeMibGroupDrawer()
}

const deleteMibObject = (index: number) => {
  if (index < 0) {
    return
  }
  mibObjects.value.splice(index, 1)
}

const openMibObjectDrawer = (index: number, mibObject: MibGroupObjectForm | null, isEditMode: CreateEditMode) => {
  mibObjectDrawerState.value = {
    visible: true,
    isEditMode: isEditMode,
    mibObjectIndex: index,
    mibObject: mibObject
  }
}

const closeMibObjectDrawer = () => {
  mibObjectErrors.value = {}
  oid.value = ''
  dataType.value = DEFAULT_MIB_OBJ_TYPE
  instance.value = DEFAULT_INSTANCE
  alias.value = ''
  isMibObjectSaveDisabled.value = true
  mibObjectDrawerState.value = {
    visible: false,
    isEditMode: CreateEditMode.None,
    mibObjectIndex: -1,
    mibObject: null
  }
}

const saveMibObject = () => {
  const validationErrors = validateMibObject()
  mibObjectErrors.value = validationErrors
  if (Object.keys(validationErrors).length === 0) {
    // The form does not show maxval and minval. Thus keep the values of the edited MIB object.
    const editedMibObject = mibObjectDrawerState.value.mibObject
    const mibObject: MibGroupObjectForm = {
      oid: oid.value,
      alias: alias.value,
      instance: instance.value.trim(),
      type: dataType.value._value as string,
      maxval: editedMibObject?.maxval ?? null,
      minval: editedMibObject?.minval ?? null
    }
    if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Create) {
      mibObjects.value.push(mibObject)
    } else if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Edit) {
      const index = mibObjectDrawerState.value.mibObjectIndex
      if (index !== -1) {
        mibObjects.value[index] = mibObject
      }
    }
    closeMibObjectDrawer()
  }
}

const saveMibGroup = async () => {
  errors.value = validateMibGroup()
  if (Object.keys(errors.value).length > 0) {
    return
  }

  if (!store.selectedCollectionSource?.id) {
    snackbar.showSnackBar({ msg: 'Please select a Collection Source first.', error: true })
    return
  }

  try {
    const payload = mapSnmpDataCollectionMibGroupPayloadToServer(
      name.value,
      normalizeIfType(ifType.value),
      store.selectedMibGroup?.mibGroupNames || [],
      mibObjects.value,
      store.selectedMibGroup?.mibObjProperties ?? null,
      status.value,
      store.selectedMibGroup?.id || 0,
      store.mibGroupDrawerState.isEditMode
    )
    let response
    if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createMibGroup(payload, store.selectedCollectionSource.id)
    } else if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateMibGroup(payload, store.selectedCollectionSource.id)
    }

    if (response) {
      await store.fetchMibGroups()
      snackbar.showSnackBar({ msg: `MIB Group ${store.mibGroupDrawerState.isEditMode === CreateEditMode.Create ? 'created' : 'updated'} successfully.` })
      closeMibGroupDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'An error occurred while saving the MIB Group.', error: true })
    }
  } catch (error) {
    console.error('Error saving MIB Group:', error)
    snackbar.showSnackBar({ msg: 'An error occurred while saving the MIB Group.', error: true })
  }
}

// An edit can keep the stored instance. When the resource type names are not loaded,
// the form accepts any value. The server logs a warning at reload for an unknown resource type.
const isValidInstance = (value: string) =>
  INSTANCE_ID_PATTERN.test(value) ||
  value === 'ifIndex' ||
  value === mibObjectDrawerState.value.mibObject?.instance ||
  store.resourceTypeNames.length === 0 ||
  store.resourceTypeNames.includes(value)

// The collector does not trim the ifType list items, and compares all and ignore in lower case.
const normalizeIfType = (value: string) => {
  const compact = value.replace(/\s+/g, '')
  const lower = compact.toLowerCase()
  return lower === IF_TYPE_ALL || lower === IF_TYPE_IGNORE ? lower : compact
}

const isValidIfType = (value: string) =>
  value === IF_TYPE_ALL || value === IF_TYPE_IGNORE || IF_TYPE_LIST_PATTERN.test(value)

const filterSuggestions = (options: string[], query: string) => {
  const lowerQuery = query.trim().toLowerCase()
  return lowerQuery ? options.filter(option => option.toLowerCase().includes(lowerQuery)) : [...options]
}

const searchIfTypes = (query: string) => {
  ifTypeSuggestions.value = filterSuggestions(IF_TYPE_SUGGESTIONS, query)
}

const searchInstances = (query: string) => {
  instanceSuggestions.value = filterSuggestions(store.resourceTypeNames, query)
}

const validateMibObject = () => {
  const validationErrors: MibGroupObjectFormErrors = {}

  if (!oid.value) {
    validationErrors.oid = 'OID is required'
  }
  if (!OID_PATTERN.test(oid.value)) {
    validationErrors.oid = 'OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)'
  }
  if (!instance.value.trim()) {
    validationErrors.instance = 'Instance is required'
  } else if (!isValidInstance(instance.value.trim())) {
    validationErrors.instance = 'Instance must be an instance identifier such as 0 or 1.5 (no leading zeros), ifIndex or a resource type name'
  }
  if (!dataType.value._value) {
    validationErrors.type = 'Data Type is required'
  }
  if (!alias.value) {
    validationErrors.alias = 'Alias is required'
  }

  return validationErrors
}

const validateMibGroup = (): MibGroupErrors => {
  const validationErrors: MibGroupErrors = {}
  if (!name.value.trim()) {
    validationErrors.name = 'Name is required.'
  }
  if (!ifType.value.trim()) {
    validationErrors.ifType = 'Interface Type is required.'
  } else if (!isValidIfType(normalizeIfType(ifType.value))) {
    validationErrors.ifType = 'Interface Type must be all, ignore, an ifType number or a comma-separated list of ifType numbers, without leading zeros.'
  }
  return validationErrors
}

const loadInitialData = () => {
  if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Create) {
    name.value = ''
    ifType.value = DEFAULT_IF_TYPE
    status.value = true
    mibObjects.value = []
  }
  if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Edit) {
    const group = store.selectedMibGroup
    if (group) {
      name.value = group.name
      ifType.value = group.ifType ?? DEFAULT_IF_TYPE
      status.value = group.enabled
      mibObjects.value = JSON.parse(group.mibObjects) || []
    }
  }
}

const loadMibObjectData = () => {
  if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Create) {
    oid.value = ''
    dataType.value = DEFAULT_MIB_OBJ_TYPE
    instance.value = DEFAULT_INSTANCE
    alias.value = ''
  }

  if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Edit) {
    const mibObject = mibObjectDrawerState.value.mibObject
    if (mibObject) {
      oid.value = mibObject.oid
      dataType.value = { _text: mibObject.type, _value: mibObject.type }
      instance.value = mibObject.instance
      alias.value = mibObject.alias
    }
  }
}

watchEffect(() => {
  mibObjectErrors.value = validateMibObject()
  isMibObjectSaveDisabled.value = Object.keys(mibObjectErrors.value).length > 0
})

watchEffect(() => {
  errors.value = validateMibGroup()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => mibObjectDrawerState.value.visible,
  (visible) => {
    if (visible) {
      loadMibObjectData()
    } else {
      // Reset errors and form data when drawer is closed
      errors.value = {}
      oid.value = ''
      dataType.value = DEFAULT_MIB_OBJ_TYPE
      instance.value = DEFAULT_INSTANCE
      alias.value = ''
      isSaveDisabled.value = true
    }
  },
  { immediate: true }
)

watch(
  () => store.mibGroupDrawerState.visible,
  (visible) => {
    if (visible) {
      loadInitialData()
    } else {
      // Reset form data and errors when drawer is closed
      oid.value = ''
      dataType.value = DEFAULT_MIB_OBJ_TYPE
      instance.value = DEFAULT_INSTANCE
      alias.value = ''
      name.value = ''
      ifType.value = DEFAULT_IF_TYPE
      status.value = true
      mibObjects.value = []
      errors.value = {}
      isSaveDisabled.value = true
      isMibObjectSaveDisabled.value = true
    }
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
@import '@/styles/onms-typography';

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

    .label {
      @include onms-headline4;
      margin-bottom: 0.5em;
    }

    .table-container {
      .header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .title {
          h3 {
            @include onms-headline3;
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
        @include onms-headline4;
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
