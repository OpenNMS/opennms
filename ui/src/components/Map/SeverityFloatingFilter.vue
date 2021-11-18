<template>
      <div>
            &ge;
            <select
                  :style="{ width: '70px' }"
                  v-model="currentValue"
                  @change="onSelectionChanged()"
            >
                  <option v-for="option in severities" :value="option" :key="option">{{ option }}</option>
            </select>
      </div>
</template>

<script>
export default {
      data: function () {
            return {
                  severities: [
                        'Normal',
                        'Warning',
                        'Minor',
                        'Major',
                        'Critical'
                  ],
                  currentValue: ""
            };
      },
      methods: {
            onSelectionChanged() {
                  if (this.currentValue === '') {
                        // clear the filter
                        this.params.parentFilterInstance((instance) => {
                              instance.onFloatingFilterChanged(null, null);
                        });
                        return;
                  }

                  this.params.parentFilterInstance((instance) => {
                        instance.onFloatingFilterChanged('contains', this.currentValue);
                  });
            },

            onParentModelChanged(parentModel) {
                  // When the filter is empty we will receive a null value here
                  if (!parentModel) {
                        this.currentValue = '';
                  } else {
                        this.currentValue = parentModel.filter;
                  }
            }

      }
}
</script>
