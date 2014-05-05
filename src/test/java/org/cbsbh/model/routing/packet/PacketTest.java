package org.cbsbh.model.routing.packet;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Mihail Chilyashev
 */
//TODO: better test messages
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
    }

    @Test
    public void testGetInvalidHeaderTR() {
        Packet pack = new Packet();
        pack.setHeader_1(0xffffffff);
        long expected = 0xfff;
        Assert.assertEquals("The TR should be equal to " + expected, expected, pack.getTR());
    }

    @Test
    public void testConstructorDNA(){
        Packet pack = new Packet(0x777, 0x808, 0xffffffff, 0xffffffff, 0xffffffff);
        long expected = 0x777;

        Assert.assertEquals("The DNA test failed", expected, pack.getDNA());
    }

    @Test
    public void testConstructorTR(){
        Packet pack = new Packet(0x777, 0x808, 0xffffffff, 0xffffffff, 0xffffffff);
        long expected = 0x808;

        Assert.assertEquals("The TR should be equal to " + expected, expected, pack.getTR());
    }

    @Test
    public void testStandardConstructorDNA(){
        Packet pack = new Packet(0xfff0777, 0xffffffff, 0xffffffff, 0xffffffff);
        long expected = 0x777;
        Assert.assertEquals("The DNA test failed", expected, pack.getDNA());
    }

    @Test
    public void testStandardConstructorTR(){
        Packet pack = new Packet(0x8080777, 0xffffffff, 0xffffffff, 0xffffffff);
        long expected = 0x808;
        Assert.assertEquals("The standard constructor TR test failed", expected, pack.getTR());
    }
}
