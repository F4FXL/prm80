/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package f4fez.prm80x0.Controler;

/**
 *
 * @author f4fez
 */
public class SerialControlerTest extends ControlerTest {

    @Override
    public void setUp() {
        this.instance = new SerialControler();        
        this.port = "/dev/ttyS0";
    }

}
