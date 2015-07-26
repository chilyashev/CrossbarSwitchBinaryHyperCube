package org.cbsbh.model.generator;

interface Generator {
    boolean newValueReady();
    int getNewValue(int from, int to);
}
