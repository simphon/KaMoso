# plotNetworkResult.R
# Daniel Duran, SFB 732 (A2), Universität Stuttgart

plotNetworkResult <- function(inDir, confFile, case, plot_with_title=TRUE, plot_with_Aratio=TRUE) {

  if( ! file.exists(confFile) ){
    warning(gettextf("[plotNetworkResult] not found: %s", confFile))
    return()
  }

  conf <-  parseConfiguration(confFile)

  allFiles <- dir(inDir)

  suppressMessages( edges <- read_csv(gettextf("%s%s", kamoso$base.path, conf$net.file)) )

  zipFiles.pat <- gettextf('%s[0-9]+\\.zip', conf$out.prefix)
  zipFiles     <- unlist(str_match_all(allFiles, zipFiles.pat))

  if(plot_with_Aratio) {
    epochsFile <- gettextf('%s/%s%s', inDir, conf$out.prefix, kamoso$epochs.file)
    suppressMessages(epochs.tbl <- read_csv(epochsFile))
  }

  for(af in zipFiles)
  {
    zipFile <- gettextf("%s/%s", inDir, af)
    suppressMessages( agents <- read_csv(unz(zipFile, kamoso$agent.file)) )
    epoch <- agents[1,]$epoch
    
    # ...............................................................
    # plot network with A-ratio:
    if(plot_with_title){
      if(conf$out.plot.title=='') {
        title <- gettextf("Epoch %d", epoch)
      } else {
        title <- gettextf("%s; Epoch %d", conf$out.plot.title, epoch)
      }
    } else {
      title <- ''
    }
    if(plot_with_Aratio) {
      epochStats <- epochs.tbl[epochs.tbl$epoch==epoch,]
      if(nrow(epochStats)>0){
        aratio <- (epochStats$productionsA / (epochStats$productionsA+epochStats$productionsB)) * 100
        title <- gettextf("%s\n%.0f%% A productions", title, aratio)
      } else {
        # this is probably epoch 1000 for which there is no production data
        title <- gettextf("%s\n ", title)
      }
    }

    suppressMessages( net.g <- plotNetwork(agents, edges, title) )
    outFile <- gettextf("%s/%s%s_netAratio%04d.pdf", inDir, conf$out.prefix, case, epoch)
    suppressMessages( ggsave(outFile, width=18.6, height=13.5, units="cm") )
    cat(gettextf("[plotNetworkResult] Saved network plot to file: %s\n", outFile))
  }
}
