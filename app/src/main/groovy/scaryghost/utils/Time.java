package scaryghost.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manipulates times from a days, hours, minutes, and seconds perspective
 * @author etsai
 */
public class Time {
    private static Pattern timePat= Pattern.compile("(\\d+) days (\\d{2}):(\\d{2}):(\\d{2})");
    
    /**
     * Converts seconds into the D days HH:MM:SS format
     * @param seconds   Number of seconds
     * @return String representation of the conversion
     */
    public static String secToStr(long seconds) {
        int sec= (int) (seconds % 60);
        int minutes= (int) (seconds / 60) % 60;
        int hours= (int) (seconds / 3600) % 24;
        int days= (int) ((seconds / 3600) / 24);
        
        return String.format("%d days %02d:%02d:%02d", days, hours, minutes, sec);
    }
    
    /**
     * Converts the "D days HH:MM:SS" representation of time into seconds
     * @param time String format of a time object
     * @return Conversion to seconds
     * @throws TimeFormatException If the input string is not in the form "D days HH:MM:SS"
     */
    public static long strToSec(String time) throws TimeFormatException {
        Matcher matcher= timePat.matcher(time);
        
        if (!matcher.matches()) {
            throw new RuntimeException("Input is not in the format D days HH:MM:SS");
        }
        int days= Integer.valueOf(matcher.group(1));
        int hours= Integer.valueOf(matcher.group(2));
        int minutes= Integer.valueOf(matcher.group(3));
        int seconds= Integer.valueOf(matcher.group(4));
        
        return (long)seconds + minutes * 60 + hours * 3600 + days * 86400;
    }
    
    private int days, hours, minutes, seconds;
    
    /**
     * Construct a Time object given the number of seconds
     * @param seconds Number of seconds
     */
    public Time(long seconds) {
        this.seconds= (int) (seconds % 60);
        minutes= (int) (seconds / 60) % 60;
        hours= (int) (seconds / 3600) % 24;
        days= (int) ((seconds / 3600) / 24);
    }
    
    /**
     * Construct a Time object given a string representation 
     * of the time in the format "D days HH:MM:SS".  If the 
     * input string does not match the format, an exception 
     * will be thrown
     * @param timeStr String representation to convert
     * @throws Exception if input does not match pattern
     */
    public Time(String timeStr) throws TimeFormatException {
        Matcher matcher= timePat.matcher(timeStr);
        
        if (!matcher.matches()) {
            throw new RuntimeException("Input is not in the format D days HH:MM:SS");
        } else {
            days= Integer.valueOf(matcher.group(1));
            hours= Integer.valueOf(matcher.group(2));
            minutes= Integer.valueOf(matcher.group(3));
            seconds= Integer.valueOf(matcher.group(4));
        }
    }
    /**
     * Get number of days
     * @return days
     */
    public int getDays() {
        return days;
    }
    /**
     * Get number of hours, between [0, 23]
     * @return hours
     */
    public int getHours() {
        return hours;
    }
    /**
     * Get number of minutes, between [0,59]
     * @return minutes
     */
    public int getMinutes() {
        return minutes;
    }
    /**
     * Get number of seconds, between [0,59]
     * @return seconds
     */
    public int getSeconds() {
        return seconds;
    }
    
    /**
     * Adds the string representation of t2 into the calling object
     * @param t2 Time offset in the form "D days HH:MM:SS"
     * @throws TimeFormatException If the input is not in the form "D days HH:MM:SS"
     */
    public void add(String t2) throws TimeFormatException {
        add(new Time(t2));
    }
    /**
     * Adds the values of t2 into the calling object
     * @param t2 Time offset to add
     */
    public void add(Time t2) {
        seconds+= t2.seconds;
        minutes+= (seconds / 60) + t2.minutes;
        seconds%= 60;
        hours+= (minutes / 60) + t2.hours;
        minutes%= 60;
        days+= (hours / 24) + t2.days;
        hours%= 24;
    }
    
    /**
     * Calculate the number of seconds represented by the object
     * @return Number of seconds
     */
    public long toSeconds() {
        return (long)seconds + minutes * 60 + hours * 3600 + days * 86400;
    }
    /**
     * Generates a string representation of the Time object.  
     * The string is in the form "D days HH:MM:SS"
     * @return Time in the form "D days HH:MM:SS"
     */
    @Override
    public String toString() {
        return String.format("%d days %02d:%02d:%02d", days, hours, minutes, seconds);
    }
}
