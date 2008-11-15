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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fmazen
 */
public class TcpControler extends PRMControler{
    private boolean connected;
    //private SerialPort serialPort;
    private Socket socket;
    private InputStream serialIn;
    private OutputStream serialOut;

    @Override
    public int connectPRM(String server) throws SerialPortException {
        this.openSocket(server);
        return 0;
    }

    @Override
    public void disconnectPRM() {
        this.connected = false;
        try {
            serialIn.close();
            serialOut.close();
            this.socket.close();
        }
        catch(Exception e) { }
    }

    private void openSocket(String server) throws SerialPortException {
        try {
            this.socket = new Socket(server, 8060);        
            this.serialIn = this.socket.getInputStream();
            this.serialOut = this.socket.getOutputStream();
            
            String serverIdent = this.waitCommandAnswer(5000);
            if (!serverIdent.contains("PRM80 server Ok")) {
                serialIn.close();
                serialOut.close();
                this.socket.close();
                throw new SerialPortException("Server identification error");
            }
            String ident = this.sendCommand("v");
            if (ident == null) {
                this.disconnectPRM();
                throw new SerialPortException("PRM80 not detected");                
            }
            if (ident.contains("PRM8060"))
                this.prmType = Controler.PRM8060;
            else if (ident.contains("PRM8070"))
                this.prmType = Controler.PRM8070;
            else {
                this.disconnectPRM();
                throw new SerialPortException("Unknow PRM80 device");
            }                
            this.majorFirmwareVersion = Integer.parseInt(ident.substring(9, 10));
            this.minorFirmwareVersion = Integer.parseInt(ident.substring(11, 12));                        
            this.connected = true;
        } catch (UnknownHostException ex) {
            throw new SerialPortException("Host not found");
        } catch (IOException ex) {
            this.disconnectPRM();
            throw new SerialPortException("Could not connect to host");
        }
    }    

    @Override
    protected void send(String data) {
        try {            
            this.serialOut.write(data.getBytes());
            Iterator<SerialListener> i = this.serialListeners.iterator();
            while (i.hasNext()) {
                i.next().dataSent(data);
            }
        } catch (IOException ex) {
            Logger.getLogger(TcpControler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected String waitChar(char c, int waitTime) {
        StringBuffer rx = new StringBuffer();
        try {
            this.socket.setSoTimeout(waitTime);
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
            return rx.toString();
        } catch (SocketTimeoutException ex) {
                return null;
        } catch (IOException ex) {
                Logger.getLogger(TcpControler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
        }
    }

}
