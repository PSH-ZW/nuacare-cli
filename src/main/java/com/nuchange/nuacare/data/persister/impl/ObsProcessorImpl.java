package com.nuchange.nuacare.data.persister.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuchange.nuacare.data.persister.ObsProcessor;
import com.nuchange.psiutil.model.FormConcept;
import com.nuchange.psiutil.model.FormControl;
import com.nuchange.psiutil.model.Forms;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;


@Component
public class ObsProcessorImpl extends JdbcDaoSupport implements ObsProcessor {

    final static Logger logger = Logger.getLogger(ObsProcessorImpl.class);

    private Map<String, String> formIdMap = new HashMap<>();
    private Map<Integer, String> conceptUuidMap = new HashMap<>();
    private Map<Integer, List<Integer>> conceptObsMap = new HashMap<>();
    private String locationName = "Bahmni^";
    private String versionString = ".1/";
    List<String> batchSqls = new ArrayList<>();
    Map<Integer, String> obsFnspMap = new HashMap<>();
    //to remove after check

    List<String> addMoreFields = new ArrayList<>();
    Map<String,String> sectionFields = new HashMap<>();
//    List<String> addMoreFields = Arrays.asList(
//            "4ace859e-8796-401d-862d-00128d4ab56a"//AIVCdateOfPrevEpisode
//            ,"7455945d-dee5-486a-be35-460a1a27d4f6"//art1of2
//            ,"5391d829-019e-4997-83b6-5a1d274f9a59"//art1of2
//            ,"49382c2c-62f5-4836-ae4b-2f861c1a6ec6"//BJE
//            ,"0c943c82-748b-4259-ab39-a4c406dc8db3"//ProHIVTestCounselling
//            ,"c2629027-05a9-4da2-af41-485fa4ea1d14"//skin
//            ,"7c234036-6e09-49e3-8009-33535f26cb9c"//skin
//            ,"e58608f3-a90e-11e9-b7ed-6c2b59806788"//viac1
//            ,"e58616df-a90e-11e9-b7ed-6c2b59806788"//viac2
//            ,"e5862472-a90e-11e9-b7ed-6c2b59806788"//viac3
//            ,"e58aeec5-a90e-11e9-b7ed-6c2b59806788"//viac4
//            ,"e58afdc0-a90e-11e9-b7ed-6c2b59806788"//viac5
//
//            ,"9b8aaa8a-3ce2-11ea-befb-6c2b59806788"//adherenceCounsellingDetails
//            ,"9b8ab82a-3ce2-11ea-befb-6c2b59806788"//adherenceCounsellingDrugs
//            ,"9b8cf69e-3ce2-11ea-befb-6c2b59806788"//adherenceCounsellingPercent
//            ,"9b8ce91d-3ce2-11ea-befb-6c2b59806788"//adherenceCounsellingDays
//            ,"9b8cdbd8-3ce2-11ea-befb-6c2b59806788"//adherenceCounsellingTabletsSuppliedVisit
//            ,"9b8cceb0-3ce2-11ea-befb-6c2b59806788"//remainingTablets
//            );
//
//    List<String> sectionFields = Arrays.asList(
//            //ART obs
//            "c37bd733-3f10-11e4-adec-0800271c1b75" //Temp
//            ,"c36e9c8b-3f10-11e4-adec-0800271c1b75" //Systolic
//            ,"c378f635-3f10-11e4-adec-0800271c1b75" //Diastolic
//            ,"c36af094-3f10-11e4-adec-0800271c1b75" //Pulse
//            ,"c3838414-3f10-11e4-adec-0800271c1b75" //SPO2
//            ,"c37d3f27-3f10-11e4-adec-0800271c1b75" //RR
//            ,"c367d9ee-3f10-11e4-adec-0800271c1b75"//BMI
//            //FPS Counselling
//            ,"20e7b885-8ab9-42dd-90ee-73b9acca00d1"//Systolic
//            ,"0a7aaf78-b479-40cb-8ba6-644d54ea1ba1"//Diastolic
//            //FP Continuation
//            ,"4a9ff8b4-4bdd-4088-a98b-0d03b770c95f"//Systolic
//            ,"fef350ce-d753-4696-925d-7c1859bf866a"//Diastolic
//            //FP Initial
//            ,"bb885092-2b66-436d-a633-5f216d033985"//Abnormality
//            ,"16f1edf2-328f-4457-a3d3-780baaedecb6"//Physical spec
//            ,"995498e9-da55-448e-a2cb-0a41002147f3"//Weight
//            ,"581a3cd1-5656-4125-883c-a8a98994de84"//Systolic
//            ,"902fda49-62a0-418b-ae27-de931d856ded"//Diastolic
//            ,"2f3c5cb7-08bb-4f32-af23-51a50bb12727"//Temp
//            ,"0da40b30-7e3a-4815-93d7-f1bdaa36006e"//P
//            ,"e7ba670e-a4bb-402c-b2f3-6d2652d6a544"//R
//            ,"05e2b7cc-2c46-4911-9d42-391aa82dc17d"//Conditions
//            ,"1c4e12d5-494d-4a9e-9a72-b0b730a9ded9"//Other
//            ,"cd7a66f0-5942-4f77-8bc5-78920a0fc5dd"//Size
//            ,"b5ebb210-98d1-426d-a9ec-da4cfe97e6b1"//Position
//            ,"5b635273-c63f-45fd-92a2-eb9294d3ff82"//Comment
//            //COSD
//            ,"f8a11c97-4fef-426b-bea1-1db843a3d631"//Temperature
//            ,"d7080f6b-365d-44a7-80e1-927adc95a466"//Systolic
//            ,"c71f5263-669b-474a-be6b-c6cc0af72df0"//Diastolic
//            ,"1c013217-5256-4278-a4f6-5f49b45cc158"//Blood Sugar
//            ,"d67aa88a-1e11-4e62-a803-4991d1fdb5df"//BMI
//            //VIAC
//            ,"e57f3426-a90e-11e9-b7ed-6c2b59806788"//Systolic
//            ,"e57f5dee-a90e-11e9-b7ed-6c2b59806788"//Diastolic
//            ,"e57efdc6-a90e-11e9-b7ed-6c2b59806788" //wgt
//            ,"e57f0af9-a90e-11e9-b7ed-6c2b59806788"//T
//            ,"e57f788a-a90e-11e9-b7ed-6c2b59806788"//P
//            ,"e58608f3-a90e-11e9-b7ed-6c2b59806788"//viac1
//            ,"e58616df-a90e-11e9-b7ed-6c2b59806788"//viac2
//            ,"e5862472-a90e-11e9-b7ed-6c2b59806788"//viac3
//            ,"e58aeec5-a90e-11e9-b7ed-6c2b59806788"//viac4
//            ,"e58afdc0-a90e-11e9-b7ed-6c2b59806788"//viac5
//    );

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    private TransactionTemplate transactionTemplate;


