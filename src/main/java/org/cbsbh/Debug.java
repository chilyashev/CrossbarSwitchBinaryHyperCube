package org.cbsbh;

import org.cbsbh.model.Tickable;
import org.cbsbh.model.routing.OutputChannel;
import org.cbsbh.model.structures.SignalArray;
import org.cbsbh.model.structures.StateStructure;

import java.io.*;

public class Debug {

    public static final int CLASS_INPUT_CHANNEL = 0;
    public static final int CLASS_OUTPUT_CHANNEL = 1;
    public static final int CLASS_FIFO_QUEUE = 2;

    private int[][] signals;

    private static final boolean debug = !(!(true));

    private static Debug _instance = null;

    private FileWriter out;

    private BufferedWriter writer;

    private Debug() {

        signals = new int[3][];
        signals[CLASS_INPUT_CHANNEL] = new int[]{
                SignalArray.BUFF_BUSY,
                SignalArray.WR_B_RG,
                SignalArray.DEMUX_RDY,
                SignalArray.CHAN_BUSY,
                SignalArray.FIFO_BUSY,
                SignalArray.WR_IN_FIFO,
        };

        signals[CLASS_OUTPUT_CHANNEL] = new int[]{
                SignalArray.RRA_BUSY,
                SignalArray.RRA_WORK,
                SignalArray.STRB_SIG,
                SignalArray.WR_MUX_ADR,
                SignalArray.WR_RRA_PTR,
                SignalArray.CNT_EQU,
                SignalArray.WR_RG_OUT,
                SignalArray.FLT_RD,
                SignalArray.TIMER_EN,
        };

        signals[CLASS_FIFO_QUEUE] = new int[]{
                SignalArray.CLR_FIFO,
                SignalArray.FIFO_BUSY,
                SignalArray.WR_FIFO_EN,
                SignalArray.PACK_WAIT,
                SignalArray.WR_IN_FIFO,
                SignalArray.DATA_ACK,
                SignalArray.TIMER_EN,
                SignalArray.FIFO_SELECT,
                SignalArray.CNT_EQU
        };

        try {
            out = new FileWriter("lolg.txt");
            writer = new BufferedWriter(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeln(String crap, Object... args) {
        try {
            writer.write(crap + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writef(String crap, Object... args) {
        try {
            writer.write(String.format(crap, args));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Statically-charged method

    public static void println(String o) {
        if (!debug) {
            return;
        }

        if (_instance == null) {
            _instance = new Debug();
        }
        System.out.println(o);
        _instance.writeln(o);
    }

    public static void printf(String o, Object... a) {
        if (!debug) {
            return;
        }

        if (_instance == null) {
            _instance = new Debug();
        }
        System.out.printf(o + "\n", a);
        _instance.writef(o + "\n", a);
    }

    public static void printSignals(int class_id, StateStructure p) {
        if (_instance == null) {
            _instance = new Debug();
        }

        int[] sign = _instance.signals[class_id];

        StringBuilder sb = new StringBuilder(p.getWho() + " ");

        for (int signal : sign) {
            sb.append(String.format("%s: %s, ", p.getSignalArray().getSignalName(signal), p.hasSignal(signal) ? "1" : "0"));
        }
        printf(sb.subSequence(0, sb.length() - 2).toString());
    }

    public static void endTheMisery() {
        if (_instance == null) {
            _instance = new Debug();
        }

        try {
            _instance.writer.flush();
            _instance.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
