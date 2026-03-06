<template>
  <FeatherDrawer
    id="drawer"
    data-test="resource-type-parameter-drawer"
    v-model="isDrawerOpen"
    :labels="{ close: 'close', title: drawerTitle }"
    hide-close
    width="40rem"
    class="resource-type-parameter-drawer"
  >
    <div class="container">
      <div class="drawer-header">
        <h2>{{ drawerTitle }}</h2>
      </div>
      <div class="spacer"></div>
      <div class="drawer-content">
        <FeatherInput
          label="Key"
          v-model.trim="key"
          data-test="resource-type-parameter-key-input"
          :error="errors.key"
        />
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FeatherInput
          label="Value"
          v-model.trim="value"
          data-test="resource-type-parameter-value-input"
          :error="errors.value"
        />
      </div>
      <div class="spacer"></div>
      <div class="drawer-footer">
        <FeatherButton
          data-test="cancel-resource-type-parameter-button"
          @click="cancel"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          data-test="save-resource-type-parameter-button"
          @click="saveResourceTypeParameter"
          :disabled="isSaveDisabled"
        >
          Save
        </FeatherButton>
      </div>
    </div>
  </FeatherDrawer>
</template>

<script lang="ts" setup>
import { KEY_PATTERN } from '@/lib/constants'
import { CreateEditMode } from '@/types'
import { PersistSelectorStrategyForm, StorageStrategyForm } from '@/types/snmpDataCollection'
import { FeatherButton } from '@featherds/button'
import { FeatherDrawer } from '@featherds/drawer'
import { FeatherInput } from '@featherds/input'

const props = defineProps<{
  state: {
    type: 'storageStrategy' | 'persistenceSelectorStrategy' | null
    visible: boolean
    isEditMode: CreateEditMode
    persistenceSelectorStrategyIndex: number
    storageStrategyIndex: number
    persistenceSelectorStrategyObject: PersistSelectorStrategyForm | null
    storageStrategyObject: StorageStrategyForm | null
  }
}>()

const emit = defineEmits<{
  (e: 'cancel'): void
  (e: 'save', type: 'storageStrategy' | 'persistenceSelectorStrategy', key: string, value: string): void
}>()

const key = ref('')
const value = ref('')
const isDrawerOpen = ref(props.state.visible)
const errors = ref<{ key?: string, value?: string }>({})
const isSaveDisabled = ref(true)
const drawerTitle = computed(() => {
  if (props.state.type === 'storageStrategy') {
    return props.state.isEditMode === CreateEditMode.Create ? 'Create Storage Strategy Parameter' : 'Edit Storage Strategy Parameter'
  }
  if (props.state.type === 'persistenceSelectorStrategy') {
    return props.state.isEditMode === CreateEditMode.Create ? 'Create Persistence Selector Strategy Parameter' : 'Edit Persistence Selector Strategy Parameter'
  }
  return 'Parameter'
})

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

const loadResourceTypeParameterData = () => {
  if (props.state.type === 'storageStrategy' && props.state.storageStrategyObject) {
    const parameter = props.state.storageStrategyObject
    if (parameter) {
      key.value = parameter.key
      value.value = parameter.value
    }
  } else if (props.state.type === 'persistenceSelectorStrategy' && props.state.persistenceSelectorStrategyObject) {
    const parameter = props.state.persistenceSelectorStrategyObject
    if (parameter) {
      key.value = parameter.key
      value.value = parameter.value
    }
  }
}

const saveResourceTypeParameter = () => {
  if (props.state.type === 'storageStrategy') {
    emit('save', 'storageStrategy', key.value, value.value)
  }
  if (props.state.type === 'persistenceSelectorStrategy') {
    emit('save', 'persistenceSelectorStrategy', key.value, value.value)
  }
}

const cancel = () => {
  errors.value = {}
  key.value = ''
  value.value = ''
  isSaveDisabled.value = true
  emit('cancel')
}

watchEffect(() => {
  errors.value = validateResourceTypeParameter()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => props.state.visible,
  (visible) => {
    isDrawerOpen.value = visible
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
</script>

<style scoped lang="scss">
.resource-type-parameter-drawer {
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