    @Autowired
    ObsProcessorImpl(DriverManagerDataSource dataSource, TransactionTemplate transactionTemplate){
        super();
        this.setDataSource(dataSource);
        this.setTransactionTemplate(transactionTemplate);
    }

    @Override
    public void migrateForms(String path, String folder) {
        //convert forms --json "/home/nuchanger/Documents/PSI/formdata/uuid.json" --folder "/home/nuchanger/Documents/PSI/formdata/latest/"
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            logger.info("Starting processing");
            Map<String, String> map = objectMapper.readValue(new File(path), Map.class);
            for(String conceptUuid : map.keySet()){
                if(conceptUuid.equals("7f739a61-3f47-4417-8de7-91b21e24c047")){
                    migrateProgramForm(conceptUuid, folder + map.get(conceptUuid));
                }
                else{
                    migrateForm(conceptUuid, folder + map.get(conceptUuid));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFormNameSpaceAndPath(Integer conceptId){
        if (conceptId != null) {
            String conceptUuid = conceptUuidMap.get(conceptId);
            if(conceptUuid!=null) {
                return formIdMap.get(conceptUuid);
            }
            String sql = "select uuid from concept where concept_id = ?";
            String uuid = getJdbcTemplate().queryForObject(
                    sql, new Object[] { conceptId }, String.class);
            conceptUuidMap.put(conceptId, uuid);
            return formIdMap.get(uuid);
        }
        return null;
    }

    public void generateMap(String path){
        logger.info("Generating map for form " + path);
        JsonNode array = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            array = objectMapper.readValue(new File(path), JsonNode.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        parseForm(array);

        //sectionFields
        //Fields created with in section (instead of obs group) should not obs_group_id.
        //We also store map of concepts that can have abnormal value ( BP - Abnormal )
        //addMoreFields
        //Fields having add more option will require extra processing to generate form name space and path.
        try {
            JsonNode concepts = objectMapper.readValue(this.getClass().getClassLoader().getResource("concepts.json"), JsonNode.class);
            sectionFields = objectMapper.readValue(concepts.get("sectionFields").toString(),Map.class);
            addMoreFields = objectMapper.readValue(concepts.get("addMoreFields").toString(), List.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void parseForm(JsonNode array){
        JsonNode resources = array.get("formJson").get("resources");
        String version = array.get("formJson").get("version").asText();
        if(!StringUtils.isEmpty(version)){
            versionString = "." + version + "/";
        }
        String value = resources.get(0).get("value").toString();
        ObjectMapper mapper = new ObjectMapper();
        Forms c = null;
        try {
            c = mapper.readValue(value.replace("\\\"","\"").replace("\\\\","\\").replaceAll("^\"|\"$", ""), Forms.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(FormControl control : c.getControls()){
            parseObj(control, c.getName());
        }
    }

    private void parseObj(FormControl control, String formName) {
        String formNameSpaceAndPath = locationName + formName + versionString +control.getId()+"-0";
        if(control.getConcept()!=null) {
            formIdMap.put(control.getConcept().getUuid(), formNameSpaceAndPath);
        }
        if(!CollectionUtils.isEmpty(control.getControls())){
            for(FormControl innerControl : control.getControls()){
                if(innerControl.getControls()!=null){
                    parseObj(innerControl, formName);
                }
                else{
                    FormConcept concept = innerControl.getConcept();
                    if(concept!=null){
                        //Bahmni^testform3.2/2-0
                        formNameSpaceAndPath = locationName + formName + versionString +innerControl.getId()+"-0";
                        formIdMap.put(concept.getUuid(), formNameSpaceAndPath);
                        if(concept.getUuid().equals("9d472fc0-ef3b-45b3-a44b-b1d16d60d44f")){
//                            System.out.println("next");
                        }
                    }
                }
            }
        }
        else{
            FormConcept concept = control.getConcept();
            if(concept!=null){
                //Bahmni^testform3.2/2-0
                formNameSpaceAndPath = locationName + formName + versionString +control.getId()+"-0";
                formIdMap.put(concept.getUuid(), formNameSpaceAndPath);
            }
        }
    }

    @Override
    public void migrateForm(String conceptUuid, String path){
//        convert form --conceptUuid ef4a6b27-aa15-11e9-8474-6c2b59806858 --json "/home/nuchanger/Documents/PSI/formdata/latest/Viac Form Template 8681_1.json"
        Integer conceptId = getConceptId(conceptUuid);
        generateMap(path);
        logger.info("Processing form " + path);
        String sql = "select obs_id from obs where concept_id = ? and voided = false ;";
        final List<Map<String, Object>> observations = getJdbcTemplate().queryForList(
                sql, new Object[]{conceptId});
        List<String> obsId = new ArrayList<>();
        for(Map<String, Object> map : observations){
            obsId.add(map.get("obs_id").toString());
        }
        logger.info("count : " + observations.size());
        updateMembers(obsId);
        updateSql(conceptObsMap, batchSqls);
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
        String updateSql = "update obs set obs_group_id = null where obs_group_id in ( " + sb.toString() + " );";
        String deleteSql = "delete from obs where concept_id = " + conceptId + " and voided = false ;";
        if(sb.length()>0) {
            batchSqls.add(updateSql);
        }
        updateAddMoreValues();
        removeConceptFromTemplateSet(conceptId);
        removeObsGroupId();
        batchSqls.add(deleteSql);
        logger.info("Running update queries for form " + path);
        getJdbcTemplate().batchUpdate(batchSqls.toArray(new String[0]));
        batchSqls.clear();
        conceptUuidMap.clear();
        conceptObsMap.clear();
        formIdMap.clear();
        logger.info("Completed processing form " + path);
    }

    private void updateMembers(List<String> obsId){
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
        String sql = "select obs_id, concept_id, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, " +
                "value_numeric, value_modifier, value_text, value_complex from obs where obs_group_id in (" + sb.toString() + ")";
        List<Map<String, Object>> members = new ArrayList<>();
        if(obsId.size()>0){
            members = getJdbcTemplate().queryForList(sql );
        }
        List<String> memberId  = new ArrayList<>();
        for(Map<String, Object> obs : members){
            if(obs.get("value_group_id") == null && obs.get("value_coded") == null && obs.get("value_coded_name_id") == null &&
                    obs.get("value_drug") == null && obs.get("value_datetime") == null && obs.get("value_modifier") ==null &&
                    obs.get("value_text") == null && obs.get("value_complex") == null  && obs.get("value_numeric") ==null){
                memberId.add((obs.get("obs_id").toString()));
            }
            Integer conceptId = (Integer) obs.get("concept_id");
            Integer obs_id = (Integer) obs.get("obs_id");

            if(!conceptObsMap.containsKey(conceptId)){
                conceptObsMap.put(conceptId, new ArrayList<Integer>());
            }
            List<Integer> obsList = conceptObsMap.get(conceptId);
            obsList.add(obs_id);
        }
        if(!CollectionUtils.isEmpty(memberId)){
            updateMembers(memberId);
        }
    }

    private void updateSql(Map<Integer, List<Integer>> obsId, List<String> batchSqls){

        for(Integer key : obsId.keySet()){
            List<Integer> obsGrp = obsId.get(key);
            String fnsp = getFormNameSpaceAndPath(key);

            String sep = ",";
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < obsGrp.size(); i++) {

                sb.append(obsGrp.get(i));

                // if not the last item
                if (i != obsGrp.size() - 1) {
                    sb.append(sep);
                }
            }

            if(!StringUtils.isEmpty(fnsp)){
                String sql = "update obs set form_namespace_and_path = \"" + fnsp + "\" where obs_id in ( " + sb.toString() + ");";
                batchSqls.add(sql);
            }
            else{
                String sql = "update obs set voided = true , void_reason = \"migration\" where obs_id in ( " + sb.toString() + ");";
                batchSqls.add(sql);
            }
        }
    }

    private void updateAddMoreValues(){
        for(String uuid : addMoreFields) {
            if (conceptUuidMap.containsValue(uuid)) {
                Integer conceptId = getKey(conceptUuidMap, uuid);
//            String fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0","-");
                String fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0", "-");
                String sql = "select * from obs where concept_id = " + conceptId + " and voided = false ";
                List<Map<String, Object>> obs = getJdbcTemplate().queryForList(sql, new Object[]{});
                Map<Integer, List<Integer>> encounterObsMap = new HashMap<>();
                for (Map<String, Object> o : obs) {
                    Integer obsId = Integer.parseInt(o.get("obs_id").toString());
                    Integer encounterId = Integer.parseInt(o.get("encounter_id").toString());
                    if (!encounterObsMap.containsKey(encounterId)) {
                        encounterObsMap.put(encounterId, new ArrayList<Integer>());
                    } else {
                        Integer index = (encounterObsMap.get(encounterId).size());
                        sql = "update obs set form_namespace_and_path = \"" + fnsp + index + "\" where obs_id = " + obsId;
                        batchSqls.add(sql);
                    }
                    encounterObsMap.get(encounterId).add(obsId);
                }
            }
        }
    }


    @Override
    public void migrateProgramForm(String conceptUuid, String path) {
        //convert programs form --conceptUuid 7f739a61-3f47-4417-8de7-91b21e24c047 --json "/home/nuchanger/Documents/PSI/formdata/latest/Programs 6475_1.json"
        Integer conceptId = getConceptId(conceptUuid);
        generateMap(path);
        Map<Integer, List<Integer>> encounterObsMap = new HashMap<>();
        String sql = "select * from obs where concept_id = ? and voided = false ;";
        final List<Map<String, Object>> observations = getJdbcTemplate().queryForList(
                sql, new Object[]{conceptId});
        List<String> obsId = new ArrayList<>();
        for(Map<String, Object> map : observations){
            Integer id = Integer.parseInt(map.get("obs_id").toString());
            Integer encounterId = Integer.parseInt(map.get("encounter_id").toString());
            String fnsp="";
            if (!encounterObsMap.containsKey(encounterId)) {
                encounterObsMap.put(encounterId, new ArrayList<Integer>());
                encounterObsMap.get(encounterId).add(id);
                Integer index = (encounterObsMap.get(encounterId).size());
                fnsp = getFormNameSpaceAndPath(conceptId).replace("-0","-");
                String val = fnsp + (index-1);
                obsFnspMap.put(id,val);
            } else {
                encounterObsMap.get(encounterId).add(id);
                Integer index = (encounterObsMap.get(encounterId).size());
                fnsp = formIdMap.get(conceptUuidMap.get(conceptId)).replace("-0","-");
                String val = fnsp + (index-1);
                obsFnspMap.put(id,val);
            }
            sql = "update obs set form_namespace_and_path = \"" + obsFnspMap.get(id) + "\" where obs_id = " + id + ";";
            batchSqls.add(sql);
            obsId.add(map.get("obs_id").toString());
        }
        updateProgramMembers(obsId);
        updateProgramsSql(conceptObsMap, batchSqls);
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsId.size(); i++) {

            sb.append(obsId.get(i));

            // if not the last item
            if (i != obsId.size() - 1) {
                sb.append(sep);
            }
        }
//        String updateSql = "update obs set obs_group_id = null where obs_group_id in ( " + sb.toString() + " );";
//        String deleteSql = "delete from obs where concept_id = " + conceptId + " and voided = false ;";
//        if(sb.length()>0) {
//            batchSqls.add(updateSql);
//        }
        updateAddMoreValues();
//        batchSqls.add(deleteSql);
        removeConceptFromTemplateSet(conceptId);
        if(batchSqls.size()>0) {
            logger.info("Running update queries for form " + path);
            getJdbcTemplate().batchUpdate(batchSqls.toArray(new String[batchSqls.size()]));
        }
        batchSqls.clear();
        conceptUuidMap.clear();
        conceptObsMap.clear();
        formIdMap.clear();
    }

    private void updateProgramMembers(List<String> obsIdList){
        String sep = ",";
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < obsIdList.size(); i++) {

            sb.append(obsIdList.get(i));

            // if not the last item
            if (i != obsIdList.size() - 1) {
                sb.append(sep);
            }
        }
        String sql = "select obs_id, concept_id, value_group_id, value_coded, value_coded_name_id, value_drug, value_datetime, " +
                "value_numeric, value_modifier, value_text, value_complex, obs_group_id from obs where obs_group_id in (" + sb.toString() + ")";
        List<Map<String, Object>> members = new ArrayList<>();
        if(obsIdList.size()>0){
            members = getJdbcTemplate().queryForList(sql );
        }
        List<String> memberId  = new ArrayList<>();
        for(Map<String, Object> obs : members){
            if(obs.get("value_group_id") == null && obs.get("value_coded") == null && obs.get("value_coded_name_id") == null &&
                    obs.get("value_drug") == null && obs.get("value_datetime") == null && obs.get("value_modifier") ==null &&
                    obs.get("value_text") == null && obs.get("value_complex") == null  && obs.get("value_numeric") ==null){
                memberId.add((obs.get("obs_id").toString()));
            }
            Integer conceptId = (Integer) obs.get("concept_id");
            Integer obsId = (Integer) obs.get("obs_id");

            if(!conceptObsMap.containsKey(conceptId)){
                conceptObsMap.put(conceptId, new ArrayList<Integer>());
            }
            List<Integer> obsList = conceptObsMap.get(conceptId);
            obsList.add(obsId);

            Integer obsGroupId = Integer.parseInt(obs.get("obs_group_id").toString());
            String value = obsFnspMap.get(obsGroupId);
            obsFnspMap.put(obsId, value);
        }
        if(!CollectionUtils.isEmpty(memberId)){
            updateMembers(memberId);
        }
    }

    private void updateProgramsSql(Map<Integer, List<Integer>> obsId, List<String> batchSqls){

        for(Integer key : obsId.keySet()){
            List<Integer> obsGrp = obsId.get(key);
            String fnsp = getFormNameSpaceAndPath(key);
            for (int i = 0; i < obsGrp.size(); i++) {
                Integer id = obsGrp.get(i);
                String pre = obsFnspMap.get(id) + '/' + fnsp.substring(fnsp.lastIndexOf('/')+1);
                String sql = "update obs set form_namespace_and_path = \"" + pre + "\" where obs_id = " + id + ";";
                batchSqls.add(sql);
            }
        }
    }

    private Integer getKey(Map<Integer, String> conceptUuidMap, String uuid ){
        for (int key : conceptUuidMap.keySet()) {
            if (conceptUuidMap.get(key).equals(uuid)) {
                return  key;
            }
        }
        return null;
    }

    private void removeObsGroupId(){
        for(String uuid : sectionFields.keySet()){
            if(conceptUuidMap.containsValue(uuid)){
                String updateSql = "update obs set obs_group_id = null where concept_id = " + getKey(conceptUuidMap, uuid)+";";
                batchSqls.add(updateSql);

                String abnormalConceptUuid = sectionFields.get(uuid);
                if(abnormalConceptUuid!=null){
                    String abnormalObsQuery =  "(select od.obs_id from obs od inner join obs oa on od.obs_group_id = oa.obs_group_id and " +
                            "od.concept_id = " + getKey(conceptUuidMap, uuid) +
                            " and oa.concept_id = " + getKey(conceptUuidMap, abnormalConceptUuid) + " where oa.value_coded = 1 and oa.voided = false) " ;
                    List<String> abnormalObs = getJdbcTemplate().queryForList(abnormalObsQuery, String.class);
                    if(!CollectionUtils.isEmpty(abnormalObs)){
                        String sep = ",";
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < abnormalObs.size(); i++) {
                            sb.append(abnormalObs.get(i));
                            // if not the last item
                            if (i != abnormalObs.size() - 1) {
                                sb.append(sep);
                            }
                        }
                        updateSql = "update obs set interpretation = 'ABNORMAL' where obs_id in (" + sb.toString() + " ) ";
                        batchSqls.add(updateSql);
                    }
                }
            }
        }
    }

    private void removeConceptFromTemplateSet(Integer conceptId){
        String query = "select count(*) from concept_set where concept_id  = ? and concept_set = (select c.concept_id from concept c " +
                "inner join concept_name cn on cn.concept_id  = c.concept_id where cn.name = 'All Observation templates' and cn.locale = 'en' and locale_preferred = true);";
        Integer count = getJdbcTemplate().queryForObject(
                query, new Object[] { conceptId }, Integer.class);
        if(count!=0){
            batchSqls.add("delete from concept_set where concept_id = " + conceptId + " and concept_set = (select c.concept_id from concept c " +
                    "inner join concept_name cn on cn.concept_id  = c.concept_id where cn.name = 'All Observation templates' and cn.locale = 'en' and locale_preferred = true);");
        }
    }

    private Integer getConceptId(String conceptUuid){
        if(StringUtils.isEmpty(conceptUuid)){
            throw new RuntimeException("Concept uuid cannot be null");
        }
        String query = "select concept_id from concept where uuid  = ? ";
        Integer conceptId = getJdbcTemplate().queryForObject(
                query, new Object[] { conceptUuid }, Integer.class);
        if(conceptId==null){
            throw new RuntimeException("Concept id not found for uuid " + conceptUuid);
        }
        return conceptId;
    }

}
