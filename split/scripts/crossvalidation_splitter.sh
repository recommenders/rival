#!/bin/sh
# similar to 'split_ratings.sh' from movielens10m package

# usage: dataset field_separator num_folds output_folder training_prefix training_suffix test_prefix test_suffix per_user overwrite
dataset=${1}
field_separator=${2}
num_fold=${3}
output_folder=${4}
training_prefix=${5}
training_suffix=${6}
test_prefix=${7}
test_suffix=${8}
per_user=${9}
overwrite=${10}

RATINGS_COUNT=`wc -l $dataset | cut -d ' ' -f 1`
echo "ratings count: $RATINGS_COUNT"
SET_SIZE=`expr $RATINGS_COUNT / $num_fold`
echo "set size: $SET_SIZE"
REMAINDER=`expr $RATINGS_COUNT % $num_fold`
echo "remainder: $REMAINDER"


dataset_shuf=$dataset\_shuf
shuf $dataset > $dataset_shuf

for (( i=1; i <= $num_fold; i++ ))
do
    training_file=$output_folder$training_prefix$i$training_suffix
    test_file=$output_folder$test_prefix$i$test_suffix

    if [ -f $training_file ] && ! $overwrite
    then
	echo "ignoring $training_file"
    else
	head -`expr \( $i - 1 \) \* $SET_SIZE` $dataset_shuf > $training_file
	tail -`expr \( $num_fold - $i \) \* $SET_SIZE` $dataset_shuf >> $training_file
	if [ $i -ne $num_fold ]; then
		tail -$REMAINDER $dataset_shuf >> $training_file
	fi
	echo "$training_file created.  `wc -l $training_file | cut -d " " -f 1` lines."
    fi

    if [ -f $test_file ] && ! $overwrite
    then
	echo "ignoring $test_file"
    else
	head -`expr $i \* $SET_SIZE` $dataset_shuf | tail -$SET_SIZE > $test_file
	if [ $i -eq $num_fold ]; then
		tail -$REMAINDER $dataset_shuf >> $test_file
	fi
	echo "$test_file created.  `wc -l $test_file | cut -d " " -f 1` lines."
    fi



    if [ $i -eq $num_fold ]; then
       tail -$REMAINDER $dataset_shuf >> $test_file
    else
       tail -$REMAINDER $dataset_shuf >> $training_file
    fi

done

rm $dataset_shuf
