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
            this.serverSlot = new Slot("/dev/ttyS0");
            new Thread(this).start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

    public static void runServer(String[] args) {
        if (args.length != 2) {
            System.out.println("Paramerters for server are : server <port>");
            System.exit(1);
        }
        new Server (Integer.parseInt(args[1]));        
    }

    
    @Override
    public void run() {
        this.running = true;
        System.out.println("Server started");
        while(this.running) {
            try {
                Socket socket = this.socketServer.accept();
                if (this.serverSlot.isFree())
                    this.serverSlot.connect(socket);
                else
                    new ErrorSlot().connect(socket);
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
