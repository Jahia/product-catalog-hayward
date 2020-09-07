/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.xmlprovider.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * XmlUtils Class.
 */
public class XmlUtils {

    // The Logger
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##");

    /**
     *  Getting the date moving time format
     *
     * @param moving_time
     * @return
     */
    public static String displayMovingTime(String moving_time) {
        int totalSeconds = Integer.parseInt(moving_time);
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        String moving_time_display;
        if (hours != 0) {
            moving_time_display = hours + ":" + displayNumberTwoDigits(minutes) + ":" + displayNumberTwoDigits(seconds);
        } else {
            if (minutes != 0) {
                moving_time_display = minutes + ":" + displayNumberTwoDigits(seconds);
            } else {
                moving_time_display = "" + seconds;
            }
        }
        return moving_time_display;
    }

    /**
     * Getting the start date in format 'E dd/MM/yyyy'
     *
     * @param start_date
     * @return
     * @throws ParseException
     */
    public static String displayStartDate(String start_date) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-M-dd").parse(start_date);
        String start_date_formatted = new SimpleDateFormat("E dd/MM/yyyy").format(date);
        return start_date_formatted;
    }

    /**
     * Getting the distance in decimal format
     *
     * @param distance
     * @return
     */
    public static String displayDistance(String distance) {
        return DECIMAL_FORMAT.format(Double.parseDouble(distance) / 1000);
    }

    /**
     * Getting the number with two digits.
     *
     * @param number
     * @return
     */
    public static String displayNumberTwoDigits(int number) {
        if (number <= 9) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

}
