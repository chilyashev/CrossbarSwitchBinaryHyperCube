package org.cbsbh.model.structures;

/**
 * A class for bit-by-bit buffering.
 * Probably used for buffering packets.
 * Date: 5/5/14 5:20 PM
 *
 * @author Georgi Georgiev
 */
public class BitwiseFIFOBuff {
    private long buffer = 0;
    private short spaces_left = 31;

    /**
     * Push bit to queue.
     * TODO: Possible change the exception type or remove it altogether.
     *
     * @param bit Pretty self-explanatory. Also: dick joke.
     * @return true - if push is successful; false - if buffer is full
     */
    public boolean push(long bit) throws Exception {
        if (bit != 0 && bit != 1) {
            throw new Exception("Only binary input accepted");
        }

        if (spaces_left > 0) {
            bit <<= spaces_left;
            buffer |= bit;
            spaces_left--;
            return true;
        }

        return false;
    }

    /**
     * Pop bit from queue
     *
     * @return top bit - if queue is not empty; -1 if queue is empty
     */
    public long pop() {
        long ret;
        if (spaces_left < 31) {
            ret = (buffer & 0x80000000) >> 31;

            //the next line is needed because java doesn't let me shift left if there is a 1 in the left-most bit
            //fuck you java
            //should have went assembler
            //TODO: open to suggestions on how to fix this
            buffer &= 0x7fffffff; //ANNIHILATE top bit

            buffer <<= 1;
            spaces_left++;
            return ret;
        }

        return -1;
    }
}
