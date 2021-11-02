package com.nuchange.nuacare.data.persister;

import java.util.Map;

/**
 * Created by sandeepe on 26/02/16.
 */
public interface CSVDataPersister {
    void updateCSV(String filePath, String validate) throws Exception;
    void setProcessor(LineProcessor processor);

}
