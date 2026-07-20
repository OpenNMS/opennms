<template>
  <div class="attribute-container" id="scv-attribute">
    <FormField
      class="input"
      data-test="attr-key"
      label="key"
      :for="keyId"
      :error="keyError"
      v-slot="{ errorId, invalid }"
    >
      <PInputText
        ref="keyRef"
        :id="keyId"
        :modelValue="attributeKey"
        @update:modelValue="updateAttributeKey"
        :invalid="invalid"
        :aria-describedby="errorId"
      />
    </FormField>
    <FormField
      class="input"
      data-test="attr-value"
      label="value"
      :for="valueId"
    >
      <PInputText
        :id="valueId"
        :modelValue="attributeValue"
        @update:modelValue="updateAttributeValue"
      />
    </FormField>

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
import Button from 'primevue/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Remove'
import FormField from '@/components/Common/FormField.vue'
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

// Unique ids per attribute row so labels, inputs and error messages stay
// associated when multiple SCVAttribute rows render together.
const keyId = computed(() => `scv-attr-key-${props.attributeIndex}`)
const valueId = computed(() => `scv-attr-value-${props.attributeIndex}`)

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
  // vertical spacing above the attribute row
  margin-top: 2rem;

  .input {
    width: 50%;
  }
}
</style>
