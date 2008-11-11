/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import gnu.io.*;

/**
 *
 * @author f4fez
 */
public class Slot implements Runnable {
    private Socket socket;
    private OutputStream outStream;
    private InputStream inStream;
    private PrintWriter out;
    
    private SerialPort serialPort;
    private InputStream serialIn;
    private OutputStream serialOut;
    
    public static final int CODE_QUIT = 129;
    public static final int CODE_PING = 128;
    
    public Slot(String serialPort) {
        this.openSerialPort(serialPort);
    }
    
    public void connect(Socket socket) {
        try {            
            if (this.socket == null) {
                this.socket = socket;
                this.outStream = this.socket.getOutputStream();
                this.inStream = this.socket.getInputStream();
                new Thread(this).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isFree() {
        return this.socket == null;
    }

    @Override
    public void run() {
        try {
            // Here, start an new thread to wait and
            // send each characters received on the 
            // serial port
            Thread serialRX = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        while (socket != null) {
                            int j = serialIn.read();
                            outStream.write(j);
                            System.out.print(Character.toChars(j));
                        }
                    } catch (IOException ex) {
                        System.out.println("Erreur reading char");
                        System.exit(1);
                    }
                }
            });
            serialRX.start();
            
            // End wait for serial RX
            
            System.out.println("Connection");
            outStream.write("PRM80 server Ok V1.0>".getBytes());
            while (this.socket != null) {
                try {
                    int i = this.inStream.read();
                    if (i == -1) {
                        this.outStream.close();
                        this.inStream.close();
                        this.socket.close();
                        this.socket = null;
                    } else {
                        if (i > 127)
                            this.command(i);
                        else {
                            this.serialOut.write(i);
                            System.out.print(Character.toChars(i));
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
                    this.outStream.close();
                    this.inStream.close();
                    this.socket.close();
                    this.socket = null;
                }
            }
            System.out.println("Déconnexion");
            this.serialPort.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void command(int code) {
        try {
            switch (code) {
                case CODE_PING:
                    this.outStream.write(CODE_PING);
                    break;
                case CODE_QUIT:
                    this.outStream.write(CODE_QUIT);
                    this.inStream.close();
                    this.outStream.close();
                    this.socket.close();
                    this.socket = null;
            }
        } catch (IOException ex) {
            Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    private void openSerialPort(String port) {
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
                        
                    } else {
                        System.out.println("Invalid serial port name.");
                        System.exit(1);
                    }
                }
            } catch (IOException ex) {
                System.out.println("I/O error");
                System.exit(1);
            } catch (UnsupportedCommOperationException ex) {
                System.out.println("Unsupported serial port parameters");
                System.exit(1);
            } catch (PortInUseException ex) {
                System.out.println("Serial port in use");
                System.exit(1);
            } catch (NoSuchPortException ex) {
                System.out.println("Serial port doesn't exist");
                System.exit(1);
                
            }
    }
}
