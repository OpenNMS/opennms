<template>
  <FeatherDrawer
    id="drawer"
    data-test="mib-group-drawer"
    v-model="store.mibGroupDrawerState.visible"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    @hidden="closeMibGroupDrawer"
    width="80rem"
    class="mib-group-drawer"
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
        <div class="label">General Details</div>
        <div>
          <FeatherInput
            label="Name"
            data-test="mib-group-name-input"
            v-model.trim="name"
            :error="errors.name"
          />
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div>
          <FeatherSelect
            label="Interface Type"
            data-test="mib-group-if-type-input"
            :options="IF_TYPE_FILTERS_OPTIONS"
            v-model="ifType"
            :error="errors.ifType"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="table-container">
          <div class="header">
            <div class="title">
              <h3>MIB Objects</h3>
            </div>
            <div class="action">
              <FeatherButton
                secondary
                data-test="add-mib-object-button"
                @click="openMibObjectDrawer(-1, null, CreateEditMode.Create)"
              >
                Add MIB Object
              </FeatherButton>
            </div>
          </div>
          <table
            class="data-table"
            aria-label="MIB Objects Table"
          >
            <thead>
              <tr>
                <th>OID</th>
                <th>Instance</th>
                <th>Alias</th>
                <th>Type</th>
                <th>Action</th>
              </tr>
            </thead>
            <TransitionGroup
              name="data-table"
              tag="tbody"
            >
              <tr
                v-for="(mibObject, index) in tableRecords"
                :key="index"
              >
                <td>{{ mibObject.oid }}</td>
                <td>{{ mibObject.instance }}</td>
                <td>{{ mibObject.alias }}</td>
                <td>{{ mibObject.type }}</td>
                <td>
                  <div class="action-container">
                    <FeatherButton
                      icon="Edit MIB Object"
                      data-test="edit-mib-object-button"
                      @click="openMibObjectDrawer(index, mibObject, CreateEditMode.Edit)"
                    >
                      <FeatherIcon :icon="Edit"> </FeatherIcon>
                    </FeatherButton>
                    <FeatherButton
                      icon="Delete MIB Object"
                      data-test="delete-mib-object-button"
                      @click="deleteMibObject(index)"
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
            v-if="tableRecords.length"
          >
            <FeatherPagination
              :modelValue="page"
              :pageSize="pageSize"
              :total="total"
              :pageSizes="[5, 10, 15, 20]"
              @update:modelValue="onPageChange"
              @update:pageSize="onPageSizeChange"
              data-test="FeatherPagination"
            />
          </div>
          <div v-if="!tableRecords.length">
            <EmptyList :content="{ msg: 'No MIB Objects added yet.' }" />
          </div>
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
          <FeatherInput
            label="OID"
            v-model.trim="oid"
            data-test="mib-object-oid-input"
            :error="mibObjectErrors.oid"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherSelect
            label="Instance"
            data-test="mib-object-instance-input"
            :options="instancesOptions"
            v-model="instance"
            :error="mibObjectErrors.instance"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherInput
            label="Alias"
            v-model.trim="alias"
            data-test="mib-object-alias-input"
            :error="mibObjectErrors.alias"
          />
          <div class="spacer"></div>
          <div class="spacer"></div>
          <FeatherSelect
            label="Data Type"
            data-test="mib-object-data-type-input"
            :options="MIB_OBJECT_DATA_TYPE_OPTIONS"
            v-model="dataType"
            :error="mibObjectErrors.type"
          >
            <FeatherIcon :icon="MoreVert" />
          </FeatherSelect>
        </div>
        <div class="spacer"></div>
        <div class="footer">
          <FeatherButton
            data-test="cancel-mib-object-button"
            @click="closeMibObjectDrawer"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            data-test="save-mib-object-button"
            @click="saveMibObject"
            :disabled="isMibObjectSaveDisabled"
          >
            Save MIB Object
          </FeatherButton>
        </div>
      </div>
      <div
        class="footer"
        v-if="!mibObjectDrawerState.visible"
      >
        <FeatherButton
          data-test="cancel-mib-group"
          @click="closeMibGroupDrawer"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          data-test="save-mib-group"
          :disabled="isSaveDisabled"
          @click="saveMibGroup"
        >
          Save MIB Group
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { computed, ref, watch, watchEffect } from 'vue'

import EmptyList from '@/components/Common/EmptyList.vue'
import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_IF_TYPE_FILTER, DEFAULT_MIB_OBJ_TYPE, DEFAULT_STATUS, IF_TYPE_FILTERS_OPTIONS, MIB_OBJECT_DATA_TYPE_OPTIONS, OID_PATTERN } from '@/lib/constants'
import { mapSnmpDataCollectionMibGroupPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createMibGroup, updateMibGroup } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { MibGroupErrors, MibGroupObjectForm, MibGroupObjectFormErrors } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Delete'
import Edit from '@featherds/icon/action/Edit'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherPagination } from '@featherds/pagination'
import { FeatherSelect, ISelectItemType } from '@featherds/select'
import { SwitchRender } from '@featherds/switch'

