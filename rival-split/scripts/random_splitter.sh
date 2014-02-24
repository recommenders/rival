#!/bin/sh

# usage: dataset field_separator percentage_training output_folder training_prefix training_suffix test_prefix test_suffix per_user per_items overwrite
dataset=${1}
field_separator=${2}
percentage_training=${3}
output_folder=${4}
training_prefix=${5}
training_suffix=${6}
test_prefix=${7}
test_suffix=${8}
per_user=${9}
per_items=${10} # unused
overwrite=${11}

# a tab is passed as $'\t'
if [[ $field_separator == "	" ]]
then
	users=`cut -f1 $dataset | sort | uniq`
else
	users=`cut -f1 -d $'$field_separator' $dataset | sort | uniq`
fi

if $per_user
then
	# per_user random split
	training_file=$output_folder$training_prefix$percentage_training$training_suffix
	test_file=$output_folder$test_prefix$percentage_training$test_suffix

	if [ -f $training_file ] && [ -f $test_file ] && ! $overwrite
	then
		echo "ignoring $training_file and $test_file "
	else
	    if [ -f $training_file ]
	    then
		rm $training_file
	    fi
	    if [ -f $test_file ]
	    then
		rm $test_file
	    fi
	    for user in $users
	    do
		dataset_user=$dataset\_$user
		grep -P "^$user$field_separator" $dataset | shuf > $dataset_user

		RATINGS_COUNT=`wc -l $dataset_user | cut -d ' ' -f 1`
		SPLIT_SIZE=`echo $RATINGS_COUNT $percentage_training | awk '{print int($1 * $2)}'`
		DIFFERENCE=`echo $RATINGS_COUNT $SPLIT_SIZE | awk '{print $1 - $2}'`

		# training
		head -$SPLIT_SIZE $dataset_user >> $training_file
		# test
		tail -$DIFFERENCE $dataset_user >> $test_file

		rm $dataset_user
	    done
	    echo "$training_file created.  `wc -l $training_file | cut -d " " -f 1` lines."
	    echo "$test_file created.  `wc -l $test_file | cut -d " " -f 1` lines."
	fi
else
	# global random split
	RATINGS_COUNT=`wc -l $dataset | cut -d ' ' -f 1`
	SPLIT_SIZE=`echo $RATINGS_COUNT $percentage_training | awk '{print int($1 * $2)}'`
	DIFFERENCE=`echo $RATINGS_COUNT $SPLIT_SIZE | awk '{print $1 - $2}'`

	dataset_shuf=$dataset\_shuf
	shuf $dataset > $dataset_shuf

	training_file=$output_folder$training_prefix$percentage_training$training_suffix
	test_file=$output_folder$test_prefix$percentage_training$test_suffix

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
fi
