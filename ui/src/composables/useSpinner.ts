const isActive = ref(false)

const useSpinner = () => {
  const startSpinner = () => (isActive.value = true)
  const stopSpinner = () => (isActive.value = false)

  return { startSpinner, stopSpinner, isActive }
}

export default useSpinner
