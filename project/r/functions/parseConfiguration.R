
parseConfiguration <- function (confFile) {

  conf <- tibble(Timestamp='',
                 SocInteraction='',
                 NetType='',
                 GridRows=0, GridCols=0 )

  cLines <- readLines(confFile)

  for(l in cLines) {
    if( substring(l,1,1)!='#' ){
      keyVal <- unlist(str_split(l, '='))
      # switch(keyVal[1],
      #        timestamp = conf[1,"Timestamp"] <- keyVal[2],
      #        soc.interaction = conf[1,"SocInteraction"] <- keyVal[2])
      switch(keyVal[1],
             timestamp = conf$Timestamp <- keyVal[2],
             soc.interaction = conf$SocInteraction <- keyVal[2],
             net.type = conf$NetType <- keyVal[2],
             net.rows = conf$GridRows <- as.integer(keyVal[2]),
             net.cols = conf$GridCols <- as.integer(keyVal[2])
             )
    }
  }
  return(conf)
}