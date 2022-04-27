<template>
  <div class="attribute-container">
    <FeatherInput
      ref="keyRef"
      label="key"
      @update:modelValue="updateAttributeKey"
      :modelValue="attributeKey"
      :error="keyError"
      class="input"
    />
    <FeatherInput
      label="value"
      @update:modelValue="updateAttributeValue"
      :modelValue="attributeValue"
      class="input"
    />

    <FeatherButton icon="Remove attribute">
      <FeatherIcon :icon="Delete" />
    </FeatherButton>
  </div>
</template>

<script setup lang="ts">
import { FeatherInput } from '@featherds/input'
import { FeatherButton } from '@featherds/button'
import { FeatherIcon } from '@featherds/icon'
import Delete from '@featherds/icon/action/Remove'
import { useStore } from 'vuex'
import { SCVCredentials } from '@/types/scv'

const store = useStore()

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
const credentials = computed<SCVCredentials>(() => store.state.scvModule.credentials)

const isDuplicateKey = (key: string) => {
  // check to see if the key already exists in another prop
  const entries = Object.entries(credentials.value.attributes)
  for (const [index, [attributeKey]] of entries.entries()) {
    if (key === attributeKey && index !== props.attributeIndex) {
      keyError.value = 'Duplicate keys not allowed.'
      return true
    }
  }

  // if not, clear errors
  keyError.value = null
  return false
}

const updateAttributeKey = (key: string) => {
  if (!isDuplicateKey(key)) {
    store.dispatch('scvModule/updateAttribute', { key: props.attributeKey, keyVal: { key, value: props.attributeValue} })
  }
}

const updateAttributeValue = (value: string) => store.dispatch('scvModule/updateAttribute', { key: props.attributeKey, keyVal: { key: props.attributeKey, value }})
onMounted(() => keyRef.value.focus())
</script>

<style lang="scss" scoped>
.attribute-container {
  display: flex;
  margin-top: 30px;
  gap: 10px;
  .input {
    width: 50%;
  }
}
</style>
