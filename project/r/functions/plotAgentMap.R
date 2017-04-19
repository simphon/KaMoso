#
#
#
plotAgentMap <- function( agentFile, confFile, gridCols=NA, gridRows=NA, 
                          outDir='', titleText='', outFilePrefix='', epochTag=0 )
{
  gridCols <- 40
  gridRows <- 10

  # -------------------------------------------------------------------

  agents <- read_csv(agentFile)

  conf <-  parseConfiguration(confFile)

  # TODO read grid cols / rows from configuration if NA

  numAgents <- nrow(agents)

  nodeIDs <- agents$nodeID
  nodeMatrix <- matrix(nodeIDs, nrow = gridRows, ncol = gridCols, byrow = TRUE)


  # -------------------------------------------------------------------
  # plot ratio A
  agentsMap  <- tibble(x=integer(numAgents), y=integer(numAgents), value=numeric(numAgents))

  for(ax in 1:numAgents) {
    agentID <- agents[ax,]$nodeID
    nodeIndex <- which(nodeMatrix==agentID, arr.ind=TRUE)
    agentsMap[ax,]$x <- nodeIndex[2]
    agentsMap[ax,]$y <- nodeIndex[1]
    ratio <- agents[ax,]$ratioA
    #if( is.na(ratio )) ratio <- 0
    agentsMap[ax,]$value <- ratio
  }

  p <- ggplot(data = agentsMap, aes(x=x, y=y, fill=value)) + 
    geom_tile() + 
    scale_fill_gradient(low = "white", high = "dodgerblue4", na.value = "wheat", limits=c(0.0,1.0)) +
    #theme_grey(base_size = 9) +
    labs(x = "", y = "", fill="% A") + 
    scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0)) +
    coord_fixed(ratio=1)

  if(length(titleText)>0) {
    p + ggtitle(titleText)
  }

  if(outDir!='') {
    ggsave(gettextf('%s%sagents%05d-ratioA.pdf', outDir, outFilePrefix, epochTag))
  }

  # -------------------------------------------------------------------
  # plot status
  agentsMap  <- tibble(x=integer(numAgents), y=integer(numAgents), value=numeric(numAgents))

  for(ax in 1:numAgents) {
    agentID <- agents[ax,]$nodeID
    nodeIndex <- which(nodeMatrix==agentID, arr.ind=TRUE)
    agentsMap[ax,]$x <- nodeIndex[2]
    agentsMap[ax,]$y <- nodeIndex[1]
    ratio <- agents[ax,]$status
    #if( is.na(ratio )) ratio <- 0
    agentsMap[ax,]$value <- ratio
  }

  p <- ggplot(data = agentsMap, aes(x=x, y=y, fill=value)) + 
    geom_tile() + 
    scale_fill_gradient(low = "white", high = "dodgerblue4", na.value = "wheat", limits=c(0.0,1.0)) +
    #theme_grey(base_size = 9) +
    labs(x = "", y = "", fill="Status") + 
    coord_fixed(ratio=1) +
    scale_x_discrete(expand = c(0, 0)) + scale_y_discrete(expand = c(0, 0))

  if(length(titleText)>0) {
    p + ggtitle(titleText)
  }

  if(outDir!='') {
    ggsave(gettextf('%s%sagents%05d-status.pdf', outDir, outFilePrefix, epochTag))
  }
}