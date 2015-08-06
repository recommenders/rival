/*
 * Copyright 2015 recommenders.net.
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
package net.recommenders.rival.evaluation;

/**
 * Bean class to store an element of type A and another of type B.
 *
 * @param <A> The type of the first element in the pair.
 * @param <B> The type of the second element in the pair.
 *
 * @author <a href="http://github.com/abellogin">Alejandro</a>
 */
public class Pair<A, B> {

    /**
     * First element.
     */
    private A first;
    /**
     * Second element.
     */
    private B second;

    /**
     * Default constructor.
     *
     * @param firstElement first element to store
     * @param secondElement second element to store
     */
    public Pair(final A firstElement, final B secondElement) {
        this.first = firstElement;
        this.second = secondElement;
    }

    /**
     * Gets the first element of the pair.
     *
     * @return the first element of the pair
     */
    public A getFirst() {
        return first;
    }

    /**
     * Gets the second element of the pair.
     *
     * @return the second element of the pair
     */
    public B getSecond() {
        return second;
    }
}
