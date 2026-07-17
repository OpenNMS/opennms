<template>
  <div class="card">
    <div class="title headline3">Notifications</div>
    <OnmsIconButton
      text
      aria-label="Edit"
      data-test="edit-button"
      :icon="IconLink"
      @click="onLinkClick"
    />

    <div class="onms-row">
      <div class="onms-col-12">
        <span class="attribute-value">Stuff goes here.</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconLink from '@opennms/onms-ui/icons/action/Link.vue'
import useRole from '@/composables/useRole'
import { Node } from '@/types'

const props = defineProps({
  baseHref: {
    required: true,
    type: String
  },
  node: {
    required: true,
    type: Object as PropType<Node>
  }
})

const { adminRole } = useRole()

const onLinkClick = () => {
  if (adminRole.value) {
    const linkUrl = `${props.baseHref}/notifications/index.jsp`
    window.location.assign(linkUrl)
  }
}
</script>

<style lang="scss" scoped>
.card {
  background: var(--p-content-background);
  border: 1px solid var(--p-content-border-color);
  border-radius: 5px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
  padding: 15px;
  margin-bottom: 15px;

  .title {
    padding: 5px 10px 0px 10px;
  }
}
</style>
