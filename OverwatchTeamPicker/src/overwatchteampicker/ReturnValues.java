/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package overwatchteampicker;

/**
 *
 * @author POWERUSER
 */
public class ReturnValues {
    public int yMid;
    public int yHeight;
    public int[] xPoints = new int[4];
    public int[] yPoints = new int[4];
    
    ReturnValues(int yMid, int yHeight) {
        this.yMid = yMid;
        this.yHeight = yHeight;
    }
    
    ReturnValues(int[] xPoints, int[] yPoints) {
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }
    
}