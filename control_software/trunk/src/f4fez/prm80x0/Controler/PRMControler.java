/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.Controler;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author f4fez
 */
public abstract class PRMControler implements Controler{
    protected static int serialTimeout = 1000;
    protected int majorFirmwareVersion;
    protected int minorFirmwareVersion;
    protected int prmType;
    private int pllStep = 12500; 
    protected ArrayList<SerialListener> serialListeners;
    protected Thread updateThread;
    protected int updateSleepTime;
    protected boolean connected;
    protected PRMStateChangeListener changeListener;
    private String holdStateString;
    
    protected int rxFreq;
    protected int txFrreq;
    protected int channel;
    protected int squelch;
    protected int volume;
    protected int mode;
    
    public PRMControler() {
        this.serialListeners = new ArrayList();
    }
    
    @Override
    public abstract int connectPRM(String port) throws SerialPortException;

    @Override
    public abstract void disconnectPRM();

    @Override
    public int getPRMState() {
        return SerialControler.STATE_NORMAL;
    }

    @Override
    public void setPLLStep(int frequency) {
        
    }

    @Override
    public int getPLLStep() {
        return this.pllStep;
    }

    @Override
    public void setRxPLLFrequecny(int frequency) {
        this.setPLLFrequencies(frequency, this.getTxPLLFrequency());
    }

    @Override
    public int getRxPLLFrequency() {        
        return this.rxFreq;
    }
    
    @Override
    public void setTxPLLFrequecny(int frequency) {
        this.setPLLFrequencies(this.getRxPLLFrequency(), frequency);
    }


    @Override
    public int getTxPLLFrequency() {
        return this.txFrreq;
    }
    
    protected void setPLLFrequencies(int rxFreq, int txFreq) {
        int rxfreq = (rxFreq + DummyControler.IF) / this.pllStep;
        int txfreq = (txFreq) / this.pllStep;
        this.send("r");
        this.waitChar(':', PRMControler.serialTimeout);
        String sFreq = Integer.toString(rxfreq, 16);
        while(sFreq.length() < 4)
            sFreq = "0" + sFreq;
        this.send(sFreq);
        this.waitChar(':', PRMControler.serialTimeout);
        sFreq = Integer.toString(txfreq, 16);
        while(sFreq.length() < 4)
            sFreq = "0" + sFreq;
        this.send(sFreq);
        this.waitChar('>', PRMControler.serialTimeout);
    }
    
    @Override
    public boolean isPllLocked() {
        return true;
    }

    @Override
    public int readVolume() {
        return this.volume;
    }

    @Override
    public void writeVolume(int volume) {
        
    }

    @Override
    public int readSquelch() {
        return this.squelch;
    }

    @Override
    public void writeSquelch(int level) {
        this.send("f");
        this.waitChar(':', PRMControler.serialTimeout);
        String sChan = Integer.toString(level);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', PRMControler.serialTimeout);
    }

    @Override
    public int getMaxChan() {
        return 65;
    }

    @Override
    public int getCurrentChannel() {        
        return this.channel;
    }

    @Override
    public void setCurrentChannel(int channel) {
        this.send("n");
        this.waitChar(':', PRMControler.serialTimeout);
        String sChan = Integer.toString(channel);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', PRMControler.serialTimeout);
    }

    @Override
    public int getPower() {
        if ( (this.mode & 2) == 2)
            return SerialControler.POWER_LO;
        else
            return SerialControler.POWER_HI;
    }

    @Override
    public void setPower(int power) {
        int mode = Integer.parseInt(this.sendCommand("e").substring(0, 2), 16);
        mode = mode ^ 2;
        this.send("d");
        this.waitChar(':', PRMControler.serialTimeout);
        String sMode = Integer.toHexString(mode).toUpperCase();
        if (sMode.length() == 1)
            sMode = "0"+sMode;
        this.send(sMode);
        this.waitChar('>', PRMControler.serialTimeout);
    }

    @Override
    public void reloadRAM() {
        
    }

    @Override
    public void resetPRM() {
        
    }

    @Override
    public int getMajorFirmwareVersion() {
        return this.majorFirmwareVersion;
    }

    @Override
    public int getMinorFirmwareVersion() {
        return this.minorFirmwareVersion;
    }
    
    @Override
    public void RAM2EEPROM() {
        
    }

    @Override
    public void EEPROM2RAM() {
        
    }    
    
    protected String sendCommand (String command) {
        return this.sendCommand(command, PRMControler.serialTimeout);
    }
    protected synchronized String sendCommand (String command, int waitDuration) {
        this.send(command);
        return waitCommandAnswer(waitDuration);
    }
    
    protected abstract void send(String data);
    
    protected synchronized String waitCommandAnswer(int waitTime) {
        return this.waitChar('>', waitTime);
    }
    
    protected abstract String waitChar(char c, int waitTime);
    
    public void addSerialListener(SerialListener list) {
        this.serialListeners.add(list);
    }
    public void removeSerialListener(SerialListener list) {
        this.serialListeners.remove(list);
    }
    public void removeSerialListeners() {
        this.serialListeners.clear();
    }

    @Override
    public MemoryImage getMemoryImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ChannelList getChannels() {
        ChannelList list = new ChannelList();
        String str = this.sendCommand("c");
        return list;
    }

    @Override
    public void setChannels(ChannelList list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected synchronized void updateState() {
        String stateLine = this.sendCommand("e");
        try {
            if (stateLine != null && stateLine.length() == 23 && !stateLine.equals(this.holdStateString)) {
                int freq = Integer.parseInt(stateLine.substring(12, 16), 16);
                this.rxFreq = freq*this.pllStep-DummyControler.IF;
                freq = Integer.parseInt(stateLine.substring(16, 20), 16);
                this.txFrreq = freq*this.pllStep;
                this.volume =  (255-Integer.parseInt(stateLine.substring(8, 10), 16)) >> 4;
                this.squelch = Integer.parseInt(stateLine.substring(6, 8), 16);
                this.channel = Integer.parseInt(stateLine.substring(2, 4), 16);
                this.mode = Integer.parseInt(stateLine.substring(0, 2), 16);
                this.holdStateString = stateLine;
                if (this.changeListener != null)
                    this.changeListener.stateUpdated();
            }
        } catch (NumberFormatException e) {
        }
    }
    
    protected void runUpdateThread(int sleepTime) {
        this.updateSleepTime = sleepTime;
        this.updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(connected) {
                        updateState();
                        Thread.sleep(updateSleepTime);
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(PRMControler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        this.updateThread.start();
    }
    
    @Override
    public void setPRMStateChangeListener(PRMStateChangeListener listener) {
        this.changeListener = listener;
    }
}
