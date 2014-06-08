package org.cbsbh.model.routing.packet;

/**
 * Contains the data for the packet being transmitted.
 * Date: 5/3/14 4:54 PM
 *
 * @author Mihail Chilyashev
 */
public class Packet {

    /**
     * Contains:<br/>
     * <ul>
     * <li>12 bits for the Destination Node Address(DNA) - bits 0-11</li>
     * <li>4 free bits - bits 12-15</li>
     * <li>12 bits for the Transport Mask (TR) - bits 16-27</li>
     * <li>4 free bits - bits 28-31</li>
     * </ul>
     */
    private long header_1;

    /**
     * The second part of the header.
     * Contains:<br/>
     * <ul>
     * <li>32 bits for the target Memory Address</li>
     * </ul>
     */
    private long memoryAddress;

    /**
     * 32 bits of data
     */
    private long data_l;

    /**
     * 32 more bits of data
     */
    private long data_h;

    /**
     * Add header content separately
     * @param dna the Destination Node Address
     * @param tr the Transport Mask
     * @param memoryAddress Memory address
     * @param data_l Data
     * @param data_h Data
     */
    public Packet(int dna, int tr, long memoryAddress, long data_l, long data_h) {
        this(((tr & 0xfff) << 16) + (dna & 0xfff), memoryAddress, data_l, data_h);
    }

    public Packet(long header_1, long memoryAddress, long data_l, long data_h) {
        this.header_1 = header_1;
        this.memoryAddress = memoryAddress;
        this.data_l = data_l;
        this.data_h = data_h;
    }

    public Packet() {
    }

    /**
     * Get the lower 12 bits of the first header
     *
     * @return the Destination Node Address
     */
    public long getDNA() {
        return (header_1 & 0xfff);
    }

    /**
     * Get the Transport Mask
     *
     * @return the Transport Mask
     */
    public long getTR() {
        return (header_1 & 0xfff0000) >> 0x10;
    }

    /**
     * @return the MemoryAddress
     */
    public long getMemoryAddress() {
        return memoryAddress;
    }

    public void setMemoryAddress(long memoryAddress) {
        this.memoryAddress = memoryAddress;
    }

    public long getData_l() {
        return data_l;
    }

    public long getData_h() {
        return data_h;
    }

    public long getHeader_1() {
        return header_1;
    }

    public void setHeader_1(long header_1) {
        this.header_1 = header_1;
    }

    public void setData_l(long data_l) {
        this.data_l = data_l;
    }

    public void setData_h(long data_h) {
        this.data_h = data_h;
    }

    public void setTR(long tr) {
        header_1 = ((tr & 0xfff) << 16) + (getDNA() & 0xfff);
    }
}
