#!/bin/sh

# ==================================================================================
# Daniel Duran
# Institut für Maschinelle Sprachverarbeitung, Universität Stuttgart, SFB 732 (A2)
#
# Run KaMoso simulations
# for an number of different configurations stored in the specified directory,
# repeating each configuration 20 times. This script produces a new subdirectory in
# the specified output base-directory where the results for each individual
# configuration are stored.
#
# ATTENTION: adjust settings to your system! Running this may take a very long time,
# depending on your machine. Also note that the required R libraries need to be
# installed.
#
# Run this in Kamoso's project directory.
#
# ==================================================================================

HIER=`pwd`
OUTBASE=${HIER}/output/is17/
mkdir $OUTBASE

CONFDIR=${HIER}/config/is17/
CONFS=`ls $CONFDIR`

for LAUF in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20
do
    TIMESTAMP=`date +"%Y-%m-%d+%H-%M-%S"`
    OUTDIR=${OUTBASE}${TIMESTAMP}/
    mkdir $OUTDIR

    for CONF in $CONFS
    do
        TAG=${CONF%.*}

        java -Xmx64G -Xms8G -Dfile.encoding=UTF-8 -cp bin/:lib/*:. \
        sfb732.kamoso.Run \
        -conf $CONFDIR$CONF -epochs 1000 \
        -out $OUTDIR \
        1> $OUTDIR/$TAG.out \
        2> $OUTDIR/$TAG.err

        echo $LAUF " " $TAG " done."
    done
    echo $LAUF " Output directory = " $OUTDIR
done

echo "Simulations done for is17."

cd r/
Rscript --vanilla analyzeRuns.R $OUTBASE $PLOTDIR \
 1> $PLOTDIR/R-alyzeRuns.out \
 2> $PLOTDIR/R-alyzeRuns.err
cd ..
   
echo "R analysis done. Output directory = " $PLOTDIR

