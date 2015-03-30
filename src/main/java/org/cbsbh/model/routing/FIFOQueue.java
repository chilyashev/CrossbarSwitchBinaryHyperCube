package org.cbsbh.model.routing;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.packet.flit.Flit;
import org.cbsbh.model.routing.packet.flit.HeadFlit;
import org.cbsbh.model.routing.packet.flit.TailFlit;
import org.cbsbh.model.structures.InputSignalArray;
import org.cbsbh.model.structures.OutputSignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.util.Queue;

/**
 * Проверката на сигнали се случва в *SignalArray класовете. hasSignal(index), например.
 * Date: 3/16/15 10:48 AM
 *
 * @author Mihail Chilyashev
 */
public class FIFOQueue extends StateStructure implements Tickable {

    public static final int STATE0_INIT = 0;
    public static final int STATE1_UPDATE_FIFO_STATUS = 1;
    public static final int STATE2_WRITE_IN_FIFO = 2;
    public static final int STATE3_END_WRITE = 3;

    protected int state;


    /**
     * баш флит
     */
    Queue<Flit> fifo;

    /**
     * Грижи се за комуникацията с Output StateStructure.
     */
    Arbiter arby;

    public FIFOQueue() {
        // TODO: големината на fifo трябва да е "размерът, указан в интерфейса - 2" заради Head/Tail флитовете
        setState(STATE0_INIT);
    }

    /**
     * Определяне на състоянието на автомата според текущите активни входни сигнали и издаване на изходни сигнали.
     */
    public int calculateState() {
        if(hasInputSignal(InputSignalArray.RESET) || hasInputSignal(InputSignalArray.INIT)){
            state = STATE0_INIT;
        }

        if(state == STATE0_INIT){
            //if()
        }

        return 0xb00b5;
    }


    @Override
    public void tick() {
        int newState = calculateState();
        switch (newState) {
            // doStuff
        }
    }

    /**
     * Добавяне в опашката
     *
     * @param flit елемент.
     */
    public void push(Flit flit) {
        fifo.add(flit);
    }

    /**
     * Махане от опашката
     */
    public void pop() {
        fifo.remove();
    }


    /////////////////////// Clutter

    public Queue<Flit> getFifo() {
        return fifo;
    }

    public void setFifo(Queue<Flit> fifo) {
        this.fifo = fifo;
    }

    public Arbiter getArby() {
        return arby;
    }

    public void setArby(Arbiter arby) {
        this.arby = arby;
    }

    public int getCurrentFlitType() {
        assert fifo.peek() != null : "This is not the flit you are looking for.";
        if(fifo.peek() != null){
            return fifo.peek().getFlitType();
        }
        return -1;
    }

    public boolean isCurrentFlitDataValid() {
        assert fifo.peek() != null : "This is not the flit you are looking for.";
        return fifo.peek().isDataValid();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
