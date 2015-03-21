package org.cbsbh.model.structures;

/**
 * Тук ще има много флагове. Няма да е масив от флагове. Не. Ще са много флагове с getter-и и setter-и.
 * Date: 3/16/15 11:00 AM
 *
 * @author Mihail Chilyashev
 */
public class InputSignalArray extends SignalArray {
    public static int RESET = 0;
    public static int INIT = 1;
    public static int FIFO_SELECT = 2;
    public static int DEMUX_RDY = 3;
    public static int VALID_DATA = 4;
    public static int TIME_ONE = 5;
    public static int CHANNEL_BUSY = 10;
}
