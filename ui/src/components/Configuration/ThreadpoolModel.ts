import useVuelidate from '@vuelidate/core'
import { required, numeric, minValue, maxValue, helpers } from '@vuelidate/validators'
import { ref } from 'vue'

export default class threadpoolModel {
  importThreads = ref(0)
  scanThreads = ref(0)
  rescanThreads = ref(0)
  writeThreads = ref(0)

  get rules() {
    return {
      importThreads: {
        required: helpers.withMessage('Input box can not be blank', required),
        numeric: helpers.withMessage('Value can only be numeric', numeric),
        minValue: helpers.withMessage('Min value should be 0', minValue(0)),
        maxValue: helpers.withMessage('Max value should be 10', maxValue(10))
      },
      scanThreads: {
        required: helpers.withMessage('Input box can not be blank', required),
        numeric: helpers.withMessage('Value can only be numeric', numeric),
        minValue: helpers.withMessage('Min value should be 0', minValue(0)),
        maxValue: helpers.withMessage('Max value should be 10', maxValue(10))
      },
      rescanThreads: {
        required: helpers.withMessage('Input box can not be blank', required),
        numeric: helpers.withMessage('Value can only be numeric', numeric),
        minValue: helpers.withMessage('Min value should be 0', minValue(0)),
        maxValue: helpers.withMessage('Max value should be 10', maxValue(10))
      },
      writeThreads: {
        required: helpers.withMessage('Input box can not be blank', required),
        numeric: helpers.withMessage('Value can only be numeric', numeric),
        minValue: helpers.withMessage('Min value should be 0', minValue(0)),
        maxValue: helpers.withMessage('Max value should be 10', maxValue(10))
      }
    }
  }

  toModel() {
    return useVuelidate(this.rules, this)
  }
}
