#!/usr/bin/env Rscript

# analyzeRuns.R
# Daniel Duran, SFB 732 (A2), Universität Stuttgart
#
# This R script analyzes the output of a series of Kamoso simulations.
#
#
# args[1] : input directory (relative to wd or absolute)
#           this should be a parent directory to one or more output directories
#           for individual runs of kamoso with one or more configurations
#
# args[2] : output directory (relative to wd or absolute)
#
# -------------------------------------------------------------------

# load required libraries
if(! file.exists('./_kamosoConfig.R')) {
  stop("Missing required R file: _kamosoConfig.R -- run R in Kamoso's r-code directory!")
} else {
  suppressMessages(library(tidyverse))
  suppressMessages(library(geomnet))
  suppressMessages(library(stringr))
  suppressMessages(library(igraph))
  suppressMessages(library(scales))# required for labels = percent_format()
  suppressMessages(library(xtable))# for exporting tables in latex format
  
  source('_kamosoConfig.R')
  # loads various variables named "kamoso.*"
  
  source('functions/parseConfiguration.R')
  source('functions/plotNetwork.R')
  source('functions/plotNetworkResult.R')
}

args = commandArgs(trailingOnly=TRUE)

inDir  <- args[1]
outDir <- args[2]

if(str_sub(inDir, -1) == '/') {
  inDir <- str_sub(inDir, start=1, end=-2)
}
if(str_sub(outDir, -1) == '/') {
  outDir <- str_sub(outDir, start=1, end=-2)
}

if( ! dir.exists(inDir) ){
  stop("Input directory not found")
}
if( ! dir.exists(outDir) ){
  stop("Output directory not found")
}

case.all.tag <- 'all'

# -------------------------------------------------------------------

# store global information table
runs.tbl <- tibble(ID=integer(),
                   socInteraction=character(),
                   netType=character(),
                   epochs=integer(),
                   finalProductionsA=integer(),
                   finalProductionsB=integer(),
                   finalAratio=numeric(),
                   dir=character(),
                   conf=character(),
                   prefix=character(),
                   case=character(),# variant competition outcome
                   sel=character()# selection weight pattern
)

# store all epochs of all simulation runs
evolution.tbl <- tibble(ID=integer(),
                        epoch=integer(),
                        productionsA=integer(),
                        productionsB=integer() )

runDirs <- dir(inDir, full.names=TRUE)

