# plotNetwork.R
# Daniel Duran, SFB 732 (A2), Universität Stuttgart
#
# Dependencies: ggplot2, igraph
#
plotNetwork <- function(agents, edges, title) {

  edges.ig <- graph.data.frame(edges[,1:2])

  myLayout <- layout_with_kk(edges.ig, maxiter=150*vcount(edges.ig))

  as_tibble(myLayout) %>% 
    rename(x=V1, y=V2) %>%
    mutate(nodeID=0:(nrow(myLayout)-1)) %>%
    left_join(agents) -> net.tbl

  ne <- nrow(edges)
  edgeCoords <- tibble(xStart=numeric(ne), yStart=numeric(ne), xEnd=numeric(ne), yEnd=numeric(ne))

  for( ex in 1:nrow(edges)) {
    currentEdge <- edges[ex,]
    from_id <- currentEdge$from
    to_id <- currentEdge$to
    currentFrom <- filter(net.tbl, nodeID==from_id)
    currentTo <- filter(net.tbl, nodeID==to_id)

    edgeCoords[ex,'xStart']<- currentFrom$x
    edgeCoords[ex,'yStart']<- currentFrom$y
    edgeCoords[ex,'xEnd']<- currentTo$x
    edgeCoords[ex,'yEnd']<- currentTo$y
  }

  colLow  <- "#132B43"
  colHigh <- "#56B1F7"
  colGuide <- "colourbar"

  if(mean(net.tbl$ratioA, na.rm=TRUE) == 0){
    # all A-ratios are 0
    colHigh <- colLow
    colGuide <- "legend"
  } else if(mean(net.tbl$ratioA, na.rm=TRUE) == 1) {
    # all A-ratios are 1
    colLow <- colHigh
    colGuide <- "legend"
  }

  g <- ggplot() + 
    geom_segment(data=edgeCoords, mapping=aes(x=xStart, y=yStart, xend=xEnd, yend=yEnd), col='gray') +
    geom_point(data=net.tbl, aes(x=x, y=y, col=ratioA, size=status)) +
    theme_net(base_size = 18) +
    scale_colour_gradient(low = colLow, high = colHigh,
                          space = "Lab", na.value = "grey50", guide = colGuide) +
    labs(col="% A", size="Status", title=title)

  return(g)
}
