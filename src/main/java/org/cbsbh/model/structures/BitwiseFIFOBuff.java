package org.cbsbh.model.structures;

/**
 * A class for bit-by-bit buffering.
 * Probably used for buffering packets.
 * Date: 5/5/14 5:20 PM
 *
 * @author Georgi Georgiev
 */
public class BitwiseFIFOBuff {
    //128 bit buffer: 4 segments of 32 bits
    private long[] buffer = new long[]{0, 0, 0, 0};

    //index of current segment
    private int currentSegment = 0;

    //number of free bits in current segment
    private int freeBits = 31;

    //Total number of significant bits in the queue
    private int significantBits = 0;

    /**
     * Push bit to queue.
     * TODO: Possibly change the exception type or remove it altogether.
     *
     * @param bit Pretty self-explanatory
     * @return true - if push is successful; false - if buffer is full
     */
    public boolean push(long bit) throws Exception {
        if (bit != 0 && bit != 1) {
            throw new Exception("Only binary input accepted");
        }

        //Are there any significant bits left?
        if (significantBits == 128) {
            return false;
        }

        bit <<= freeBits;
        buffer[currentSegment] |= bit;

        //decrementing circular counter
        freeBits = (freeBits == 0) ? 31 : (freeBits - 1);

        //divide the significant bits by 32 to get the current segment. also sexy bit shifting there
        currentSegment = significantBits >> 5;

        significantBits++;
        return true;
    }

    /**
     * Shift bit from queue
     *
     * @return first bit - if queue is not empty; -1 if queue is empty
     */
    public long shift() {

        //Are there any significant bits left?
        if (significantBits == 0) {
            return -1;
        }

        //carry array to store each segment's carry
        long[] carry = new long[]{0, 0, 0, 0};

        for (int i = currentSegment; i >= 0; i--) {

            //get current segment's carry:
            carry[i] = (buffer[i] & 0x80000000) >> 31;

            //shift the motherfucker left:
            buffer[i] &= 0x7fffffff;
            buffer[i] <<= 1;

            //unless its the last segment
            if (i != 3) {

                //flip its least significant bit to match the previous carry:
                buffer[i] |= carry[i + 1];
            }
        }

        significantBits--;

        //dat current segment
        currentSegment = significantBits >> 5;

        //modulo on powers of 2 can be done by gating the motherfucker. much elegant. wow
        freeBits = ++freeBits & 31;

        return carry[0];
    }

    /**
     * Get header if it has been received.
     *
     * @return header if received, otherwise 0
     */
    public long getReceivedHeader() {
        if (significantBits > 32) {
            return buffer[0];
        }
        return 0;
    }

    /**
     * Get total number of significant bits in the queue.
     *
     * @return number of significant bits in the queue
     */
    public int getSignificantBits() {
        return significantBits;
    }

}