const store = useSnmpDataCollectionDetailStore()
const name = ref('')
const ifType = ref<ISelectItemType>(DEFAULT_IF_TYPE_FILTER)
const mibObjects = ref<MibGroupObjectForm[]>([])
const status = ref(DEFAULT_STATUS)
const errors = ref<MibGroupErrors>({})
const isSaveDisabled = ref(true)
const snackbar = useSnackbar()
const oid = ref('')
const dataType = ref<ISelectItemType>(DEFAULT_MIB_OBJ_TYPE)
const instance = ref<ISelectItemType>({ _text: '0', _value: '0' })
const instancesOptions = ref<ISelectItemType[]>([])
const alias = ref('')
const mibObjectErrors = ref<MibGroupObjectFormErrors>({})
const isMibObjectSaveDisabled = ref(true)
const page = ref(1)
const pageSize = ref(5)
const total = ref(0)
const tableRecords = ref<MibGroupObjectForm[]>([])
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

const onPageChange = (newPage: number) => {
  page.value = newPage
  tableRecords.value = mibObjects.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const onPageSizeChange = (newPageSize: number) => {
  pageSize.value = newPageSize
  page.value = 1
  tableRecords.value = mibObjects.value.slice(0, pageSize.value)
}

const onChangeStatus = () => {
  status.value = !status.value
}

const closeMibGroupDrawer = async () => {
  oid.value = ''
  dataType.value = DEFAULT_MIB_OBJ_TYPE
  instance.value = { _text: '0', _value: '0' }
  alias.value = ''
  name.value = ''
  ifType.value = DEFAULT_IF_TYPE_FILTER
  status.value = true
  mibObjects.value = []
  errors.value = {}
  page.value = 1
  pageSize.value = 5
  total.value = 0
  tableRecords.value = []
  isSaveDisabled.value = true
  isMibObjectSaveDisabled.value = true
  closeMibObjectDrawer()
  await store.closeMibGroupDrawer()
}

const deleteMibObject = (index: number) => {
  const actualIndex = (page.value - 1) * pageSize.value + index
  mibObjects.value.splice(actualIndex, 1)
  total.value = mibObjects.value.length

  // If current page is now empty and we're not on the first page, go back one page
  const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value))
  if (page.value > maxPage) {
    page.value = maxPage
  }

  tableRecords.value = mibObjects.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const openMibObjectDrawer = (index: number, mibObject: MibGroupObjectForm | null, isEditMode: CreateEditMode) => {
  const actualIndex = isEditMode === CreateEditMode.Edit ? (page.value - 1) * pageSize.value + index : index
  mibObjectDrawerState.value = {
    visible: true,
    isEditMode: isEditMode,
    mibObjectIndex: actualIndex,
    mibObject: mibObject
  }
}

const closeMibObjectDrawer = () => {
  mibObjectErrors.value = {}
  oid.value = ''
  dataType.value = DEFAULT_MIB_OBJ_TYPE
  instance.value = { _text: '0', _value: '0' }
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
    const mibObject: MibGroupObjectForm = {
      oid: oid.value,
      alias: alias.value,
      instance: instance.value._value as string,
      type: dataType.value._value as string,
      maxval: null,
      minval: null
    }
    if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Create) {
      mibObjects.value.push(mibObject)
    } else if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Edit) {
      const index = mibObjectDrawerState.value.mibObjectIndex
      if (index !== -1) {
        mibObjects.value[index] = mibObject
      }
    }
    total.value = mibObjects.value.length
    tableRecords.value = mibObjects.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
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
      ifType.value._value as string,
      store.selectedMibGroup?.mibGroupNames || [],
      mibObjects.value,
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

const validateMibObject = () => {
  const validationErrors: MibGroupObjectFormErrors = {}

  if (!oid.value) {
    validationErrors.oid = 'OID is required'
  }
  if (!OID_PATTERN.test(oid.value)) {
    validationErrors.oid = 'OID must be in the format of numbers separated by dots (e.g. 1.2.3.4)'
  }
  if (!instance.value._value) {
    validationErrors.instance = 'Instance is required'
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
  if (!ifType.value._value) {
    validationErrors.ifType = 'Interface Type is required.'
  }
  return validationErrors
}

const loadInitialData = () => {
  instancesOptions.value = store.resourceTypeNames.map(name => ({ _text: name, _value: name }))
  if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Create) {
    name.value = ''
    ifType.value = DEFAULT_IF_TYPE_FILTER
    status.value = true
    mibObjects.value = []
  }
  if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Edit) {
    const group = store.selectedMibGroup
    if (group) {
      name.value = group.name
      ifType.value = { _text: group.ifType, _value: group.ifType }
      status.value = group.enabled
      mibObjects.value = JSON.parse(group.mibObjects) || []
    }
  }
  total.value = mibObjects.value.length
  tableRecords.value = mibObjects.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value)
}

const loadMibObjectData = () => {
  if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Create) {
    oid.value = ''
    dataType.value = DEFAULT_MIB_OBJ_TYPE
    instance.value = { _text: '0', _value: '0' }
    alias.value = ''
  }

  if (mibObjectDrawerState.value.isEditMode === CreateEditMode.Edit) {
    const mibObject = mibObjectDrawerState.value.mibObject
    if (mibObject) {
      oid.value = mibObject.oid
      dataType.value = { _text: mibObject.type, _value: mibObject.type }
      instance.value = { _text: mibObject.instance, _value: mibObject.instance }
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
      instance.value = { _text: '0', _value: '0' }
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
      instance.value = { _text: '0', _value: '0' }
      alias.value = ''
      name.value = ''
      ifType.value = DEFAULT_IF_TYPE_FILTER
      status.value = true
      mibObjects.value = []
      errors.value = {}
      page.value = 1
      pageSize.value = 5
      total.value = 0
      tableRecords.value = []
      isSaveDisabled.value = true
      isMibObjectSaveDisabled.value = true
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
        @include headline4;
      }
    }
  }

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
      @include headline4;
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
