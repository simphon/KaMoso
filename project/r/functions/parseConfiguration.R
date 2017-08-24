# parseConfiguration.R
# Daniel Duran, SFB 732 (A2), Universität Stuttgart

parseConfiguration <- function (confFile) {

  conf <- tibble(timestamp=character(),
                 soc.interaction=character(),
                 net.type=character(),
                 net.rows=integer(),
                 net.cols=integer(),
                 net.file=character(),
                 x.phon.dim=integer(),
                 out.prefix=character(),
                 out.plot.title=character(),
                 x.alpha=numeric(),
                 x.beta=numeric(),
                 x.gamma=numeric(),
                 x.sel.tag=character()
                 )

  # using suppressMessages to ignore output from readr
  suppressMessages(cLines <- readLines(confFile))

  rx <- nrow(conf) + 1

  for(l in cLines) {
    if( substring(l,1,1)!='#' ){
      keyVal <- unlist(str_split(l, '='))
      switch(keyVal[1],
             timestamp = conf[rx,]$timestamp <- keyVal[2],
             soc.interaction = conf[rx,]$soc.interaction <- keyVal[2],
             net.type = conf[rx,]$net.type  <- keyVal[2],
             net.rows = conf[rx,]$net.rows <- as.integer(keyVal[2]),
             net.cols = conf[rx,]$net.cols <- as.integer(keyVal[2]),
             net.file = conf[rx,]$net.file  <- keyVal[2],
             x.phon.dim = conf[rx,]$x.phon.dim <- as.integer(keyVal[2]),
             out.prefix = conf[rx,]$out.prefix <- keyVal[2],
             out.plot.title = conf[rx,]$out.plot.title <- keyVal[2],
             x.alpha = conf[rx,]$x.alpha <- as.numeric(keyVal[2]),
             x.beta  = conf[rx,]$x.beta  <-  as.numeric(keyVal[2]),
             x.gamma = conf[rx,]$x.gamma <-  as.numeric(keyVal[2]),
             x.sel.tag = conf[rx,]$x.sel.tag <- keyVal[2]
      )
    }
  }
  return(conf)
}
