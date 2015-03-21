package org.cbsbh.model.routing.packet.flit;

/**
 * C*
 * Date: 3/16/15 10:52 AM
 *
 * @author Mihail Chilyashev
 */
// Не е сигурно дали това ще се ползва
// Може да е за метаданни
public class Flit {
    enum FlitType{
        TYPE_NORMAL,
        TYPE_HEAD,
        TYPE_TAIL
    }

    FlitType type;

    long flit; // c*

    public Flit(FlitType type) {
        this.type = type;
    }

    public FlitType getType() {
        return type;
    }

    public void setType(FlitType type) {
        this.type = type;
    }
}
