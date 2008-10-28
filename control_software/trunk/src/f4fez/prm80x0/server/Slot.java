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

/**
 *
 * @author f4fez
 */
public class Slot implements Runnable {
    private Socket socket;
    private OutputStream outStream;
    private InputStream inStream;
    private PrintWriter out;
    
    public static String CODE_ = "";
    
    public void connect(Socket socket) {
        try {            
            if (this.socket == null) {
                this.socket = socket;
                this.outStream = this.socket.getOutputStream();
                this.out = new PrintWriter(outStream, true);
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

    public void run() {
        try {
            System.out.println("Connection");
            outStream.write("PRM80 server V1.0>OK\r\n".getBytes());
            while (this.socket != null) {
                try {
                    int i = this.inStream.read();
                    if (i == -1) {
                        this.socket = null;
                    } else {
                        this.outStream.write(i);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
                    this.socket = null;
                }
            }
            System.out.println("Déconnexion");
        } catch (IOException ex) {
            Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
