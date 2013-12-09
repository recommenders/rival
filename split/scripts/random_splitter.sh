#!/bin/sh

# usage: dataset field_separator num_folds output_folder training_prefix training_suffix test_prefix test_suffix per_user per_items overwrite
dataset=${1}
field_separator=${2}
num_fold=${3}
output_folder=${4}
training_prefix=${5}
training_suffix=${6}
test_prefix=${7}
test_suffix=${8}
per_user=${9}
per_items=${10}
overwrite=${11}

RATINGS_COUNT=`wc -l $dataset | cut -d ' ' -f 1`
echo "ratings count: $RATINGS_COUNT"
SET_SIZE=`expr $RATINGS_COUNT / $num_fold`
echo "set size: $SET_SIZE"
SPLIT_SIZE=`expr \( $num_fold - 1 \) \* $SET_SIZE`
echo "split size: $SPLIT_SIZE"
DIFFERENCE=`expr $RATINGS_COUNT - $SPLIT_SIZE`
echo "difference: $DIFFERENCE"

echo $overwrite

for (( i=1; i <= $num_fold; i++ ))
do
    dataset_shuf=$dataset\_shuf
    #shuf $dataset > $dataset_shuf
    cp $dataset $dataset_shuf

    training_file=$output_folder$training_prefix$i$training_suffix
    test_file=$output_folder$test_prefix$i$test_suffix

    if [ -f $training_file ] && ! $overwrite
    then
	echo "ignoring $training_file"
    else
	head -$SPLIT_SIZE $dataset_shuf > $training_file
	echo "$training_file created.  `wc -l $training_file | cut -d " " -f 1` lines."
    fi

    if [ -f $test_file ] && ! $overwrite
    then
	echo "ignoring $test_file"
    else
	tail -$DIFFERENCE $dataset_shuf > $test_file
	echo "$test_file created.  `wc -l $test_file | cut -d " " -f 1` lines."
    fi

    # delete files
    rm $dataset_shuf
done
