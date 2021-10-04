<template>
  <FeatherButton
    :icon="icon"
    :primary="primary"
    :secondary="secondary"
    :text="text"
    :disabled="disabled"
    :asAnchor="asAnchor"
  >
    <slot></slot>
  </FeatherButton>
</template> 

<script setup lang="ts">
import { FeatherButton } from '@featherds/button'

defineProps({
  primary: {
    type: Boolean
  },
  secondary: {
    type: Boolean
  },
  icon: {
    type: String,
    required: false
  },
  text: {
    type: Boolean
  },
  disabled: {
    type: Boolean
  },
  asAnchor: {
    type: Boolean
  }
})
</script> 
