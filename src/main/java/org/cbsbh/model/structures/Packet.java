package org.cbsbh.model.structures;

import javafx.scene.paint.Color;
import org.cbsbh.Debug;

import java.util.ArrayList;

/**
 * A packet is an array of flits.
 * Date: 14/4/16 11:51 PM
 *
 * @author Georgi Georgiev
 */
public class Packet extends ArrayList<Flit> {

    Color color;

    public Packet() {
        super();
    }

    public Packet(int sourceId, int targetId, int packetSize) {
        color = new Color(Math.random(), Math.random(), Math.random(), 1);

        ///Generate head flit:
        Flit flit = new Flit(sourceId, targetId, Flit.FLIT_TYPE_HEADER, color);
        flit.setDNA(targetId);
        flit.setTR(sourceId ^ flit.getDNA());
        Debug.printf("> [Just the tip] Generating a message. From %d to %d", sourceId, flit.getDNA());
        this.add(flit);

        //Generate body flits
        while (packetSize-- > 0) {
            this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_BODY, color));
        }

        //Generate tail flit:
        this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_TAIL, color));
    }
}
