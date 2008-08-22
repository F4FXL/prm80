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

    private ChannelList channels;
    
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
        return this.image.getRamData(MemoryImage.RAM_ADRESS_SQUELCH);
    }

    @Override
    public void writeSquelch(int level) {
        assert level > 15;
        this.image.setRamData(MemoryImage.RAM_ADRESS_SQUELCH, (byte) level);
    }

    @Override
    public int getCurrentChannel() {
        return this.image.getRamData(MemoryImage.RAM_ADRESS_CHAN);
    }

    @Override
    public void setCurrentChannel(int channel) {
        assert channel > this.image.getEepromData(MemoryImage.RAM_ADRESS_MAX_CHAN);
        this.image.setRamData(MemoryImage.RAM_ADRESS_CHAN, (byte) channel);
        
        int eepromPos = channel*2 + MemoryImage.RAM_AREA_ADRESS_FREQ;
        
        byte wordHi = this.image.getEepromData(eepromPos+1);
        byte wordLo = this.image.getEepromData(eepromPos);
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
    public void reloadRAM() {
        this.volume = 4;
        this.connected = false;
        this.setPLLStep(12500);        
        this.image.copyEeprom2Ram();
       
    }

    @Override
    public void resetPRM() {
        this.channels = new ChannelList();
        ArrayList<Integer> freq = new ArrayList<Integer>();
        freq.add(144100000);
        freq.add(145600000);
        freq.add(145800000);
        this.loadVirtualEEprom(freq);
        
        this.reloadRAM();
        
        this.setCurrentChannel(this.image.getRamData(MemoryImage.RAM_ADRESS_CHAN));
    }

    @Override
    public int getMajorFirmwareVersion() {
        return 1;
    }

    @Override
    public int getMaxChan() {
        return this.image.getRamData(MemoryImage.RAM_ADRESS_MAX_CHAN);
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
            int eepromPos = i*2 + MemoryImage.RAM_AREA_ADRESS_FREQ;
            int pllWord = array.get(i) / 12500;
            byte wordHi = (byte) (pllWord / 256);
            byte wordLo = (byte) (pllWord - (wordHi * 256) );
            int x = wordLo;
            this.image.setEepromData(eepromPos, wordLo);
            this.image.setEepromData(eepromPos+1, wordHi);
        }
        this.image.setEepromData(MemoryImage.RAM_ADRESS_MAX_CHAN, (byte) array.size());        
        this.image.setEepromData(MemoryImage.RAM_ADRESS_SQUELCH, (byte) 5);
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

    public ChannelList getChannels() {
        return this.channels;
    }


}
