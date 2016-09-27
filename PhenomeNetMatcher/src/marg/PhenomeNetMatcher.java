package marg;

import marg.match.Alignment;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

/**
 * Created by marg27 on 29/05/16.
 */
public class PhenomeNetMatcher {
    //the type 0 for default which means PLAIN_ONTOLOGY_ALIGNMENT
    private int typeAlignment = 0;

    public PhenomeNetMatcher() {
        try {
            Properties properties = new Properties();
            properties.load(new FileInputStream(new File(getConfigurationPath() + "PhenomeNetMatcherConfigurationFile.properties")));
            typeAlignment = Integer.parseInt(properties.getProperty("type_alignment"));
            System.out.println("Type of alignment:"+typeAlignment);
        }catch(Exception e) {
            System.out.println("ERROR: The configuration file was not loaded. PLAIN_ONTOLOGY_ALIGNMENT is executing");
        }
    }

    public static String getConfigurationPath(){
        Logger.getRootLogger().setLevel(Level.OFF);
        return(System.getProperty("user.dir")+File.separator+"conf"+File.separator+"configuration"+File.separator);
        //return(System.getProperty("user.dir")+File.separator+"configuration"+File.separator);
    }

    public PhenomeNetMatcher(int typeAlignment){
        this.typeAlignment = typeAlignment;
        System.out.println("Type of alignment:"+typeAlignment);
    }
    /**
     * matches to ontologies referred to via their filepath and
     * return the File that has been generated as a result.
     */
    public File match(String filepathSourceOnt, String filepathTargetOnt) {
        if((filepathSourceOnt!=null)&&(filepathTargetOnt!=null)){
            try {
                File alignmentFile = match(URI.create(filepathSourceOnt).toURL(),
                                            URI.create(filepathTargetOnt).toURL());
                return alignmentFile;
            }catch(Exception e){
                System.out.println("Error: Impossible to do the matching: "+e.getMessage());
            }
        }
        return(null);
    }


    /**
     * matches to ontologies referred to via their URI and
     * returns the alignment generated directly as a file.
     */
    public File match(URL filepathSourceOnt, URL filepathTargetOnt) {
        if((filepathSourceOnt!=null)&&(filepathTargetOnt!=null)) {
            try {
                System.out.println("source: "+filepathSourceOnt);
                System.out.println("target: "+filepathTargetOnt);
                OntologyAlignmentEngine engine = new OntologyAlignmentEngine(filepathSourceOnt,filepathTargetOnt);
                engine.applyAlignment(typeAlignment);
                Alignment alignment = engine.getAlignment();
                File alignmentFile = File.createTempFile("PhenomeNetMatcher_alignment" + typeAlignment+"_", ".rdf");
                if(alignment!=null) {
                    alignment.saveRDF(alignmentFile.getPath());
                }
                System.out.println(alignmentFile.getPath());
                return(alignmentFile);

            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Error: Impossible to do the matching: "+e.getMessage());
            }
        }
       return(null);
    }

    /**
     * Evaluate the matches.
     */
    public void evaluateAlignment(URL filepathSourceOnt, URL filepathTargetOnt, URL fileAlignments) {
        if((filepathSourceOnt!=null)&&(filepathTargetOnt!=null)&&(fileAlignments!=null)) {
            try {
                OntologyAlignmentEngine engine = new OntologyAlignmentEngine(filepathSourceOnt,filepathTargetOnt);
                engine.applyAlignment(typeAlignment);
                Alignment alignment = engine.getAlignment();
                if(alignment!=null) {
                    engine.evaluateAlignment(fileAlignments);
                    System.out.println(engine.getEvaluation());
                    File alignmentFile = File.createTempFile("alignment_type_"+typeAlignment+"_", ".rdf");
                    alignment.saveRDF(alignmentFile.getPath());
                    System.out.println(alignmentFile.getPath());
                }
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Error: The evaluation was not performed "+e.getMessage());
            }
        }
    }

    /**
     * matches to ontologies referred to via their URL and
     * returns the URL of the file generated locally
     */
    public URL align(URL sourceOnt, URL targetOnt) {
        if((sourceOnt!=null)&&(targetOnt!=null)) {
            try {
                File file = match(sourceOnt, targetOnt);
                if(file!=null) {
                    return (file.toURI().toURL());
                }
            }catch(Exception e) {
                System.out.println("URL is not well-formed: "+e.getMessage());
            }
        }
        return(null);
    }

    public static void main(String args[]) {
        try {
            File hpFile = new File(System.getProperty("user.dir") + "/baseline/HP.rdf");
            File mpFile = new File(System.getProperty("user.dir") + "/baseline/MP.rdf");
            File mphpBaseline = new File(System.getProperty("user.dir") + "/baseline/HP_MP_baseline.rdf");

            PhenomeNetMatcher matcher = new PhenomeNetMatcher(0);
            System.out.println("PLAIN_ONTOLOGY_ALIGNMENT\tHPvsMP");
            //matcher.align(hpFile.toURI().toURL(), mpFile.toURI().toURL());
            matcher.evaluateAlignment(hpFile.toURI().toURL(),mpFile.toURI().toURL(),mphpBaseline.toURI().toURL());


            matcher = new PhenomeNetMatcher(1);
            System.out.println("MAP_ONTOLOGY_ALIGNMENT\tHPvsMP");
            //matcher.align(hpFile.toURI().toURL(), mpFile.toURI().toURL());
            matcher.evaluateAlignment(hpFile.toURI().toURL(),mpFile.toURI().toURL(),mphpBaseline.toURI().toURL());


            matcher = new PhenomeNetMatcher(2);
            System.out.println("FULL_ONTOLOGY_ALIGNMENT\tHPvsMP");
            //matcher.align(hpFile.toURI().toURL(), mpFile.toURI().toURL());
            matcher.evaluateAlignment(hpFile.toURI().toURL(),mpFile.toURI().toURL(),mphpBaseline.toURI().toURL());


            File doid = new File(System.getProperty("user.dir") + "/baseline/DOID.rdf");
            File ordo = new File(System.getProperty("user.dir") + "/baseline/ORDO.rdf");
            File doidordoBaseline = new File(System.getProperty("user.dir") + "/baseline/DOID_ORDO_baseline.rdf");

            matcher = new PhenomeNetMatcher(0);
            System.out.println("PLAIN_ONTOLOGY_ALIGNMENT\tDOIDvsORDO");
            //matcher.align(doid.toURI().toURL(), ordo.toURI().toURL());
            matcher.evaluateAlignment(doid.toURI().toURL(),ordo.toURI().toURL(),doidordoBaseline.toURI().toURL());


            matcher = new PhenomeNetMatcher(1);
            System.out.println("MAP_ONTOLOGY_ALIGNMENT\tDOIDvsORDO");
            //matcher.align(doid.toURI().toURL(), ordo.toURI().toURL());
            matcher.evaluateAlignment(doid.toURI().toURL(),ordo.toURI().toURL(),doidordoBaseline.toURI().toURL());

            matcher = new PhenomeNetMatcher(2);
            System.out.println("FULL_ONTOLOGY_ALIGNMENT\tDOIDvsORDO");
            //matcher.align(doid.toURI().toURL(), ordo.toURI().toURL());
            matcher.evaluateAlignment(doid.toURI().toURL(),ordo.toURI().toURL(),doidordoBaseline.toURI().toURL());

        } catch (Exception e) {

        }
    }
}
