<template>
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
</template>

<script setup lang="ts">
</script>
