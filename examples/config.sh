#!/bin/bash

# Choose which algorithm to run.
# 1 = Diversity Browsing
# 2 = BRID with sequential scan 
EXAMPLE="2"

DATASET_NAME="MNIST"

FOLD_NAME="QT1"

# The value of "k" for the k-NN
K_VALUE=5

# Options are TRUE and FALSE
BUILD_TREE="TRUE"

# Folder containing the train.csv file.
# The result of the example will be written to this folder under the name of numNeighborsAndMaxRadius.csv
MAIN_FOLDER="MNIST/QT1"

# Location of the file containing query objects.
TEST_FILE_LOCATION="MNIST/QT1/test.csv"

# Location of the file containing the dataset to be queried.
TRAIN_FILE_LOCATION="MNIST/QT1/train.csv"
#TRAIN_FILE_LOCATION="MNIST/full_dataset.csv"

# The folder in which the VP_TREE will be stored.
OUTPUT_FOLDER="MNIST/QT1/outputMaxVar"

# Options are RANDOM and VP_PIVOT
PIVOT_ALGO="VP_PIVOT"

# Options are EUCLIDEAN_DISTANCE and COSINE_DISTANCE
DISTANCE_FUNCTION="EUCLIDEAN_DISTANCE"