package org.cbsbh;

public class Debug {
    private static final boolean debug = true;
    public static void println(String o){
        if(!debug){return;}
        System.out.println(o);
    }
}
