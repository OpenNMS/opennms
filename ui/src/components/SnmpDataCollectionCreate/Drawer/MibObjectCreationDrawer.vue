<template>
  <FeatherDrawer
    id="drawer"
    data-test="mib-object-drawer"
    v-model="isDrawerOpen"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    width="40rem"
    class="mib-object-drawer"
  >
    <div class="container">
      <div class="drawer-header">
        <h2>{{ drawerTitle }}</h2>
      </div>
      <div class="spacer"></div>
      <div class="drawer-content">
        <FeatherInput
          label="OID"
          v-model.trim="oid"
          data-test="mib-object-oid-input"
          :error="errors.oid"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherSelect
          label="Instance"
          data-test="mib-object-instance-input"
          :options="instancesOptions"
          v-model="instance"
          :error="errors.instance"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherInput
          label="Alias"
          v-model.trim="alias"
          data-test="mib-object-alias-input"
          :error="errors.alias"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherSelect
          label="Data Type"
          data-test="mib-object-data-type-input"
          :options="MIB_OBJECT_DATA_TYPE_OPTIONS"
          v-model="dataType"
          :error="errors.type"
        >
          <FeatherIcon :icon="MoreVert" />
        </FeatherSelect>
      </div>
      <div class="spacer"></div>
      <div class="drawer-footer">
        <FeatherButton
          data-test="cancel-mib-object-button"
          @click="cancel"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          data-test="save-mib-object-button"
          @click="saveMibObject"
          :disabled="isSaveDisabled"
        >
          Save
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { DEFAULT_MIB_OBJ_TYPE, MIB_OBJECT_DATA_TYPE_OPTIONS, OID_PATTERN } from '@/lib/constants'
import { CreateEditMode } from '@/types'
import { MibGroupObjectForm, MibGroupObjectFormErrors } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherIcon } from '@featherds/icon'
import MoreVert from '@featherds/icon/navigation/MoreVert'
import { FeatherInput } from '@featherds/input'
import { FeatherSelect, ISelectItemType } from '@featherds/select'

const props = defineProps<{
  state: {
    visible: boolean
    isEditMode: CreateEditMode
    mibObjectIndex: number,
    mibObject: MibGroupObjectForm | null
  },
  names: string[]
}>()
const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', mibObject: MibGroupObjectForm): void
}>()

const isDrawerOpen = ref(props.state.visible)
const oid = ref('')
const dataType = ref<ISelectItemType>(DEFAULT_MIB_OBJ_TYPE)
const instance = ref<ISelectItemType>({ _text: '0', _value: '0' })
const instancesOptions = ref<ISelectItemType[]>(props.names.map((name) => ({ _text: name, _value: name })))
const alias = ref('')
const errors = ref<MibGroupObjectFormErrors>({})
const isSaveDisabled = ref(true)
const drawerTitle = computed(() =>
  props.state.isEditMode === CreateEditMode.Create
    ? 'Create Mib Object'
    : 'Edit Mib Object'
)

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

const loadMibObjectData = () => {
  if (props.state.isEditMode === CreateEditMode.Create) {
    oid.value = ''
    dataType.value = DEFAULT_MIB_OBJ_TYPE
    instance.value = { _text: '0', _value: '0' }
    alias.value = ''
  }

  if (props.state.isEditMode === CreateEditMode.Edit) {
    const mibObject = props.state.mibObject
    if (mibObject) {
      oid.value = mibObject.oid
      dataType.value = { _text: mibObject.type, _value: mibObject.type }
      instance.value = { _text: mibObject.instance, _value: mibObject.instance }
      alias.value = mibObject.alias
    }
  }
}

const saveMibObject = () => {
  const validationErrors = validateMibObject()
  errors.value = validationErrors
  if (Object.keys(validationErrors).length === 0) {
    const mibObject: MibGroupObjectForm = {
      oid: oid.value,
      alias: alias.value,
      instance: instance.value._value as string,
      type: dataType.value._value as string,
      maxval: null,
      minval: null
    }
    emit('save', mibObject)
  }
}

const cancel = () => {
  errors.value = {}
  oid.value = ''
  dataType.value = DEFAULT_MIB_OBJ_TYPE
  instance.value = { _text: '0', _value: '0' }
  alias.value = ''
  isSaveDisabled.value = true
  emit('cancel')
}

watchEffect(() => {
  errors.value = validateMibObject()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => props.state.visible,
  (visible) => {
    isDrawerOpen.value = visible
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
</script>

<style lang="scss" scoped>
.mib-object-drawer {
  .container {
    .drawer-header {
      padding: 40px 20px;
    }

    .spacer {
      min-height: 0.5em;
    }

    .drawer-content {
      padding: 0 20px;
    }

    .drawer-footer {
      padding: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}
</style>

