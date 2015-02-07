# Options
inputFile <- 'stdin'
inputColumn <- '${inputColumn}'
firstIndex <- ${firstIndex?c}
lastIndex <- ${lastIndex?c}
polynomialOrder <- ${polynomialOrder?c}

# Read the data from stdin
data <- read.csv(file=inputFile)

x <- data[,'Timestamp'][firstIndex:lastIndex]
y <- data[,inputColumn][firstIndex:lastIndex]

# Least-square fit
C <- lm(y ~ poly(x, polynomialOrder, raw=TRUE))

# Send the results to stdout
write.csv(coefficients(C))
