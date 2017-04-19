#
#
# plot agent network
#
# clear workspace
rm(list = ls())

library(tidyverse)
library(stringr)
library(ggplot2)

codePath   <- '/path/to/your/code/here/'

# -------------------------------------------------------------------
setwd(codePath)

# -------------------------------------------------------------------

source('functions/parseConfiguration.R')
source('functions/plotAgentMap.R')

# -------------------------------------------------------------------

gridCols <- 40
gridRows <- 10

# -------------------------------------------------------------------

prefixes <- c('par-dist', 'par-reg', 'par-stat', 'sw-dist', 'sw-reg', 'sw-stat', 'reg-dist', 'reg-reg', 'reg-stat')

titles <- c('Parochial network topology (closeness interaction)',
            'Parochial network topology (regular interaction)',
            'Parochial network topology (status interaction)',
            'Small-world network topology (closeness interaction)',
            'Small-world network topology (regular interaction)',
            'Small-world network topology (status interaction)',
            'Regular network topology (closeness interaction)',
            'Regular network topology (regular interaction)',
            'Regular network topology (status interaction)')

agentEpochs <- c(0, 250, 500, 750, 1000)
inputDirs <- c('/path/to/kamoso/output/directory/')

# ------------------------------------------------------------------
# dd <- 1
for(dd in 1:length(inputDirs))
{
  inputBaseDir <- inputDirs[dd]
  outputDir    <- inputBaseDir
  # x <- 1
  for(x in 1:length(prefixes))
  {
    confFile  <- gettextf('%s%s_config.prop', inputBaseDir, prefixes[x] )
    
    for(ep in 1:length(agentEpochs)){
      
      agentFile <- gettextf('%s%s_agents%05d.csv', inputBaseDir, prefixes[x], agentEpochs[ep] )
      
      plotAgentMap(agentFile, confFile, gridCols = gridCols, gridRows = gridRows, 
                   outDir = outputDir,
                   titleText = titles[x],
                   outFilePrefix = gettextf('%s-', prefixes[x]),
                   epochTag = agentEpochs[ep] )
    }
  }
}