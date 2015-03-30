package org.cbsbh.model.routing.packet.flit;

/**
 * C*
 * Date: 3/16/15 10:52 AM
 *
 * @author Mihail Chilyashev
 */
// Не е сигурно дали това ще се ползва
// Може да е за метаданни
public class Flit { // *C

    public static final int FLIT_TYPE_NO_DATA = 0b00;
    public static final int FLIT_TYPE_HEADER = 0b01;
    public static final int FLIT_TYPE_TAIL = 0b10;
    public static final int FLIT_TYPE_BODY = 0b11;

    long flitData; // c*

    /**
     * Съдържа:
     * <ul>
     * <li>битове 0 и 1 - тип на флита</li>
     * <li>бит 2 - EXTERNAL_CLOCK</li>
     * <li>бит 3 - VALID_DATA</li>
     * <li>бит 4 - DATA_ACK</li>
     * <li>бит 5 - CHANNEL_BUSY (0 - free, 1 - busy)</li>
     * <li>бит 6 - CHANNEL_LOAD</li>
     * <li>бит 7 - празен :(</li>
     * </ul>
     */
    short controlByte;


    /**
     * Ако младшите два бита на controlByte-а са:
     * <ul>
     * <li>00 - NO_DATA, т.е. празен флит</li>
     * <li>01 - header флит</li>
     * <li>10 - tail флит</li>
     * <li>11 - body</li>
     * </ul>
     *
     * @return типът на флита
     */
    public int getFlitType() {
        return controlByte & 0b11;
    }

    public int getExternalClock() {
        return controlByte & (1 << 2);
    }

    public boolean isDataValid() {
        return (controlByte & (1 << 3)) != 0;
    }

    /**
     * @return true if busy. Not true (a.k.a false) - not busy.
     */
    public boolean isChannelBusy() {
        return (controlByte >> 4 & 1) == 1;
    }

    /**
     * 0 - Channel is not load
     * 1 - Channel is very load
     *
     * @return true or false
     */
    public boolean isChannelUnderHeavyLoad() {
        return (controlByte >> 5 & 1) == 1;
    }

    /**
     * Get the Transport Mask
     *
     * @return the Transport Mask
     */
    public long getTR() {
        if(getFlitType() != FLIT_TYPE_HEADER){
            return -1;
        }
        return (flitData & 0xfff0000) >> 0x10;
    }


    public void setTR(long tr) {
        assert getFlitType() == FLIT_TYPE_HEADER : "Wrong flit type";
        flitData = ((tr & 0xfff) << 16);
    }
}
