/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.Controler;

import java.util.ArrayList;


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
        int freq = Integer.parseInt(this.sendCommand("e").substring(12, 16), 16);
        return freq*this.pllStep-DummyControler.IF;
    }
    
    @Override
    public void setTxPLLFrequecny(int frequency) {
        this.setPLLFrequencies(this.getRxPLLFrequency(), frequency);
    }


    @Override
    public int getTxPLLFrequency() {
        int freq = Integer.parseInt(this.sendCommand("e").substring(16, 20), 16);
        return freq*this.pllStep;
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
        return (255-Integer.parseInt(this.sendCommand("e").substring(8, 10), 16)) >> 4;
    }

    @Override
    public void writeVolume(int volume) {
        
    }

    @Override
    public int readSquelch() {
        return Integer.parseInt(this.sendCommand("e").substring(6, 8), 16);
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
        return Integer.parseInt(this.sendCommand("e").substring(2, 4), 16);
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
        int mode = Integer.parseInt(this.sendCommand("e").substring(0, 2), 16);
        if ( (mode & 2) == 2)
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
    protected String sendCommand (String command, int waitDuration) {
        this.send(command);
        return waitCommandAnswer(waitDuration);
    }
    
    protected abstract void send(String data);
    
    protected String waitCommandAnswer(int waitTime) {
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setChannels(ChannelList list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
