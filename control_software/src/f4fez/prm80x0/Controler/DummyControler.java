/*
 * DummyControler.java
 * 
 * Created on 14 déc. 2007, 22:38:06
 * 
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

import java.util.ArrayList;

/**
 *
 * @author Florian
 */
public class DummyControler implements PRMControler{

    private static int ramAreaFreqAdress = 0x100;
    private static int ramAreaStateAdress = 0x200;
    
    private static int ramChanAdress = 0x010;
    private static int ramSquelchAdress = 0x011;
    private static int ramModeAdress = 0x012;
    private static int ramMaxChanAdress = 0x013;
    
    private static int localOscillatorFrequency = 6000000;
    protected static int IF = 21400000;
    
    private boolean connected;
    
    private int volume = 5;
    
   
    private int power;
    
    private int pllRefCounter;
    private int pllCounter;
    private int rxFreq;
    private int txFreq;
    
    private byte[] ram;
    private byte[] eeprom;
    
    private MemoryImage image;
    
    @Override
    public int connectPRM(String port) {
        this.connected = true;
        image = new MemoryImage();
        this.resetPRM();        
        return DummyControler.PRM8060;        
    }

    @Override
    public void disconnectPRM() {
        this.connected = false;
    }

    @Override
    public int getPRMState() {
        return DummyControler.STATE_NORMAL;
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
        assert volume > 16;
        this.volume = volume;
    }

    @Override
    public int readSquelch() {
        return this.ram[DummyControler.ramSquelchAdress];
    }

    @Override
    public void writeSquelch(int level) {
        assert level > 15;
        this.ram[DummyControler.ramSquelchAdress] = (byte) level;
    }

    @Override
    public int getCurrentChannel() {
        return this.ram[DummyControler.ramChanAdress];
    }

    @Override
    public void setCurrentChannel(int channel) {
        assert channel > this.eeprom[DummyControler.ramMaxChanAdress];
        this.ram[DummyControler.ramChanAdress] = (byte) channel;
        
        int eepromPos = channel*2 + DummyControler.ramAreaFreqAdress;
        
        byte wordHi = this.eeprom[eepromPos+1];
        byte wordLo = this.eeprom[eepromPos];
        int pllWord = ((wordHi & 0xFF) << 8) + (wordLo & 0xFF);
        
        this.setRxPLLFrequecny(pllWord * (DummyControler.localOscillatorFrequency / this.pllRefCounter));
        this.setTxPLLFrequecny(pllWord * (DummyControler.localOscillatorFrequency / this.pllRefCounter));
    }

    @Override
    public int getPower() {
        return this.power;
    }

    @Override
    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public byte[] readEEPROM(int adress, int length) {
        byte[] buf = new byte[length];
        for (int i=0; i < length; i++)
            buf[i] = this.eeprom[i+adress];
        return buf;
    }

    /*@Override
    public void writeEEPROM(byte[] data, int adress) {
        for (int i=0; i < data.length; i++)
            this.eeprom[i+adress] = data[i];
    }*/

    @Override
    public byte[] readRAM(int adress, int length) {
        byte[] buf = new byte[length];
        for (int i=0; i < length; i++)
            buf[i] = this.ram[i+adress];
        return buf;
    }

    @Override
    public void writeRAM(byte[] data, int adress) {
        for (int i=0; i < data.length; i++)
            this.ram[i+adress] = data[i];
    }

    @Override
    public void reloadRAM() {
        this.volume = 4;
        this.connected = false;
        this.setPLLStep(12500);
        
        for (int i = 0; i < 2048; i++)
            this.ram[i] = this.eeprom[i];
       
    }

    @Override
    public void resetPRM() {
        ram = new byte[32768];
        eeprom = new byte[2048];

        ArrayList<Integer> freq = new ArrayList<Integer>();
        freq.add(144100000);
        freq.add(145600000);
        freq.add(145800000);
        this.loadVirtualEEprom(freq);
        
        this.reloadRAM();
        
        this.setCurrentChannel(this.ram[DummyControler.ramChanAdress]);
    }

    @Override
    public int getMajorFirmwareVersion() {
        return 1;
    }

    @Override
    public int getMaxChan() {
        return this.eeprom[DummyControler.ramMaxChanAdress];
    }

    @Override
    public void setPLLStep(int frequency) {
        this.pllRefCounter = DummyControler.localOscillatorFrequency / frequency;
    }

    @Override
    public int getPLLStep() {
        return DummyControler.localOscillatorFrequency / this.pllRefCounter;
    }

    @Override
    public void setRxPLLFrequecny(int frequency) {
        this.rxFreq = frequency;
        //this.pllCounter = frequency / this.getPLLStep();
    }

    @Override
    public int getRxPLLFrequency() {
        return this.rxFreq;
        //return this.pllCounter * this.getPLLStep();
    }
        public void setTxPLLFrequecny(int frequency){
        this.txFreq = frequency;
    }

    public int getTxPLLFrequency() {
        return this.txFreq;
    }
    
    private void loadVirtualEEprom(ArrayList<Integer> array) {
        for(int i= 0; i < array.size(); i++) {
            int eepromPos = i*2 + DummyControler.ramAreaFreqAdress;
            int pllWord = array.get(i) / 12500;
            byte wordHi = (byte) (pllWord / 256);
            byte wordLo = (byte) (pllWord - (wordHi * 256) );
            int x = wordLo;
            this.eeprom[eepromPos] = wordLo;
            this.eeprom[eepromPos+1] = wordHi;
        }
        this.eeprom[DummyControler.ramMaxChanAdress] = (byte) array.size();        
        this.eeprom[DummyControler.ramSquelchAdress] = 9;
    }

    @Override
    public void RAM2EEPROM() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void EEPROM2RAM() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMinorFirmwareVersion() {
        return 0;
    }

    public MemoryImage getMemoryImage() {
        return this.image;
    }


}
