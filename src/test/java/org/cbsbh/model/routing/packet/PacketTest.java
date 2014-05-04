package org.cbsbh.model.routing.packet;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Mihail Chilyashev
 */
public class PacketTest {

    @Test
    public void testGetDNA() {
        Packet pack = new Packet();
        pack.setHeader_1(0xeff0827);
        long expected = 0x827;
        Assert.assertEquals("The DNA should be equal to " + expected, expected, pack.getDNA());
    }

    @Test
    public void testGetTR() {
        Packet pack = new Packet();
        pack.setHeader_1(0xeff0827);
        long expected = 0xeff;
        Assert.assertEquals("The TR should be equal to " + expected, expected, pack.getTR());
        // Testing with an invalid TR
        pack.setHeader_1(0xffffffff);
        expected = 0xfff;
        Assert.assertEquals("The TR should be equal to " + expected, expected, pack.getTR());
    }
}
