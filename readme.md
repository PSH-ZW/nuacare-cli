#Steps for running CLI
pre-requisites : in psi-util ,mvn clean install -DskipTests should be run for build to complete as it is a
dependent project
1. Run cmd : bash scripts/compile.sh, post this necessary files in target would be available
2. Navigate to nuacare-cli/target/CSVUploader-1.0-SNAPSHOT-bundle.tar and  transfer the file to vm
   extract the contents (tar -xvf CSVUploader-1.0-SNAPSHOT-bundle.tar) to required folder (Note :any of the other bundles
   could be used which starts with CSVUploader-1.0-SNAPSHOT-bundle)
3. To get the Nuacare command line interface run on the box: bash uploader/scripts/run.sh

#CLI interface before flattening
1. Whenever tables for forms are not yet created or forms are updated and new fields are added to it, they are inconsistent
   with existing form tables, to find all conflicts run below command
    1. command : show_conflicts
    2. shows all inconsistent tables with necessary details
    3. (if table needs to be upgraded) a. will display : "form name:" <nameOfTheForm>
       "| missing columns:" <missingColumns> "| Highest Version available:" <highestVersion>
       "command :" <actual command>(This needs to be executed below.)
       (if table needs to be created) b. will diplay : "form name " <nameOfTheForm> "| command :" <actual command>
       (This needs to be executed below.)

2. In continuation from point 2 to resolve the conflicts below command needs to be entered with result available from point 2
   look for (command :fix_form --form_name "AViac Form Template 8681")
    1. command (example) : fix_form --form_name "AViac Form Template 8681"
       (here form name is directly taken from point 2 result needs to be given within "" ,
       If no columns are shown in missing columns the command still needs to be run just to update version).
    2. will show create/alter query for necessary tables to keep back-end consistent.
    3. will return queries run to keep backend consistent.
    4. Important : for the changes to be applied in the database another field  "--dry_run false" needs to be added
       i.e. example command : fix_form --form_name "AViac Form Template 8681" --dry_run false
   #Note :
   1. Command is given in its actual form
      1. should look for "command :" at the end ,for the result ,of each line to be executed after show_conflicts cmd is run
      2. point number 2 : if only the query needs to be displayed without applying changes to backend tables a flag needs to be
         added "--dry_run " (takes boolean field) eg command :
         fix_form --form_name "AViac Form Template 8681" --dry_run true (OR)
         fix_form --form_name "AViac Form Template 8681"
      3. continuing from above point if the changes need to be applied to the backend tables the below command needs to be run eg:
         fix_form --form_name "AViac Form Template 8681" --dry_run false

#Note: paths mentioned are relative


#Prerequisites for table creation from forms
1. As table columns are derived from form concepts their length could easily be long and therefore certain string 
manipulation is being done to reduce this length therefore it is suggested to not repeat form fields or to make them 
unique (Especially for multiselect as this has a higher probability for conflict).
1a. Currently, an exception is thrown from backend when such an issue is encountered with necessary details using which 
after necessary steps are taken to make specified form concept unique the necessary CLI could be rerun.