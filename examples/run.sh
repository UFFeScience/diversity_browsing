#!/bin/bash

echo "Starting..." > /dev/stdout
source config.sh
if [ "$BUILD_TREE" = "TRUE" ] ; then
	java -Xmx11G -jar LIDEffectOnDiversitySearch.jar -example $EXAMPLE -k $K_VALUE -mainfolder $MAIN_FOLDER -testfile $TEST_FILE_LOCATION -trainfile $TRAIN_FILE_LOCATION -output $OUTPUT_FOLDER -datasetname $DATASET_NAME -pivotalgo $PIVOT_ALGO -distfunc $DISTANCE_FUNCTION -vptype VP_TREE -buildtree > /dev/stdout
else
	java -Xmx11G -jar LIDEffectOnDiversitySearch.jar -example $EXAMPLE -k $K_VALUE -mainfolder $MAIN_FOLDER -testfile $TEST_FILE_LOCATION -trainfile $TRAIN_FILE_LOCATION -output $OUTPUT_FOLDER -datasetname $DATASET_NAME -pivotalgo $PIVOT_ALGO -distfunc $DISTANCE_FUNCTION -vptype VP_TREE > /dev/stdout
fi
