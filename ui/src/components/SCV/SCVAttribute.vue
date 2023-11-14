<template>
  <div class="attribute-container" id="scv-attribute">
    <FeatherInput
      data-test="attr-key"
      ref="keyRef"
      label="key"
      @update:modelValue="updateAttributeKey"
      :modelValue="attributeKey"
      :error="keyError"
      class="input"
    />
    <FeatherInput
      data-test="attr-value"
      label="value"
      @update:modelValue="updateAttributeValue"
      :modelValue="attributeValue"
      class="input"
    />

    <FeatherButton icon="Remove attribute" @click="removeAttribute" data-test="rm-attr-btn">
      <FeatherIcon :icon="Delete" />
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Remove'
import { useScvStore } from '@/stores/scvStore'
import { SCVCredentials } from '@/types/scv'
import { UpdateModelFunction } from '@/types'

const scvStore = useScvStore()
const emit = defineEmits(['set-key-error'])

const props = defineProps({
  attributeKey: {
    type: String,
    required: true
  },
  attributeValue: {
    type: String,
    required: true
  },
  attributeIndex: ({
    type: Number,
    required: true
  })
})

const keyRef = ref()
const keyError = ref()
const credentials = computed<SCVCredentials>(() => scvStore.credentials)

const isDuplicateKey = (key: string) => {
  // check to see if the key already exists in another prop
  const entries = Object.entries(credentials.value.attributes)

  for (const [index, [attributeKey]] of entries.entries()) {
    if (key === attributeKey && index !== props.attributeIndex) {
      keyError.value = 'Duplicate keys not allowed.'
      emit('set-key-error', true)
      return true
    }
  }

  // if not, clear errors
  keyError.value = null
  emit('set-key-error', false)
  return false
}

const updateAttributeKey: UpdateModelFunction = (key: string) => {
  if (!isDuplicateKey(key)) {
    scvStore.updateAttribute({ key: props.attributeKey, keyVal: { key, value: props.attributeValue} })
  }
}

const updateAttributeValue: UpdateModelFunction = (value: string) =>
  scvStore.updateAttribute({ key: props.attributeKey, keyVal: { key: props.attributeKey, value }})

const removeAttribute = () => scvStore.removeAttribute(props.attributeKey)

onMounted(() => keyRef.value.focus())
</script>

<style lang="scss" scoped>
.attribute-container {
  display: flex;
  gap: 10px;
  .input {
    width: 50%;
  }
}
</style>
