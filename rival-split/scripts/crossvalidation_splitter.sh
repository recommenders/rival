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

# a tab is passed as $'\t'
if [[ $field_separator == "	" ]]
then
	users=`cut -f1 $dataset | sort | uniq`
else
	users=`cut -f1 -d $'$field_separator' $dataset | sort | uniq`
fi

if $per_user
then
	echo "Per user cross validation"
	# per_user cross validation
	for (( i=1; i <= $num_fold; i++ ))
	do
	    training_file=$output_folder$training_prefix$i$training_suffix
	    test_file=$output_folder$test_prefix$i$test_suffix

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
			SET_SIZE=`expr $RATINGS_COUNT / $num_fold`
			REMAINDER=`expr $RATINGS_COUNT % $num_fold`

			# training
			head -$(( ($i - 1) * $SET_SIZE )) $dataset_user >> $training_file
			if [ $i -ne $num_fold ]; then
				tail -n +$(( $i * $SET_SIZE + 1 )) $dataset_user >> $training_file
			fi

			# test
			sed -n "$(( ($i - 1) * $SET_SIZE + 1 )), $(( $i * SET_SIZE ))p" $dataset_user >> $test_file
			if [ $i -eq $num_fold ]; then
				tail -$REMAINDER $dataset_user >> $test_file
			fi

			rm $dataset_user
		    done
		    echo "$training_file created.  `wc -l $training_file | cut -d " " -f 1` lines."
		    echo "$test_file created.  `wc -l $test_file | cut -d " " -f 1` lines."
	    fi
	done
else
	echo "Global cross validation"
	# global cross validation
	dataset_shuf=$dataset\_shuf
	shuf $dataset > $dataset_shuf

	RATINGS_COUNT=`wc -l $dataset_shuf| cut -d ' ' -f 1`
	SET_SIZE=`expr $RATINGS_COUNT / $num_fold`
	REMAINDER=`expr $RATINGS_COUNT % $num_fold`

	for (( i=1; i <= $num_fold; i++ ))
	do
	    training_file=$output_folder$training_prefix$i$training_suffix
	    test_file=$output_folder$test_prefix$i$test_suffix

	    if [ -f $training_file ] && ! $overwrite
	    then
		echo "ignoring $training_file"
	    else
		head -$(( ($i - 1) * $SET_SIZE )) $dataset_shuf > $training_file
		if [ $i -ne $num_fold ]; then
			tail -n +$(( $i * $SET_SIZE + 1 )) $dataset_shuf >> $training_file
		fi
		echo "$training_file created.  `wc -l $training_file | cut -d " " -f 1` lines."
	    fi

	    if [ -f $test_file ] && ! $overwrite
	    then
		echo "ignoring $test_file"
	    else
		sed -n "$(( ($i - 1) * $SET_SIZE + 1 )), $(( $i * SET_SIZE ))p" $dataset_shuf > $test_file
		if [ $i -eq $num_fold ]; then
			tail -$REMAINDER $dataset_shuf >> $test_file
		fi
		echo "$test_file created.  `wc -l $test_file | cut -d " " -f 1` lines."
	    fi
	done

	rm $dataset_shuf
fi
