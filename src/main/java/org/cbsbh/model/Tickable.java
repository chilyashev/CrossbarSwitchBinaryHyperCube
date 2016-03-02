package org.cbsbh.model;

/**
 * Description goes here
 * Date: 6/1/14 7:39 PM
 *
 * @author Mihail Chilyashev
 */
public interface Tickable {
    /**
     * For logic done tick by tick.
     */
    void tick();

    void calculateNewState();
}
