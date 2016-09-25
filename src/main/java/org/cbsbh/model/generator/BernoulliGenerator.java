package org.cbsbh.model.generator;

import java.util.Random;

public class BernoulliGenerator implements Generator{

    private double probability = 0.5;

    private Random r = new Random();

    @Override
    public boolean newValueReady() {
        return r.nextDouble() <= probability;
    }

    @Override
    public int getNewValue(int from, int to) {
        return r.nextInt();
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
