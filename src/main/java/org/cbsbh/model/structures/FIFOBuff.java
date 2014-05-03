package org.cbsbh.model.structures;

import java.util.LinkedList;

/**
 * A class for the buffers used in some of the components.
 * It's kinda not thread-safe, but we don't need it to be, so it's all fine.
 * Date: 4/29/14 7:37 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOBuff<T> {
    private LinkedList<T> content;

    public FIFOBuff() {
        content = new LinkedList<>();
    }

    public void push(T ob) {
        content.add(ob);
    }

    public T pop() {
        if (content.size() > 0) {
            return content.pop();
        } else {
            return null;
        }
    }

}
