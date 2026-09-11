<template>
  <TogglePanel
    :collapsed="collapsed"
    class="categories-help-panel"
    data-test="categories-help-panel"
    @update:collapsed="(value: boolean) => (collapsed = value)"
  >
    <template #header>
      <span class="panel-header">
        <OnmsIcon :icon="Help" class="help-icon" />
        About Surveillance Categories
      </span>
    </template>
    <div class="help-columns">
      <div class="help-section">
        <div class="section-title">What surveillance categories are for</div>
        <p>
          A surveillance category is a named grouping of nodes used across
          OpenNMS for filtering, dashboards, notifications, and the surveillance
          view. A node can belong to any number of categories, and group
          authorizations can restrict which categories a user group sees.
        </p>
      </div>
      <div class="help-section">
        <div class="section-title">How to use this page</div>
        <p>
          <strong>Add New Category</strong> creates a category with an optional
          description; <strong>Edit</strong> changes the description (the name is
          the identifier and cannot be changed). <strong>Manage Nodes</strong>
          assigns and unassigns nodes. <strong>Delete</strong> removes the
          category — filters or authorizations that reference it by name stop
          matching, so review them afterward. The same operations are available
          to tooling through the <code>/rest/categories</code> REST API.
        </p>
      </div>
    </div>
  </TogglePanel>
</template>

<script setup lang="ts">
import { OnmsIcon } from '@opennms/onms-ui'
import { ref } from 'vue'

import Help from '@/components/icons/action/Help.vue'
import TogglePanel from '@/components/Common/TogglePanel.vue'

const collapsed = ref(true)
</script>

<style lang="scss" scoped>
.panel-header {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;

  .help-icon {
    color: var(--p-primary-color);
  }
}

.help-columns {
  display: flex;
  gap: 2.5rem;
  flex-wrap: wrap;

  .help-section {
    flex: 1;
    min-width: 320px;
  }
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
}

p {
  margin: 0 0 0.75rem 0;
  font-size: 0.9rem;
  line-height: 1.5;
}
</style>
