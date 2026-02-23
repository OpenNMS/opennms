<template>
  <TableCard class="mib-group-form-container">
    <div class="header">
      <div class="title-container">
        <h2 class="title">{{ title }}</h2>
      </div>
    </div>
    <div class="content">
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
      <div class="table-container">
        <div class="header">
          <div class="title">
            <h3>MIB Objects</h3>
          </div>
          <div class="action">
            <FeatherButton
              primary
              data-test="add-mib-object-button"
              @click="openMibObjectDrawer(-1, null, CreateEditMode.Create)"
            >
              Add MIB Object
            </FeatherButton>
          </div>
        </div>
        <table
          class="data-table"
          aria-label="Mib Objects Table"
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
              v-for="(mibObject, index) in mibObjects"
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
        <div v-if="!mibObjects.length">
          <EmptyList :content="{ msg: 'No MIB Objects added yet.' }" />
        </div>
      </div>
    </div>
    <div class="footer">
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
        Save
      </FeatherButton>
    </div>
    <MibObjectCreationDrawer
      :state="mibObjectDrawerState"
      :names="store.resourceTypeNames"
      @cancel="closeMibObjectDrawer"
      @save="saveMibObject"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import useSnackbar from '@/composables/useSnackbar'
import { DEFAULT_IF_TYPE_FILTER, IF_TYPE_FILTERS_OPTIONS, STATUS_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionMibGroupPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { MibGroupErrors, MibGroupObjectForm } from '@/types/snmpDataCollection'
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
import MibObjectCreationDrawer from './Drawer/MibObjectCreationDrawer.vue'
import { createMibGroup, updateMibGroup } from '@/services/snmpDataCollectionService'

const store = useSnmpDataCollectionDetailStore()
const name = ref('')
const ifType = ref<ISelectItemType>(DEFAULT_IF_TYPE_FILTER)
const mibObjects = ref<MibGroupObjectForm[]>([])
const status = ref(true)
const errors = ref<MibGroupErrors>({})
const isSaveDisabled = ref(true)
const snackbar = useSnackbar()
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

const title = computed(() =>
  store.mibGroupDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create Mib Group'
    : 'Edit Mib Group'
)

const openMibObjectDrawer = (index: number, mibObject: MibGroupObjectForm | null, isEditMode: CreateEditMode) => {
  mibObjectDrawerState.value = {
    visible: true,
    isEditMode: isEditMode,
    mibObjectIndex: index,
    mibObject: mibObject
  }
}

const closeMibObjectDrawer = () => {
  mibObjectDrawerState.value = {
    visible: false,
    isEditMode: CreateEditMode.None,
    mibObjectIndex: -1,
    mibObject: null
  }
}

const saveMibObject = (mibObject: MibGroupObjectForm) => {
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

const closeMibGroupDrawer = () => {
  closeMibObjectDrawer()
  store.closeMibGroupDrawer()
}

const deleteMibObject = (index: number) => {
  mibObjects.value.splice(index, 1)
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

watchEffect(() => {
  errors.value = validateMibGroup()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => store.mibGroupDrawerState.visible,
  (visible) => {
    if (visible) {
      loadInitialData()
    } else {
      // Reset form data and errors when drawer is closed
      name.value = ''
      ifType.value = DEFAULT_IF_TYPE_FILTER
      status.value = true
      mibObjects.value = []
      errors.value = {}
      isSaveDisabled.value = true
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

.mib-group-form-container {
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

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
  }
}
</style>

