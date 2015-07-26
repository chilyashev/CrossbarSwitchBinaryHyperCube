package org.cbsbh.model.generator;

import java.util.Random;

public class BernoulliGenerator implements Generator{

    final double BERNOULLI_PROBABILITY = 0.5;

    private Random r = new Random();

    @Override
    public boolean newValueReady() {
        return r.nextDouble() < BERNOULLI_PROBABILITY;
    }

    @Override
    public int getNewValue(int from, int to) {
        return r.nextInt();
    }
}
