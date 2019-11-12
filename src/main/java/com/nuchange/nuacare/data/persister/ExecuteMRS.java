package com.nuchange.nuacare.data.persister;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by sandeepe on 14/03/16.
 */
public class ExecuteMRS extends JdbcDaoSupport{
    private static final String CASHPOINT_PREFIX = "CashPoint";

    public void executeSQLFile(String file) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                int update = getJdbcTemplate().update(line);
                if (update<1)
                    System.out.println("Executing :" + line + "Result :"+update);
            }
        }
        System.out.println("Done");
    }
}
