<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <FeatherInput disabled :modelValue="filename"/>
      <input
        :style="'display: none;'"
        id="raised-button-file"
        type="file"
        @model:update="uploadHandler"
      />
      <label htmlFor="raised-button-file">
        <FeatherButton
          primary>
          {{ buttonCopy }}
        </FeatherButton>
      </label>
    </div>
  </div>
</template>

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'
import { FeatherInput } from '@featherds/input'

const emit = defineEmits(['uploadHandler'])

const uploadHandler = (e: any) => {
  emit('uploadHandler', e.target.value)
}

defineProps({
  filename: {
    required: true,
    type: String
  },
  buttonCopy: {
    required: true,
    type: String
  }
})
</script>
