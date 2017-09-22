/*
 * Copyright 2017 recommenders.net.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.recommenders.rival.split.splitter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;

/**
 * Class that splits a dataset using a leave one out user based splitter It
 * leaves the result in the path passed as parameters, returns null.
 * 
 * @author <a href="https://github.com/andresmore">Andrés Moreno </a>
 *
 */
public class IterativeLeaveOneOutSplitter<U, I> implements Splitter<U, I> {

	/**
	 * An instance of a Random class.
	 */
	private Random rnd = new Random(0);

	/**
	 * Minimum number of preferences of user to include it on train and test set
	 */
	private int minPreferences = 0;

	/**
	 * Path out
	 */
	private String outPath;

	/**
	 * Constructor.
	 *
	 * @param seed
	 *            value to initialize a Random class
	 * @param minPreferences
	 *            minimum number of preferences either the user must have to be
	 *            included in the splits.
	 * @param outPath
	 *            folder where each split (train and test) will be written
	 */
	public IterativeLeaveOneOutSplitter(final long seed, final int minPreferences, final String outPath) {

		this.rnd = new Random(seed);
		this.minPreferences = minPreferences;
		this.outPath = outPath;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataModelIF<U, I>[] split(DataModelIF<U, I> data) {

		@SuppressWarnings("unchecked")
		final PrintWriter[] splits = new PrintWriter[2];

		TemporalDataModelIF<U, I> temporalModel = null;

		boolean hasTimestamps = data instanceof TemporalDataModelIF<?, ?>;
		if (hasTimestamps) {
			temporalModel = (TemporalDataModelIF<U, I>) data;
		}
		String trainFile = outPath + "train_0.csv";
		String testFile = outPath + "test_0.csv";
		try {
			splits[0] = new PrintWriter(trainFile);
			splits[1] = new PrintWriter(testFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error writting: " + e);
		}

		for (U user : data.getUsers()) {

			List<I> items = new ArrayList<>();
			for (I i : data.getUserItems(user)) {
				items.add(i);
			}

			if (items.size() >= minPreferences) {

				Collections.shuffle(items, rnd);
				I crossValidatedItem = items.remove(0);
				double prefCV = data.getUserItemPreference(user, crossValidatedItem);
				String timestamp = null;

				if (hasTimestamps) {
					List<Long> times = new ArrayList<>();
					for (Long time : temporalModel.getUserItemTimestamps(user, crossValidatedItem)) {
						times.add(time);
					}

					timestamp = "" + Collections.min(times);
					splits[1].println(user + "\t" + crossValidatedItem + "\t" + prefCV + "\t" + timestamp);
				} else {
					splits[1].println(user + "\t" + crossValidatedItem + "\t" + prefCV);
				}

				for (I item : items) {
					Double pref = data.getUserItemPreference(user, item);
					if (hasTimestamps) {
						List<Long> times = new ArrayList<>();
						for (Long time : temporalModel.getUserItemTimestamps(user, item)) {
							times.add(time);
						}
						timestamp = "" + Collections.min(times);
						splits[0].println(user + "\t" + item + "\t" + pref + "\t" + timestamp);
					}
					else {
						splits[0].println(user + "\t" + item + "\t" + pref);
					}
					
				}

			}

		}
		for (int i = 0; i < splits.length; i++) {
			splits[i].flush();
			splits[i].close();
		}
		return null;
	}

	@Override
	public TemporalDataModelIF<U, I>[] split(TemporalDataModelIF<U, I> data) {

		return (TemporalDataModelIF<U, I>[]) this.split(((DataModelIF) data));
	}

}
