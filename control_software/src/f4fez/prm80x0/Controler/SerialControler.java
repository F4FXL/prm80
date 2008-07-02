/*
 *   Copyright (c) 2007, 2008 Florian MAZEN
 *   
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package f4fez.prm80x0.Controler;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fmazen
 */
public class SerialControler implements PRMControler{
    private static int serialTimeout = 1000;
    private boolean connected;
    private SerialPort serialPort;
    private InputStream serialIn;
    private OutputStream serialOut;
    private ArrayList<SerialListener> serialListeners;
    private int majorFirmwareVersion;
    private int minorFirmwareVersion;
    private int prmType;
    private int pllStep = 12500;    
    
    public SerialControler() {
        this.serialListeners = new ArrayList();
    }
    
    @Override
    public int connectPRM(String port) throws SerialPortException {
        this.openSerialPort(port);        
        return 0;
    }

    @Override
    public void disconnectPRM() {
        this.connected = false;
        try {
            serialIn.close();
            serialOut.close();
            this.serialPort.close();
        }
        catch(Exception e) { }
    }

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
        this.waitChar(':', SerialControler.serialTimeout);
        String sFreq = Integer.toString(rxfreq, 16);
        while(sFreq.length() < 4)
            sFreq = "0" + sFreq;
        this.send(sFreq);
        this.waitChar(':', SerialControler.serialTimeout);
        sFreq = Integer.toString(txfreq, 16);
        while(sFreq.length() < 4)
            sFreq = "0" + sFreq;
        this.send(sFreq);
        this.waitChar('>', SerialControler.serialTimeout);
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
        this.waitChar(':', SerialControler.serialTimeout);
        String sChan = Integer.toString(level);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', SerialControler.serialTimeout);
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
        this.waitChar(':', SerialControler.serialTimeout);
        String sChan = Integer.toString(channel);
        if (sChan.length() == 1)
            sChan = "0"+sChan;
        this.send(sChan);
        this.waitChar('>', SerialControler.serialTimeout);
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
        this.waitChar(':', SerialControler.serialTimeout);
        String sMode = Integer.toHexString(mode).toUpperCase();
        if (sMode.length() == 1)
            sMode = "0"+sMode;
        this.send(sMode);
        this.waitChar('>', SerialControler.serialTimeout);
    }

    @Override
    public byte[] readEEPROM(int adress, int length) {
        return null;
    }

    @Override
    public byte[] readRAM(int adress, int length) {
        return null;
    }

    @Override
    public void writeRAM(byte[] data, int adress) {
        
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
    
    private void openSerialPort(String port) throws SerialPortException {
            try {
                CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
                if (portIdentifier.isCurrentlyOwned()) {
                    System.out.println("Error: Serial port is currently in use");
                } else {
                    CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
                    if (commPort instanceof SerialPort) {
                        serialPort = (SerialPort) commPort;
                        serialPort.setSerialPortParams(4800, SerialPort.DATABITS_7, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
                        serialIn = serialPort.getInputStream();
                        serialOut = serialPort.getOutputStream();
                        
                        String ident = this.sendCommand("v");
                        if (ident == null)
                            throw new SerialPortException("PRM80 not detected");
                        if (ident.contains("PRM8060"))
                            this.prmType = PRMControler.PRM8060;
                        else if (ident.contains("PRM8070"))
                            this.prmType = PRMControler.PRM8070;
                        else
                            throw new SerialPortException("Unknow PRM80 device");
                        this.majorFirmwareVersion = Integer.parseInt(ident.substring(9, 10));
                        this.minorFirmwareVersion = Integer.parseInt(ident.substring(11, 12));                        
                        this.connected = true;
                    } else {
                        throw new SerialPortException("Invalid serial port name.");
                    }
                }
            } catch (IOException ex) {
                throw new SerialPortException("I/O error");
            } catch (UnsupportedCommOperationException ex) {
                throw new SerialPortException("Unsupported serial port parameters");
            } catch (PortInUseException ex) {
                throw new SerialPortException("Serial port in use");
            } catch (NoSuchPortException ex) {
                throw new SerialPortException("Serial port doesn't exist");
                
            }
    }
    
    private String sendCommand (String command) {
        return this.sendCommand(command, SerialControler.serialTimeout);
    }
    private String sendCommand (String command, int waitDuration) {
        this.send(command);
        return waitCommandAnswer(waitDuration);
    }
    
    private void send(String data) {
        try {            
            this.serialOut.write(data.getBytes());
            Iterator<SerialListener> i = this.serialListeners.iterator();
            while (i.hasNext()) {
                i.next().dataSent(data);
            }
        } catch (IOException ex) {
            Logger.getLogger(SerialControler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String waitCommandAnswer(int waitTime) {
        return this.waitChar('>', waitTime);
    }
    
    private String waitChar(char c, int waitTime) {
        StringBuffer rx = new StringBuffer();
        try {
            if (waitTime > 0) {
                this.serialPort.enableReceiveTimeout(waitTime);
            } else {
                this.serialPort.disableReceiveTimeout();
            }
            try {
                byte[] b = new byte[1];

                do {
                    b[0] = (byte) this.serialIn.read();
                    if (b[0] == -1) {
                        return null;
                    }
                    String s = new String(b);
                    rx.append(s);
                    Iterator<SerialListener> i = this.serialListeners.iterator();
                    while (i.hasNext()) {
                        i.next().dataReceived(s);
                    }
                } while (b[0] != c);
            } catch (IOException ex) {
                Logger.getLogger(SerialControler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return rx.toString();
        } catch (UnsupportedCommOperationException ex) {
            Logger.getLogger(SerialControler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rx.toString();
    }
    
    public void addSerialListener(SerialListener list) {
        this.serialListeners.add(list);
    }
    public void removeSerialListener(SerialListener list) {
        this.serialListeners.remove(list);
    }
    public void removeSerialListeners() {
        this.serialListeners.clear();
    }

    public MemoryImage getMemoryImage() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
