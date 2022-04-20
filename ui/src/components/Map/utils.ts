const numericSeverityLevel = (severity: string | undefined) => {
  if (severity) {
    switch (severity.toUpperCase()) {
      case 'NORMAL':
        return 11
      case 'WARNING':
        return 22
      case 'MINOR':
        return 33
      case 'MAJOR':
        return 44
      case 'CRITICAL':
        return 55
      default:
        return 0
    }
  }
  return 0
}

export { numericSeverityLevel }
