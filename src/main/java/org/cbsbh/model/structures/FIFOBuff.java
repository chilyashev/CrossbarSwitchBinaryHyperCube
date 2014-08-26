package org.cbsbh.model.structures;

import java.util.LinkedList;
import java.util.List;

/**
 * A class for the buffers used in some of the components.
 * It's kinda not thread-safe, but we don't need it to be, so it's all fine.
 * Date: 4/29/14 7:37 PM
 *
 * @author Mihail Chilyashev
 */
public class FIFOBuff<T> {
    private int maxSize = 4;
    private boolean full;
    private LinkedList<T> content;

    public FIFOBuff(int maxSize) {
        this.maxSize = maxSize;
    }

    public FIFOBuff() {
        content = new LinkedList<>();
    }

    public void push(T ob) {
        /*if(ob.equals(0)){
            System.err.println("stopmeh");
        }*/
        if(content.size()+1 > maxSize){
            full = true;
            return;
        }
        content.add(ob);
    }

    public T pop() {
        full = false;
        if (content.size() > 0) {
            return content.pop();
        } else {
            return null;
        }
    }

    public void clear(){
            content.clear();
    }

    public boolean isFull() {
        return full;
    }

    public int getItemCount(){
        return content.size();
    }

    public T peek() {
        if (content.size() > 0) {
            return content.peek();
        } else {
            return null;
        }
    }

    public List<T> toList(){
        return content;
    }
}
