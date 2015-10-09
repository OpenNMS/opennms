# Options
inputFile <- 'stdin'
inputColumn <- '${inputColumn?js_string}'
firstIndex <- ${firstIndex?c}
lastIndex <- ${lastIndex?c}
polynomialOrder <- ${polynomialOrder?c}

# Read the data from stdin
data <- read.csv(file=inputFile)

x <- data[,'timestamp'][firstIndex:lastIndex]
y <- data[,inputColumn][firstIndex:lastIndex]

# Least-square fit
C <- lm(y ~ poly(x, polynomialOrder))

# Send the results to stdout
write.csv(coefficients(C))
