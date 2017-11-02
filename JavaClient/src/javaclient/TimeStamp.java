/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaclient;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author lamkailoon
 */
public class TimeStamp {
    
    //get current date and time
    public static String get(){
        return "[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "]";
    }
}
