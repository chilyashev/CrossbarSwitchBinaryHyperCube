package org.cbsbh.model.structures;

/**
 * Тук ще има много флагове. Няма да е масив от флагове. Не. Ще са много флагове с getter-и и setter-и.
 * Date: 3/16/15 11:00 AM
 *
 * @author Mihail Chilyashev
 */
public class OutputSignalArray extends SignalArray {
    public static int CHAN_BUSY = 0;
    public static final int PACK_WAIT = 1;
    public static final int FIFO_BUSY = 2;
    public static final int CNT_EQU = 3;
    public static final int DATA_ACK = 4;
    public static final int CLR_FIFO = 5;
    public static final int WR_FIFO_EN = 6;
    public static final int WR_IN_FIFO = 7;
    public static final int TIMER_EN = 8;
    public static final int TIME_ONE = 9;

}
