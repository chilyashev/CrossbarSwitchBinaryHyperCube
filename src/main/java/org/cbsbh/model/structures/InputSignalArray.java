package org.cbsbh.model.structures;

/**
 * Тук ще има много флагове. Няма да е масив от флагове. Не. Ще са много флагове с getter-и и setter-и.
 * Date: 3/16/15 11:00 AM
 *
 * @author Mihail Chilyashev
 */
public class InputSignalArray extends SignalArray {
    public static final int RESET = 0;
    public static final int INIT = 1;
    public static final int FIFO_SELECT = 2;
    public static final int DEMUX_RDY = 3;
    //public static final int VALID_DATA = 4;
    public static final int TIME_ONE = 5;
    public static final int CNT_EQU = 6;
    public static final int TIME_TWO = 7;
    public static final int WR_IN_FIFO = 8;
    public static final int FIFO_BUSY = 9;
    public static final int CHANNEL_BUSY = 10;
    public static final int TRANSFER = 11;
}