for( rd in runDirs ) {

  if( ! file.info(rd)$isdir ){
    # it's probably a file
    next()
  }

  runFiles <- dir(rd, full.names=TRUE)
  propFiles <- which(str_detect(runFiles, kamoso$prop.pat))

  if(length(propFiles)==0) {
    warning(gettextf("No conf files in %s", rd))
    next()
  }

  # go through all configuration files found in current output directory
  for(px in 1:length(propFiles))
  {
    rowX <- nrow(runs.tbl) + 1

    confFile  <- runFiles[propFiles[px]]
    if( ! file.exists(confFile) ){
      warning(gettextf("Conf file %d not found: %s in %s", px, confFile, rd))
      next()
    }

    # read configuration from file
    conf <- parseConfiguration(confFile)

    # check the output error log file
    errFile <- gettextf('%s/%s%s', rd, str_sub(conf$out.prefix, start=1, end=-2), kamoso$err)
    if( file.info(errFile)$size > 0 ) {
      warning(gettextf('Skipping because of non-zero error file: %s', errFile))
      next()
    }

    # # compute tag for exemplar selection weights pattern
    if( is.na(conf$x.sel.tag)) {
      if( conf$x.alpha>0 && conf$x.beta>0 && conf$x.gamma>0 ) {
        #sel <- 'abg'
        conf$x.sel.tag <- 'abg'
        warning('Set conf$x.sel.tag to "abg"')
      } else {
        if(conf$x.beta == 0) {
          #sel <- 'ag'
          conf$x.sel.tag <- 'ag'
          warning('Set conf$x.sel.tag to "ag"')
        } else {
          stop("Selection pattern not recognized")
        }
      }
    }

    # read epoch data from file
    epochsFile <- gettextf('%s/%s%s', rd, conf$out.prefix, kamoso$epochs.file)
    if( ! file.exists(epochsFile) ){
      warning(gettextf("Epochs file not found: %s", epochsFile))
      next()
    }

    # store information in global table
    runs.tbl[rowX,]$ID <- rowX

    runs.tbl[rowX,]$socInteraction <- conf$soc.interaction
    runs.tbl[rowX,]$netType        <- conf$net.type
    runs.tbl[rowX,]$prefix         <- conf$out.prefix

    runs.tbl[rowX,]$dir  <- rd
    runs.tbl[rowX,]$conf <- runFiles[propFiles[px]]
    runs.tbl[rowX,]$sel  <- conf$x.sel.tag

    suppressMessages(epochs.tbl <- read_csv(epochsFile))

    # get information about the variant competition outcome
    zeroA <- filter(epochs.tbl, productionsA==0)

    if(nrow(zeroA)>0) {
      # variant A was lost
      runs.tbl[rowX,]$epochs <- zeroA[1,]$epoch +1
      runs.tbl[rowX,]$finalProductionsA <- zeroA[1,]$productionsA
      runs.tbl[rowX,]$finalProductionsB <- zeroA[1,]$productionsB
      runs.tbl[rowX,]$finalAratio <- 0.0
      runs.tbl[rowX,]$case <- 'B'
    } else {
      zeroB <- filter(epochs.tbl, productionsB==0)
      if(nrow(zeroB)>0) {
        # variant B was lost
        runs.tbl[rowX,]$epochs <- zeroB[1,]$epoch +1
        runs.tbl[rowX,]$finalProductionsA <- zeroB[1,]$productionsA
        runs.tbl[rowX,]$finalProductionsB <- zeroB[1,]$productionsB
        runs.tbl[rowX,]$finalAratio <- 1.0
        runs.tbl[rowX,]$case <- 'A'
      } else {
        # no variant was lost
        runs.tbl[rowX,]$epochs <- epochs.tbl[nrow(epochs.tbl),]$epoch +1
        runs.tbl[rowX,]$finalProductionsA <- epochs.tbl[nrow(epochs.tbl),]$productionsA
        runs.tbl[rowX,]$finalProductionsB <- epochs.tbl[nrow(epochs.tbl),]$productionsB
        runs.tbl[rowX,]$finalAratio <- epochs.tbl[nrow(epochs.tbl),]$productionsA / 
          (epochs.tbl[nrow(epochs.tbl),]$productionsA + epochs.tbl[nrow(epochs.tbl),]$productionsB)
        runs.tbl[rowX,]$case <- 'AB'
      }
    }

    # store epoch information in evolutions table
    mutate(epochs.tbl, ID=rowX) %>%
      select(-timestamp) -> tmp.tbl

    evolution.tbl <- rbind(evolution.tbl, tmp.tbl)
  }
}

outFile <- gettextf("%s/runs.csv", outDir)
write_csv(runs.tbl, outFile)

prefixes <- unique(runs.tbl$prefix)

all.selections <- unique(runs.tbl$sel)

all.cases <- c(case.all.tag, unique(runs.tbl$case))

all.interactions <- unique(runs.tbl$socInteraction)

all.nets <- unique(runs.tbl$netType)

cat(gettextf("* Loaded runs data from %d directories. Found %d runs.\n", length(runDirs), nrow(runs.tbl) ))

# -------------------------------------------------------------------
# compute summary table
# -------------------------------------------------------------------

summary.tbl <- tibble(prefix=character(),
                      netType=character(),
                      socInteraction=character(),
                      sel=character(),
                      case=character(),
                      num=integer(),
                      epochs.mean=numeric(),
                      epochs.sd=numeric(),
                      epochs.min=numeric(),
                      epochs.max=numeric(),
                      Aratio.mean=numeric(),
                      Aratio.sd=numeric(),
                      Aratio.min=numeric(),
                      Aratio.max=numeric()
)

