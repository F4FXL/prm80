/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.server;

import java.io.IOException;
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
    private PrintWriter out;
    
    public void connect(Socket socket) {
        try {
            if (this.socket == null) {
                this.socket = socket;
                this.outStream = this.socket.getOutputStream();
                this.out = new PrintWriter(outStream);
                new Thread(this);
            }
        } catch (IOException ex) {
            Logger.getLogger(Slot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isFree() {
        return this.socket == null;
    }

    public void run() {
        while(this.socket != null) {
            
        }
    }
}
