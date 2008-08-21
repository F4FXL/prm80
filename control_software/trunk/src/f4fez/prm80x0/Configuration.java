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

package f4fez.prm80x0;

import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.prefs.Preferences;

/**
 *
 * @author fmazen
 */
public class Configuration {
    private String serialPort;
    private boolean expertMode;
    private Preferences prefs;
    
    private static String prefRoot = "f4fez/prm80";
    private static String prefSerialPort = "serialPort";
    private static String prefExpertMode = "expertMode";
    
    public Configuration() {
        this.prefs = Preferences.userRoot().node(prefRoot);
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
            if (com.getPortType() == CommPortIdentifier.PORT_SERIAL ) {
                this.serialPort = this.prefs.get(prefSerialPort, com.getName());
                break;
            }
        }
        this.expertMode = this.prefs.getBoolean(Configuration.prefExpertMode, false);
    }            
    
    public void setSerialPort(String port) {
        this.serialPort = port;
        this.prefs.put(prefSerialPort, port);
    }
    
    public String getSerialPort() {
        return this.serialPort;
    }
    
    public boolean isExpertMode() {
        return this.expertMode;
    }
    
    public void setEpertMode(boolean expert) {
        this.prefs.putBoolean(Configuration.prefExpertMode, expert);
    }
}