for(net in all.nets) {
  for(ip in all.interactions) {
    for(sp in all.selections) {
      for(cs in all.cases) {

        sameConf.tbl <- filter(runs.tbl, netType==net, socInteraction==ip, sel==sp, case==cs )

        if(nrow(sameConf.tbl)==0) {
          next()
        }
        rowX <- nrow(summary.tbl) + 1

        summary.tbl[rowX,]$prefix         <- sameConf.tbl[1,]$prefix
        summary.tbl[rowX,]$netType        <- net
        summary.tbl[rowX,]$socInteraction <- ip
        summary.tbl[rowX,]$sel            <- sp
        summary.tbl[rowX,]$case           <- cs
        summary.tbl[rowX,]$num            <- nrow(sameConf.tbl)

        summary.tbl[rowX,]$epochs.mean <- mean(sameConf.tbl$epochs, na.rm=TRUE)
        summary.tbl[rowX,]$epochs.sd   <- sd(sameConf.tbl$epochs, na.rm=TRUE)
        summary.tbl[rowX,]$epochs.min  <- min(sameConf.tbl$epochs, na.rm=TRUE)
        summary.tbl[rowX,]$epochs.max  <- max(sameConf.tbl$epochs, na.rm=TRUE)

        summary.tbl[rowX,]$Aratio.mean <- mean(sameConf.tbl$finalAratio, na.rm=TRUE)
        summary.tbl[rowX,]$Aratio.sd   <- sd(sameConf.tbl$finalAratio, na.rm=TRUE)
        summary.tbl[rowX,]$Aratio.min  <- min(sameConf.tbl$finalAratio, na.rm=TRUE)
        summary.tbl[rowX,]$Aratio.max  <- max(sameConf.tbl$finalAratio, na.rm=TRUE)
      }
    }
  }
}

outFile <- gettextf("%s/summary.csv", outDir)
write_csv(summary.tbl, outFile)

outFile <- gettextf("%s/summary.tex", outDir)
print(xtable(summary.tbl), file = outFile)


for(ip in all.interactions) {
  for(sp in all.selections) {
    filter(summary.tbl, sel==sp) %>%
      select(-prefix, -sel, -socInteraction) -> 
      tmp.tbl
    outFile <- gettextf("%s/summary-%s-%s.tex", outDir, ip, sp)
    print(xtable(tmp.tbl, digits=c(0,0,0,0,2,2,0,0,2,2,2,2)), file = outFile, include.rownames=FALSE)
  }
}

cat("* Summary files: done.\n")

# -------------------------------------------------------------------

# compute statistics and export summary table

interaction_labels <- c('regular' = 'reg',
                        'byStatus' = 'sta',
                        'byDistance' = 'dis',
                        'byDistanceDet' = 'dis' # TODO define other label
                        )

prefix_labels <- c('is17-Par_' = 'Parochial',
                   'is17-Sw_' = 'Small-World',
                   'is17-Reg_' = 'Regular net',
                   'is17-Par-ag_' = 'Parochial',
                   'is17-Sw-ag_' = 'Small-World',
                   'is17-Reg-ag_' = 'Regular net',

                   'is17-Pr-dis-abg_' = 'Parochial',
                   'is17-Sw-dis-abg_' = 'Small-World',
                   'is17-Rg-dis-abg_' = 'Regular net',
                   'is17-Pr-dis-ag_' = 'Parochial',
                   'is17-Sw-dis-ag_' = 'Small-World',
                   'is17-Rg-dis-ag_' = 'Regular net',
                   'is17-Pr-dis-a_' = 'Parochial',
                   'is17-Sw-dis-a_' = 'Small-World',
                   'is17-Rg-dis-a_' = 'Regular net',

                   'is17-b-Pr-dis-abg_' = 'Parochial',
                   'is17-b-Sw-dis-abg_' = 'Small-World',
                   'is17-b-Rg-dis-abg_' = 'Regular net',
                   'is17-b-Pr-dis-ag_' = 'Parochial',
                   'is17-b-Sw-dis-ag_' = 'Small-World',
                   'is17-b-Rg-dis-ag_' = 'Regular net',
                   'is17-b-Pr-dis-a_' = 'Parochial',
                   'is17-b-Sw-dis-a_' = 'Small-World',
                   'is17-b-Rg-dis-a_' = 'Regular net'
                   )

prefix_labels_all <- c('is17-Par_' = 'Par (abg)',
                       'is17-Sw_' = 'SW (abg)',
                       'is17-Reg_' = 'Reg (abg)',
                       'is17-Par-ag_' = 'Par (ag)',
                       'is17-Sw-ag_' = 'SW (ag)',
                       'is17-Reg-ag_' = 'Reg (ag)')


