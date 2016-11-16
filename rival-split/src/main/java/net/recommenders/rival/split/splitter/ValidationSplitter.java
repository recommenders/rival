package net.recommenders.rival.split.splitter;

import net.recommenders.rival.core.DataModelIF;
import net.recommenders.rival.core.TemporalDataModelIF;


public class ValidationSplitter<U, I> implements Splitter<U, I> {
    private final Splitter<U, I> splitter;

    public ValidationSplitter(Splitter<U, I> splitter) {
        if (splitter instanceof ValidationSplitter) {
            throw new IllegalArgumentException("Unable to apply a validation splitter recursively!");
        }
        this.splitter = splitter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataModelIF<U, I>[] split(DataModelIF<U, I> data) {
        DataModelIF<U, I>[] trainingTestSplits = this.splitter.split(data);
        DataModelIF<U, I>[] newSplits = new DataModelIF[trainingTestSplits.length / 2 * 3];

        for (int i = 0; i < trainingTestSplits.length / 2; i++) {
            DataModelIF<U, I> trainingVal = trainingTestSplits[2 * i];
            DataModelIF<U, I>[] trainingValSplit = splitter.split(trainingVal);
            DataModelIF<U, I> test = trainingTestSplits[2 * i + 1];

            newSplits[3 * i] = trainingValSplit[0];
            newSplits[3 * i + 1] = trainingValSplit[1];
            newSplits[3 * i + 2] = test;
        }
        return newSplits;

    }

    @SuppressWarnings("unchecked")
    @Override
    public TemporalDataModelIF<U, I>[] split(TemporalDataModelIF<U, I> data) {
        TemporalDataModelIF<U, I>[] trainingTestSplits = this.splitter.split(data);
        TemporalDataModelIF<U, I>[] newSplits = new TemporalDataModelIF[trainingTestSplits.length / 2 * 3];

        for (int i = 0; i < trainingTestSplits.length / 2; i++) {
            TemporalDataModelIF<U, I> trainingVal = trainingTestSplits[2 * i];
            TemporalDataModelIF<U, I>[] trainingValSplit = splitter.split(trainingVal);
            TemporalDataModelIF<U, I> test = trainingTestSplits[2 * i + 1];

            newSplits[3 * i] = trainingValSplit[0];
            newSplits[3 * i + 1] = trainingValSplit[1];
            newSplits[3 * i + 2] = test;
        }
        return newSplits;
    }
}
