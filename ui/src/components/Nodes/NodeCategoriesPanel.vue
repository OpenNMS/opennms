<template>
  <div class="card">
    <div class="title headline3">Surveillance Category Memberships</div>
    <OnmsIconButton
      v-if="adminRole"
      text
      aria-label="Edit"
      data-test="edit-button"
      :icon="IconEdit"
      @click="onEditClick"
    />
    <div class="onms-row" v-if="props.node?.categories?.length === 0">
      <div class="onms-col-12">
        <span class="attribute-value">This node is not a member of any categories.</span>
      </div>
    </div>
    <div class="onms-row" v-for="item in props.node?.categories" :key="item.id">
      <div class="onms-col-12">
        <span class="attribute-value">{{ item.name }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PropType } from 'vue'
import { OnmsIconButton } from '@opennms/onms-ui'
import IconEdit from '@opennms/onms-ui/icons/action/Edit.vue'
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

const onEditClick = () => {
  if (adminRole.value) {
    const editUrl = `${props.baseHref}/admin/categories.htm?edit&node=${props.node.id}`
    window.location.assign(editUrl)
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
