<template>
  <TableCard
    class="mib-group-form-card"
    v-if="store.mibGroupDrawerState.isEditMode !== CreateEditMode.None"
  >
    <div class="header">
      <div class="title-container">
        <h3 class="title">
          {{ store.mibGroupDrawerState.isEditMode === CreateEditMode.Edit ? 'Update MIB Group'
            : 'Create MIB Group' }}
        </h3>
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
        @click="handleCancel"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="save-mib-group"
        @click="saveMibGroup"
        :disabled="isSaveDisabled"
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
import { DEFAULT_IF_TYPE_FILTER, IF_TYPE_FILTERS_OPTIONS, STATUS_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionMibGroupPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { CreateEditMode } from '@/types'
import { MibGroupErrors, MibGroupObjectForm, SnmpCollectionMibGroupPayload } from '@/types/snmpDataCollection'
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

const store = useSnmpDataCollectionCreationStore()
const name = ref('')
const ifType = ref<ISelectItemType>(DEFAULT_IF_TYPE_FILTER)
const mibObjects = ref<MibGroupObjectForm[]>([])
const status = ref(true)
const errors = ref<MibGroupErrors>({})
const isSaveDisabled = ref(true)
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

const deleteMibObject = (index: number) => {
  mibObjects.value.splice(index, 1)
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

const saveMibGroup = () => {
  if (Object.keys(errors.value).length > 0) {
    return
  }

  const payload = mapSnmpDataCollectionMibGroupPayloadToServer(
    name.value,
    ifType.value._value as string,
    [],
    mibObjects.value,
    status.value,
    0,
    CreateEditMode.Create
  )

  if (store.mibGroupDrawerState.isEditMode === CreateEditMode.Edit) {
    store.configForm.mibGroup[store.mibGroupDrawerState.mibGroupIndex] = payload
  } else {
    store.configForm.mibGroup.push(payload)
  }
  handleCancel()
}

const handleCancel = () => {
  name.value = ''
  ifType.value = DEFAULT_IF_TYPE_FILTER
  mibObjects.value = []
  status.value = true
  store.mibGroupDrawerState = {
    visible: false,
    isEditMode: CreateEditMode.None,
    mibGroupIndex: -1
  }
}

const loadMibGroup = (mibGroup: SnmpCollectionMibGroupPayload | null) => {
  if (mibGroup === null) {
    name.value = ''
    ifType.value = DEFAULT_IF_TYPE_FILTER
    mibObjects.value = []
    status.value = true
  }
  if (mibGroup !== null) {
    const group = store.configForm.mibGroup[store.mibGroupDrawerState.mibGroupIndex] || null
    if (group) {
      name.value = group.name
      ifType.value = IF_TYPE_FILTERS_OPTIONS.find(option => option._value === group.ifType) || DEFAULT_IF_TYPE_FILTER
      mibObjects.value = JSON.parse(group.mibObjects).map((obj: any) => ({
        oid: obj.oid,
        instance: obj.instance,
        alias: obj.alias,
        type: obj.type
      }))
      status.value = group.enabled
    }
  }
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

watchEffect(() => {
  errors.value = validateMibGroup()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => store.mibGroupDrawerState.visible,
  (newVal) => {
    if (newVal) {
      const mibGroup = store.configForm.mibGroup[store.mibGroupDrawerState.mibGroupIndex] || null
      loadMibGroup(mibGroup)
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

.mib-group-form-card {
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

