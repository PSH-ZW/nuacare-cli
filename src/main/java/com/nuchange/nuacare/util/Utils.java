package com.nuchange.nuacare.util;

import java.util.List;

/**
 * Created by sandeepe on 26/02/16.
 */
public class Utils {

    public static boolean isEmptyList(List list) {
        return list == null || list.size() == 0;
    }
}
