package com.nuchange.nuacare.data.persister.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuchange.nuacare.data.persister.LineProcessor;
import org.springframework.stereotype.Component;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Component
public class FormsProcessor extends LineProcessor {

    private final String[] header = {"obs_id", "person_id", "concept_id", "encounter_id", "order_id", "obs_datetime", "location_id", "obs_group_id", "accession_number", "value_group_id", "value_coded", "value_coded_name_id", "value_drug", "value_datetime", "value_numeric", "value_modifier", "value_text", "value_complex", "comments", "creator", "date_created", "voided", "voided_by", "date_voided", "void_reason", "uuid", "previous_version", "form_namespace_and_path", "status", "interpretation"};

    private Map<String, Integer> formIdMap = new HashMap<>();
    private Map<Integer, String> conceptUuidMap = new HashMap<>();

    @Override
    public String processLine(String[] line) {
        formIdMap.put("4cee88f5-b69c-4e75-a7a9-7963a10cf329", 2);
        String id = line[0]; //obs_id
        String conceptId = line[2]; //concept_id
        String coded = line[10];//"value_coded"
        String drug = line[12];//"value_drug"
        String datetime = line[13];//"value_datetime"
        String numeric = line[14];//"value_numeric"
        String text = line[16];//"value_text"
        String complex = line[17];//"value_complex"
        Integer formNameSpaceAndPath = getJsonId(Integer.parseInt(conceptId));
        if (coded != null || drug != null || datetime != null || numeric != null || text != null || complex != null) {
            jdbcTemplate.update("update obs set form_namespace_and_path = " + formNameSpaceAndPath + " where obs_id = " + id);
        }
        System.out.println(line);
        return null;
    }

    public Integer getJsonId(Integer conceptId){
        if (conceptId != null) {
            String conceptUuid = conceptUuidMap.get(conceptId);
            if(conceptUuid!=null) {
                return formIdMap.get(conceptUuid);
            }
            String sql = "select uuid from concept where concept_id = ?";
            String uuid = jdbcTemplate.queryForObject(
                    sql, new Object[] { conceptId }, String.class);
            conceptUuidMap.put(conceptId, uuid);
            return formIdMap.get(uuid);

        }
        return null;
    }
    public void generateMap(String path){
        JsonNode array = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            array = objectMapper.readValue(new File(path), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        formIdMap = parseForm(array);
    }
    static String stripQuotes(String string){
        if (string != null){
//            return string.replaceAll("\"", "");
            String s = string.replaceAll("^\"|\"$", "");
            return s;
        }
        return "";
    }


     Map<String,Integer> parseForm(JsonNode array){
        JsonNode resources = array.get("formJson").get("resources");
        String value = resources.get(0).get("value").toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode c = null;
        try {
            c = mapper.readValue(value.replace("\\", "").replaceAll("^\"|\"$", ""), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonNode controls = c.get("controls");
        return parseObj(controls, formIdMap);
    }

     Map<String, Integer> parseObj(JsonNode controls, Map<String, Integer> formIdMap) {
        int size = controls.size();
        if (size > 0) {
            int i = 0;
            Iterator<JsonNode> iterator = controls.iterator();
            while (iterator.hasNext()) {
                JsonNode jsonNode = iterator.next();
                JsonNode innerControls = jsonNode.get("controls");
                if (innerControls != null && innerControls.size()>0){
                    parseObj(jsonNode, formIdMap);
                }
                else{
//                    JsonNode obsControl = jsonNode.get("type");
//                    if (obsControl != null ){//&& "obsControl".equals(stripQuotes(String.valueOf(obsControl)))) {
                        JsonNode concept = jsonNode.get("concept");
                        if (concept != null){
                            String uuid = String.valueOf(concept.get("uuid")).replace("\"", "");
                            Integer id = Integer.parseInt(jsonNode.get("id").toString().replace("\"", ""));
                            formIdMap.put(uuid, id);
                        }
                }

                if (i>=size){
                    break;
                }
            }
        }
        return formIdMap;
    }
}
