package org.cbsbh;

public class Debug {
    private static final boolean debug = false;
    public static void println(String o){
        if(!debug){return;}
        System.out.println(o);
    }
}
