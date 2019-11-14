package com.nuchange.nuacare.util;

import org.springframework.jdbc.core.RowMapper;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sandeepe on 26/02/16.
 */
public class Utils {

    public static boolean isEmptyList(List list) {
        return list == null || list.size() == 0;
    }

    public static File createDirectoryIfNotExist(String dirName) {
        File fileDir = new File(dirName);
        if (!fileDir.exists()) {
            System.out.println(dirName+" directory does not exists !!! creating directory : " + fileDir.getPath());
            fileDir.mkdirs();
        }
        return fileDir;
    }

    public static RowMapper<List<Object>> getRowMapperByColumnCount(final int columnCount) {
        return new RowMapper<List<Object>>() {
            @Override
            public List<Object> mapRow(ResultSet resultSet, int i) throws SQLException {
                List<Object> objectArrayList = new ArrayList<>();
                for (int count = 1; count <= columnCount; count++) {
                    objectArrayList.add(resultSet.getObject(count));
                }
                return objectArrayList;
            }
        };
    }
}
