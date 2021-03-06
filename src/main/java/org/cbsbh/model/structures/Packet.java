package org.cbsbh.model.structures;

import javafx.scene.paint.Color;
import org.cbsbh.Debug;

import java.util.ArrayList;

/**
 * A packet is an array of flits.
 * Date: 14/4/16 11:51 PM
 *
 * @author Georgi Georgiev, Mr. Mihail Chilyashev
 */
public class Packet extends ArrayList<Flit> {

    private int sourceId;
    private int targetId;

    private String id;
    private Color color;


    public Packet() {
        super();
    }

    public Packet(String id, int sourceId, int targetId, int packetSize) {
        color = new Color(Math.random(), Math.random(), Math.random(), 1);
        this.id = id;
        this.sourceId = sourceId;
        this.targetId = targetId;
        ///Generate head flit:
        Flit flit = new Flit(sourceId, targetId, Flit.FLIT_TYPE_HEADER, id, color);
        flit.setDNA(targetId);
        flit.setTR(sourceId ^ flit.getDNA());
        Debug.printf("> [Just the tip] Generating a message. From %d to %d", sourceId, flit.getDNA());
        this.add(flit);

        //Generate body flits
        while (packetSize-- > 0) {
            this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_BODY, id, color));
        }

        //Generate tail flit:
        this.add(new Flit(sourceId, targetId, Flit.FLIT_TYPE_TAIL, id, color));
    }

    public String getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public int getSourceId() {
        return sourceId;
    }

    public int getTargetId() {
        return targetId;
    }
}