for(ip in all.interactions) {
  for(sp in all.selections){

    runs.sel <- filter(runs.tbl, sel==sp, socInteraction==ip)
    # summarize(runs.sel)
    ggplot(data = runs.sel, aes(x=case, y=epochs, fill=case)) + 
      geom_boxplot() + 
      theme_gray(base_size = 18) +
      scale_fill_discrete(guide=FALSE) +
      facet_grid(.~prefix, labeller = as_labeller(prefix_labels)) +
      labs(title="Epochs per simulation run", x="competition outcome")

    outFile <- gettextf("%s/epochs-boxplot-%s-%s.pdf", outDir, interaction_labels[ip], sp)
    ggsave(outFile, width=18.6, height=13.5, units="cm")

    ggplot(data=runs.sel, aes(x=case, fill=case)) +
      geom_bar(aes(y = (..count..)/sum(..count..))) +
      theme_gray(base_size = 18) +
      scale_fill_discrete(guide=FALSE) +
      scale_y_continuous(labels = percent_format()) + 
      labs(y="outcome proportion", title="Outcomes in variant competition", x="competition outcome") + 
      facet_grid(.~prefix, labeller = as_labeller(prefix_labels))

    outFile <- gettextf("%s/epochs-barplot-%s-%s.pdf", outDir, interaction_labels[ip], sp)
    ggsave(outFile, width=18.6, height=13.5, units="cm")

    ggplot(data=runs.sel, aes(x=case, fill=case)) +
      geom_bar(aes(y = (..count..)/tapply(..count..,..PANEL..,sum)[..PANEL..])) +
      theme_gray(base_size = 18) +
      scale_fill_discrete(guide=FALSE) +
      scale_y_continuous(labels = percent_format()) + 
      labs(y="outcome proportion", title="Outcomes in variant competition", x="competition outcome") + 
      facet_grid(.~prefix, labeller = as_labeller(prefix_labels))

    outFile <- gettextf("%s/epochs-barplot-%s-%s-panel.pdf", outDir, interaction_labels[ip], sp)
    ggsave(outFile, width=18.6, height=13.5, units="cm")
  }# end for sp
}#end for ip

cat("* Epochs box- and bar-plots: done.\n")

# -------------------------------------------------------------------

# plot evolutions
for(ip in all.interactions) {
  for(sp in all.selections) {

    evolution.m <- tibble(epoch=integer(),
                          Aratio.mean=numeric(),
                          Aratio.sd=numeric(),
                          Aratio.min=numeric(),
                          Aratio.max=numeric(),
                          prefix=character(),
                          case=character() )

    for(ex in 1:kamoso$epochs.max){
      for( pfx in prefixes) {

        tmp.tbl <- filter(runs.tbl, sel==sp, prefix==pfx, socInteraction==ip)
        if(nrow(tmp.tbl)==0) {
          warning(gettextf('Skipping sp=%s; pfx=%s: no data found', sp, pfx))
          next()
        }
        ids <- unique(tmp.tbl$ID)

        filter(evolution.tbl, ID %in% ids, epoch == ex) %>%
          mutate(Aratio=productionsA/(productionsA+productionsB)) -> thisEpoch

        rowX <- nrow(evolution.m) + 1

        evolution.m[rowX,]$epoch <- ex
        evolution.m[rowX,]$Aratio.mean <- mean(thisEpoch$Aratio, na.rm = TRUE)
        evolution.m[rowX,]$Aratio.sd   <- sd(thisEpoch$Aratio, na.rm = TRUE)
        evolution.m[rowX,]$Aratio.min  <- min(thisEpoch$Aratio, na.rm = TRUE)
        evolution.m[rowX,]$Aratio.max  <- max(thisEpoch$Aratio, na.rm = TRUE)
        evolution.m[rowX,]$prefix      <- pfx
        evolution.m[rowX,]$case        <- case.all.tag

        for(cx in all.cases) {

          case.tbl <- filter(tmp.tbl, case==cx)
          if(nrow(case.tbl)==0) {
            warning(gettextf('Skipping sp=%s; pfx=%s, case=%s: no data found', sp, pfx, cx))
            next()
          }
          ids <- unique(case.tbl$ID)
          thisCaseEpoch <- filter(thisEpoch, ID %in% ids)

          rowX <- nrow(evolution.m) + 1
          evolution.m[rowX,]$epoch <- ex
          evolution.m[rowX,]$Aratio.mean <- mean(thisCaseEpoch$Aratio, na.rm = TRUE)
          evolution.m[rowX,]$Aratio.sd   <- sd(thisCaseEpoch$Aratio, na.rm = TRUE)
          evolution.m[rowX,]$Aratio.min  <- min(thisCaseEpoch$Aratio, na.rm = TRUE)
          evolution.m[rowX,]$Aratio.max  <- max(thisCaseEpoch$Aratio, na.rm = TRUE)
          evolution.m[rowX,]$prefix      <- pfx
          evolution.m[rowX,]$case        <- cx
        }# end for cx
      }# end for pfx
    }# end for ex

    for(cx in all.cases) {

      case.tbl <- filter(evolution.m, case==cx)
      
      if(nrow(case.tbl)==0) {
        cat(gettextf("Skipping evolution plot for case %s-%s \"%s\" : no data found!\n", ip, sp, cx))
        next()
      }

      ggplot(data=case.tbl, aes(x=epoch, fill=prefix)) +
        geom_ribbon(aes(ymin=Aratio.mean-Aratio.sd, ymax=Aratio.mean+Aratio.sd), alpha=0.33) +
        geom_line(aes(x=epoch, y=Aratio.mean, col=prefix), size=1.3) +
        theme_gray(base_size = 18) +
        scale_fill_discrete(guide=FALSE) +
        scale_color_discrete(guide=FALSE) +
        scale_y_continuous(labels = percent_format()) +
        labs(y="proportion A productions", x="Epoch", 
             title=gettextf("Evolution of A productions (%s outcome)", cx)) + 
        facet_grid(prefix ~ ., labeller = as_labeller(prefix_labels))

      outFile <- gettextf("%s/evolution-%s-%s-%s.pdf", outDir, interaction_labels[ip], sp, cx)
      ggsave(outFile, width=18.6, height=13.5, units="cm")
    }
  }#end for sp
}#end for ip

