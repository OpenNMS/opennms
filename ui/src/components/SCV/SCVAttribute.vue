<template>
  <div class="attribute-container">
    <FeatherInput
      ref="keyRef"
      label="key"
      @update:modelValue="updateAttributeKey"
      :modelValue="attributeKey"
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

defineProps({
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
const updateAttributeKey = (key: string) => console.log(key)
const updateAttributeValue = (value: string) => console.log(value)

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
