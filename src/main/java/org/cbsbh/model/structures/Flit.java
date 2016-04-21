package org.cbsbh.model.structures;

import javafx.scene.paint.Color;

import java.util.ArrayList;

/**
 * C*
 * Date: 3/16/15 10:52 AM
 *
 * @author Mihail Chilyashev
 */
// Не е сигурно дали това ще се ползва
// Може да е за метаданни
public class Flit { // *C

    // DEBUG
    public String id;
    private int packetId;
    // /DEBUG

    // UI
    public Color packetColor;
    // /UI

    public static final int FLIT_TYPE_NO_DATA = 0b00;
    public static final int FLIT_TYPE_HEADER = 0b01;
    public static final int FLIT_TYPE_TAIL = 0b10;
    public static final int FLIT_TYPE_BODY = 0b11;

    public ArrayList<String> history = new ArrayList<>();
    public ArrayList<FlitHistoryEntry> pathHistory = new ArrayList<>();

    long flitData; // c*

    /**
     * Съдържа:
     * <ul>
     * <li>битове 0 и 1 - тип на флита</li>
     * <li>бит 2 - EXTERNAL_CLOCK</li>
     * <li>бит 3 - VALID_DATA</li>
     * <li>бит 4 - DATA_ACK</li>
     * <li>бит 5 - CHAN_BUSY (0 - free, 1 - busy)</li>
     * <li>бит 6 - CHANNEL_LOAD</li>
     * <li>бит 7 - празен :(</li>
     * </ul>
     */
    short controlByte;


    public Flit(int sourceId, int targetId, int flitType, int packetId, Color packetColor){
        id = String.format("%d->%d_%d", sourceId, targetId, System.currentTimeMillis());
        this.packetId = packetId;
        this.packetColor = packetColor;
        if(flitType == FLIT_TYPE_BODY) setFlitData(targetId);
        setFlitType(flitType);
        setValidDataBit();
    }

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
     * 0 - StateStructure is not load
     * 1 - StateStructure is very load
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
        assert getFlitType() == FLIT_TYPE_HEADER;
        if (getFlitType() != FLIT_TYPE_HEADER) {
            return -1;
        }
        return (flitData & 0xfff0000) >> 0x10;
    }

    /**
     * Get the Destination Node Address
     *
     * @return the Transport Mask
     */
    public long getDNA() {
        if (getFlitType() != FLIT_TYPE_HEADER) {
            return -1;
        }

        return (flitData & 0xfff);
    }


    public void setValidDataBit() {
        controlByte = (short)(controlByte | (1 << 3));
    }

    public void setFlitType(int flitType) {
        controlByte = (short)(controlByte | (flitType));
    }

    public void setTR(long tr) {
        assert getFlitType() == FLIT_TYPE_HEADER : "Wrong flit type";
        id += " TR: " + Long.toBinaryString(tr);

        long dong = ~((0xfff << 16)); // 0xFFFFFFFFF000FFFF

        flitData = flitData & dong; // Занулява TR

        // След като TR е занулена, слагаме новата
        flitData |= ((tr & 0xfff) << 16);
    }

    public long getFlitData() {
        return flitData;
    }

    public void setFlitData(long flitData) {
        this.flitData = flitData;
    }

    public void setDNA(int DNA) {
        assert getFlitType() == FLIT_TYPE_HEADER : "Wrong flit type";
        assert (flitData & 0xfff) == 0 : "There's something in the 12 least significant bits";
        this.flitData |= (DNA & 0xfff);
    }

    @Override
    public String toString() {
        return "Flit{ " + id  +
                "\nflitType=" + getFlitType() +
                "\n, data=" + getFlitData() +
                //"\n, color=" + packetColor.toString() +
                "\n}\n";
    }

    public int getPacketId() {
        return packetId;
    }
}