cat("* Evolution plots: done.\n")


# -------------------------------------------------------------------
# identify interesting runs and produce network/agent plots
# -------------------------------------------------------------------

netplots.tbl <- tibble(netType=character(),
                       socInteraction=character(),
                       sel=character(),
                       case=character(),
                       reference=character(),
                       mean=numeric(),
                       actual=numeric(),
                       dir=character(),
                       conf=character()
)

for(net in all.nets) {
  for(ip in all.interactions) {
    for(sp in all.selections) {
      for(cs in all.cases) {

        summary.tbl[
          summary.tbl$netType==net & 
            summary.tbl$socInteraction==ip &
            summary.tbl$sel==sp &
            summary.tbl$case==cs, ] -> tmp.tbl
        if(nrow(tmp.tbl)==0) {# should be one or zero
          next()
        }

        runs.tbl[
          runs.tbl$netType==net &
            runs.tbl$socInteraction==ip &
            runs.tbl$sel==sp &
            runs.tbl$case==cs, ] -> sameConf.tbl
        
        if(nrow(sameConf.tbl)==0) {
          next()
        }

        if(cs == 'AB') {
          # plot network which is closest to the average final A ratio
          # find a specific results which is closest to the mean outcome
          mutate(sameConf.tbl, mDist = abs(finalAratio - tmp.tbl$Aratio.mean)) -> sameConf.tbl
          x <- which.min(sameConf.tbl$mDist)
          rf <- 'A ratio'
          mn <- tmp.tbl$Aratio.mean
          cl <- sameConf.tbl[x,]$finalAratio
        } else {
          # plot network which is closest to the average number of epochs
          mutate(sameConf.tbl, mDist = abs(epochs - tmp.tbl$epochs.mean)) -> sameConf.tbl
          x <- which.min(sameConf.tbl$mDist)
          rf <- 'epochs'
          mn <- tmp.tbl$epochs.mean
          cl <- sameConf.tbl[x,]$epochs
        }
        cat(gettextf("Plotting net for %s_%s_%s %s : mean %s = %f; closest = %f\n", 
                     net, ip, sp, cs, rf, mn, cl))

        plotNetworkResult(sameConf.tbl[x,]$dir, sameConf.tbl[x,]$conf, cs)
        rowX <- nrow(netplots.tbl) + 1
        netplots.tbl[rowX,]$netType   <- net
        netplots.tbl[rowX,]$socInteraction <- ip
        netplots.tbl[rowX,]$sel       <- sp
        netplots.tbl[rowX,]$case      <- cs
        netplots.tbl[rowX,]$reference <- rf
        netplots.tbl[rowX,]$mean      <- mn
        netplots.tbl[rowX,]$actual    <- cl
        netplots.tbl[rowX,]$dir       <- sameConf.tbl[x,]$dir
        netplots.tbl[rowX,]$conf      <- sameConf.tbl[x,]$conf
      }
    }
  }
}

outFile <- gettextf("%s/netplots.csv", outDir)
write_csv(netplots.tbl, outFile)

cat("* Network plots: done.\n")
