package com.nuchange.nuacare.common;

import com.nuchange.nuacare.data.persister.impl.LabResultsProcessor;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UtilTest {

    @Test
    public void testTimeStampUtil() throws ParseException {

        String input = "2018-10-12 10:10:10";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(input);
        Timestamp timestamp = new Timestamp(date.getTime());
        System.out.println(timestamp);
    }
}
