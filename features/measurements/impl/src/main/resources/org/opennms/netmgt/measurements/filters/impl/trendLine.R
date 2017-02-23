# Options
inputFile <- 'stdin'
inputColumn <- '${inputColumn?js_string}'
firstIndex <- ${firstIndex?c}
lastIndex <- ${lastIndex?c}
polynomialOrder <- ${polynomialOrder?c}
stepInMs <- ${stepInMs?c}
numStepsAhead <- ${numStepsAhead?c}

# Read the data from stdin
data <- read.csv(file=inputFile)

x <- data[,'timestamp'][firstIndex:lastIndex]
y <- data[,inputColumn][firstIndex:lastIndex]

# Least-square fit
fit <- lm(y ~ poly(x, polynomialOrder))

# Apply
xx <- seq(data[,'timestamp'][firstIndex], data[,'timestamp'][lastIndex] + (numStepsAhead * stepInMs), by=stepInMs)
write.csv(predict(fit, data.frame(x=xx)))
