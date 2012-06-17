/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.etsai.kfsxtrackingserver;

/**
 * Manipulates times from a DD:HH:MM:SS perspective
 * @author etsai
 */
public class Time {
    private int days, hours, minutes, seconds;
    
    public Time(long seconds) {
        this.seconds= (int) (seconds % 60);
        minutes= (int) (seconds / 60);
        hours= (int) (seconds / 3600) % 24;
        days= (int) ((seconds / 3600) / 24);
    }
    
    public int getDays() {
        return days;
    }
    public int getHours() {
        return hours;
    }
    public int getMinutes() {
        return minutes;
    }
    public int getSeconds() {
        return seconds;
    }
    public Time add(Time t2) {
        seconds+= t2.seconds;
        minutes+= (seconds / 60) + t2.minutes;
        seconds%= 60;
        hours+= (minutes / 60) + t2.hours;
        minutes%= 60;
        days+= (hours / 24) + t2.days;
        hours%= 24;
        
        return this;
    }
}
