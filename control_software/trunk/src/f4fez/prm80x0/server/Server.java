/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.server;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author f4fez
 */
public class Server implements Runnable {

    private int port;
    private ServerSocket socketServer;
    private boolean running;
    private Slot serverSlot;
    
    public Server(int port) {
        try {
            this.port = port;
            socketServer = new ServerSocket(port);
            this.serverSlot = new Slot();
            new Thread(this).start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1)
            System.out.println("Syntax is : server <port>");
        new Server (Integer.parseInt(args[0]));        
    }

    private void denied(Socket socket) {
        OutputStream outStream = null;
        try {
            outStream = socket.getOutputStream();
            PrintWriter out = new PrintWriter(outStream);
            out.print("PRM80 server V1.0>FU\n");
            out.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void run() {
        this.running = true;
        while(this.running) {
            try {
                Socket socket = this.socketServer.accept();
                if (this.serverSlot.isFree())
                    this.serverSlot.connect(socket);
                else
                    this.denied(socket);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
