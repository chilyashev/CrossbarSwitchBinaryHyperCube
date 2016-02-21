package org.cbsbh.model.structures;

import java.util.Arrays;

/**
 * Description goes here
 * Date: 3/16/15 11:34 AM
 *
 * @author Mihail Chilyashev
 */
public class SignalArray {
    // Kinda output:
    public static final int CHAN_BUSY = 0;
    public static final int PACK_WAIT = 1;
    public static final int FIFO_BUSY = 2;
    public static final int CNT_EQU = 3;
    public static final int DATA_ACK = 4;
    public static final int CLR_FIFO = 5;
    public static final int WR_FIFO_EN = 6;
    public static final int WR_IN_FIFO = 7;
    public static final int TIMER_EN = 8;
    public static final int TIME_ONE = 9;
    public static final int BUFF_BUSY = 10;
    public static final int WR_B_RG = 11;
    public static final int DEMUX_RDY = 12;
    public static final int RRA_BUSY = 13;
    public static final int RRA_WORK = 14;
    public static final int STRB_SIG = 15;
    public static final int WR_MUX_ADR = 16;
    public static final int WR_RRA_PTR = 17;
    public static final int VALID_DATA = 18; // не е сигнал. Valid Data е писане в контролния байт на флита
    public static final int WR_RG_OUT = 19;
    public static final int FLT_RD = 20;
    public static final int CLR_MUX_ADDR = 21;

    // Kinda input:

    public static final int RESET = 22;
    public static final int INIT = 23;
    public static final int FIFO_SELECT = 24;
    //public static final int VALID_DATA = 4;
    public static final int TIME_TWO = 25;
    public static final int TRANSFER = 26;

    public static final int LAST_SIGNAL = 26;
    boolean signals[];

    String[] signalDescriptors = new String[LAST_SIGNAL + 1];

    public SignalArray() {
        signals = new boolean[40]; // TODO: това да се направи достатъчно голямо, когато се разбере колко сигнала има.
        Arrays.fill(signals, false);
    }

    public void resetAll() {
        for (int i = 0; i < signals.length; i++) {
            signals[i] = false;
        }
    }

    public void setSignal(int index, boolean value) {
        assert index >= 0 && index < signals.length : "this index doesn't exist. Sucker.";
        signals[index] = value;
    }

    public boolean hasSignal(int index) {
        assert index >= 0 && index < signals.length : String.format("this index (%d) doesn't exist. Sucker.", index);
        return signals[index];
    }


    public String getSignalName(int signal) {
        // Kinda output:
        signalDescriptors[CHAN_BUSY] = "CHAN_BUSY";
        signalDescriptors[PACK_WAIT] = "PACK_WAIT";
        signalDescriptors[FIFO_BUSY] = "FIFO_BUSY";
        signalDescriptors[CNT_EQU] = "CNT_EQU";
        signalDescriptors[DATA_ACK] = "DATA_ACK";
        signalDescriptors[CLR_FIFO] = "CLR_FIFO";
        signalDescriptors[WR_FIFO_EN] = "WR_FIFO_EN";
        signalDescriptors[WR_IN_FIFO] = "WR_IN_FIFO";
        signalDescriptors[TIMER_EN] = "TIMER_EN";
        signalDescriptors[TIME_ONE] = "TIME_ONE";
        signalDescriptors[BUFF_BUSY] = "BUFF_BUSY";
        signalDescriptors[WR_B_RG] = "WR_B_RG";
        signalDescriptors[DEMUX_RDY] = "DEMUX_RDY";
        signalDescriptors[RRA_BUSY] = "RRA_BUSY";
        signalDescriptors[RRA_WORK] = "RRA_WORK";
        signalDescriptors[STRB_SIG] = "STRB_SIG";
        signalDescriptors[WR_MUX_ADR] = "WR_MUX_ADR";
        signalDescriptors[WR_RRA_PTR] = "WR_RRA_PTR";
        signalDescriptors[VALID_DATA] = "VALID_DATA";
        signalDescriptors[WR_RG_OUT] = "WR_RG_OUT";
        signalDescriptors[FLT_RD] = "FLT_RD";
        signalDescriptors[CLR_MUX_ADDR] = "CLR_MUX_ADDR";

        // Kinda input:
        signalDescriptors[RESET] = "RESET";
        signalDescriptors[INIT] = "INIT";
        signalDescriptors[FIFO_SELECT] = "FIFO_SELECT";
        signalDescriptors[VALID_DATA] = "VALID_DATA";
        signalDescriptors[TIME_TWO] = "TIME_TWO";
        signalDescriptors[TRANSFER] = "TRANSFER";

        assert signalDescriptors[signal] != null;

        return signalDescriptors[signal];
    }

    public boolean[] getSignals() {
        return signals;
    }
}
