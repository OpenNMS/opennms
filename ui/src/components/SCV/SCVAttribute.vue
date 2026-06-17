<template>
  <div class="attribute-container" id="scv-attribute">
    <div class="input">
      <FloatLabel data-test="attr-key">
        <PInputText
          ref="keyRef"
          id="scv-attr-key"
          :modelValue="attributeKey"
          @update:modelValue="updateAttributeKey"
          :invalid="!!keyError"
        />
        <label for="scv-attr-key">key</label>
      </FloatLabel>
      <small
        v-if="keyError"
        class="field-error"
      >{{ keyError }}</small>
    </div>
    <div class="input">
      <FloatLabel data-test="attr-value">
        <PInputText
          id="scv-attr-value"
          :modelValue="attributeValue"
          @update:modelValue="updateAttributeValue"
        />
        <label for="scv-attr-value">value</label>
      </FloatLabel>
    </div>

    <PButton
      text
      aria-label="Remove attribute"
      data-test="rm-attr-btn"
      @click="removeAttribute"
    >
      <FeatherIcon :icon="Delete" />
    </PButton>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import InputText from 'primevue/inputtext'
import FloatLabel from 'primevue/floatlabel'
import Button from 'primevue/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Remove'
import { useScvStore } from '@/stores/scvStore'
import { SCVCredentials } from '@/types/scv'
import { UpdateModelFunction } from '@/types'

const PInputText = InputText
const PButton = Button

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
    scvStore.updateAttribute({ key: props.attributeKey, keyVal: { key, value: props.attributeValue }})
  }
}

const updateAttributeValue: UpdateModelFunction = (value: string) =>
  scvStore.updateAttribute({ key: props.attributeKey, keyVal: { key: props.attributeKey, value }})

const removeAttribute = () => scvStore.removeAttribute(props.attributeKey)

onMounted(() => (keyRef.value?.$el as HTMLInputElement)?.focus())
</script>

<style lang="scss" scoped>
.attribute-container {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  // room above the inputs for the floating labels
  margin-top: 2rem;

  .input {
    width: 50%;

    :deep(.p-inputtext) {
      width: 100%;
    }
  }

  .field-error {
    color: var(--p-red-500);
  }
}
</style>
