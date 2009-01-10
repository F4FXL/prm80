/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.Controler;

import java.util.ArrayList;
import java.util.StringTokenizer;
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
    
    protected final static int RETRY = 5;
    
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
        this.updateState();
    }

    @Override
    public int getRxPLLFrequency() {        
        return this.rxFreq;
    }
    
    @Override
    public void setTxPLLFrequecny(int frequency) {
        this.setPLLFrequencies(this.getRxPLLFrequency(), frequency);
        this.updateState();
    }


    @Override
    public int getTxPLLFrequency() {
        return this.txFrreq;
    }
    
    protected synchronized void setPLLFrequencies(int rxFreq, int txFreq) {
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
        this.updateState();
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
    public synchronized void writeSquelch(int level) {
        this.send("f");
        this.waitChar(':', PRMControler.serialTimeout);
        String sChan = Integer.toString(level);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', PRMControler.serialTimeout);
        this.updateState();
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
    public synchronized void setCurrentChannel(int channel) {
        this.send("n");
        this.waitChar(':', PRMControler.serialTimeout);
        String sChan = Integer.toString(channel);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', PRMControler.serialTimeout);
        this.updateState();
    }

    @Override
    public int getPower() {
        if ( (this.mode & 2) == 2)
            return SerialControler.POWER_LO;
        else
            return SerialControler.POWER_HI;
    }

    @Override
    public synchronized void setPower(int power) {
        int mode = Integer.parseInt(this.sendCommand("e").substring(0, 2), 16);
        mode = mode ^ 2;
        this.send("d");
        this.waitChar(':', PRMControler.serialTimeout);
        String sMode = Integer.toHexString(mode).toUpperCase();
        if (sMode.length() == 1)
            sMode = "0"+sMode;
        this.send(sMode);
        this.waitChar('>', PRMControler.serialTimeout);
        this.updateState();
    }

    @Override
    public void reloadRAM() {
        
    }

    @Override
    public void resetPRM() {
        this.sendCommand("0");
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
        this.sendCommand("x");
    }

    @Override
    public void EEPROM2RAM() {
        this.sendCommand("s");
    }    
    
    protected String sendCommand (String command, String regex) {
        return this.sendCommand(command, PRMControler.serialTimeout, regex);
    }
    
    protected String sendCommand (String command) {
        return this.sendCommand(command, PRMControler.serialTimeout);
    }
    protected synchronized String sendCommand (String command, int waitDuration, String regex) {
        String result = null;
        for (int i= 0; i < RETRY && result == null; i++) {
            if (i > 0)
                this.sendEscapeCommand();
            result = this.sendCommand(command, waitDuration);
            if (regex != null && (result == null || !result.matches(regex)))
                result = null;
        }
        return result;
            
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
        String result = null;
        int chanMax = this.getMaxChan();
        
        for (int i= 0; i < RETRY && result == null; i++) {
            int chanCount = 0;
            list.clear();
            if (i > 0)
                this.sendEscapeCommand();
            result = this.sendCommand("c");
            
            StringTokenizer tokenizer = new StringTokenizer(result, "\r\n");
            if (!tokenizer.hasMoreTokens() || !tokenizer.nextToken().equals("Channels list :")) {
                result = null;
            }
            else {
                while (tokenizer.hasMoreTokens() && result != null) {
                    String line = tokenizer.nextToken();
                    if (chanCount <= chanMax) {
                        if (line.matches("^[0-9]{2} : [0-9A-F]{4} [0-9A-F]{2}$")) {
                            int chanNum = Integer.parseInt(line.substring(0, 2), 10);
                            int freq = Integer.parseInt(line.substring(5, 9), 16);
                            int state = Integer.parseInt(line.substring(10, 12), 16);
                            if (chanNum == chanCount++) {
                                Channel chan = new Channel(freq * this.getPLLStep(), state != 0);
                                list.addChannel(chan);
                            } else {
                                result = null;
                            }
                        }
                        else {
                            result = null;
                        }
                    }
                    else { // last line
                        
                    }
                }
            }

        }
        if (result != null)
            return list;
        else
            return null;
    }

    @Override
    public void setChannels(ChannelList list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected synchronized void updateState() {
        String stateLine = this.sendCommand("e", "^[0-9A-F]{20}\r\n>$");
        
        if (stateLine != null) {
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
    
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    
    public boolean sendEscapeCommand() {
        this.send("!");
        return waitCommandAnswer(PRMControler.serialTimeout) != null;
    }
}
