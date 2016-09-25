package marg;

import marg.algorithm.AlgorithmAlignment;
import marg.algorithm.AxiomAlignmentAlgorithm;
import marg.match.Alignment;
import org.apache.commons.io.FileUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by marg27 on 01/06/16.
 */

public class OntologyAlignmentEngine {
    public static final int PLAIN_ONTOLOGY_ALIGNMENT=0;
    public static final int MAP_ONTOLOGY_ALIGNMENT=1;
    public static final int FULL_ONTOLOGY_ALIGNMENT=2;

    private URL sourceURL;
    private URL targetURL;

    private Alignment alignment;
    private String evaluation;

    public OntologyAlignmentEngine(URL sourceURL, URL targetURL) {
        this.sourceURL = sourceURL;
        this.targetURL = targetURL;
    }

    public void applyAlignment(int alignmentType) {

        OWLOntology sourceOnto = loadOntology(sourceURL);
        if (sourceOnto == null) {
            System.out.println("Source ontology not loaded");
            System.exit(-1);
        }

        OWLOntology targetOnto = loadOntology(targetURL);
        if (targetOnto == null) {
            System.out.println("Target ontology not loaded");
            System.exit(-1);
        }

        switch (alignmentType) {
            case PLAIN_ONTOLOGY_ALIGNMENT: {
                try {
                    File phenomeNetPlainFile = new File(PhenomeNetMatcher.getConfigurationPath()+"phenomenet-plain.owl");
                    if(phenomeNetPlainFile.exists()) {
                        OWLOntology phenomeNetPlanOnto = loadOntology(phenomeNetPlainFile.toURI().toURL());
                        if (phenomeNetPlanOnto == null) {
                            System.out.println(phenomeNetPlainFile.getPath() + " ontology not loaded");
                            System.exit(-1);
                        }
                        AlgorithmAlignment axiomAlignment = new AxiomAlignmentAlgorithm(sourceOnto, targetOnto, phenomeNetPlanOnto);
                        alignment = axiomAlignment.applyMatch();
                    }else {
                        System.out.println("Error: phenomenet-plain.owl not loaded");
                    }
                }catch(Exception e){
                    System.out.println("Exception: phenomenet-plain.owl not loaded, message: "+e.getMessage());
                }
                return;
            }
            case MAP_ONTOLOGY_ALIGNMENT: {
                try{
                    File phenomeNetMapFile = new File(PhenomeNetMatcher.getConfigurationPath()+"phenomenet-map.owl");
                    if(phenomeNetMapFile.exists()) {
                        OWLOntology phenomeNetMapOnto = loadOntology(phenomeNetMapFile.toURI().toURL());
                        if (phenomeNetMapOnto == null) {
                            System.out.println(phenomeNetMapFile.getPath() + " ontology not loaded");
                            System.exit(-1);
                        }
                        AlgorithmAlignment lexicalAlignment = new AxiomAlignmentAlgorithm(sourceOnto, targetOnto,phenomeNetMapOnto);
                        alignment = lexicalAlignment.applyMatch();
                    }else {
                        System.out.println("Error: phenomenet-map.owl not loaded");
                    }
                }catch(Exception e){
                    System.out.println("Exception: phenomenet-map.owl not loaded, message: "+e.getMessage());
                }
                return;
            }
            case FULL_ONTOLOGY_ALIGNMENT: {
                try{
                    File phenomeNetFullFile = new File(PhenomeNetMatcher.getConfigurationPath()+"phenomenet-full.owl");

                    if(phenomeNetFullFile.exists()) {
                        OWLOntology phenomeNetFullOnto = loadOntology(phenomeNetFullFile.toURI().toURL());
                        if (phenomeNetFullOnto == null) {
                            System.out.println(phenomeNetFullFile.getPath() + " ontology not loaded");
                            System.exit(-1);
                        }
                        AlgorithmAlignment lexicalAlignment = new AxiomAlignmentAlgorithm(sourceOnto, targetOnto,phenomeNetFullOnto);
                        alignment = lexicalAlignment.applyMatch();
                    }else{
                        System.out.println("Error: phenomenet-full.owl not loaded");
                    }
                }catch(Exception e){
                    System.out.println("Exception: phenomenet-full.owl not loaded, message: "+e.getMessage());
                }
            }
        }
    }

    public OWLOntology loadOntology(URL pathOntology) {
        OWLOntology ontology = null;
        try {
            if ((pathOntology != null)) {
                File owlFile = null;
                if(pathOntology.getHost().isEmpty()){//local
                    owlFile = new File(pathOntology.toURI());
                }else {//foreign
                    HttpURLConnection connection = (HttpURLConnection) pathOntology.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.connect();
                    String contentType = connection.getContentType();
                    if(contentType.toLowerCase().contains("owl")){
                        owlFile = File.createTempFile(pathOntology.getFile(), ".owl");
                    }else if(contentType.toLowerCase().contains("obo")){
                        owlFile = File.createTempFile(pathOntology.getFile(), ".obo");
                    }else{
                        owlFile = File.createTempFile(pathOntology.getFile(), ".rdf");
                    }
                    FileUtils.copyURLToFile(pathOntology, owlFile);
                }
                OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
                config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
                FileDocumentSource fSource = new FileDocumentSource(owlFile);
                ontology = manager.loadOntologyFromOntologyDocument(fSource, config);
                return (ontology);
            }
        }catch(Exception e){
            System.out.println("Error: Ontology not loaded");
        }
        return(null);
    }

    public Alignment getAlignment() {
        return (alignment);
    }

    public void evaluateAlignment(URL refFileAlignment) {
        double precision=0.0;
        double recall=0.0;
        double fMeasure=0.0;
        DecimalFormat df = new DecimalFormat("##0.00");
        Alignment reference = new Alignment(refFileAlignment);

        int[] eval = alignment.evaluate(reference);
        int found = alignment.getMappings().size() - eval[1];
        int correct = eval[0];
        int total = reference.getMappings().size();

        precision = (double)correct/found;
        String prc = df.format(Math.round(precision*1000)/10.0) + "%";
        recall = (double)correct/total;
        String rec = df.format(Math.round(recall*1000)/10.0) + "%";
        fMeasure = (double)2*precision*recall/(precision+recall);
        String fms = df.format(Math.round(fMeasure*1000)/10.0) + "%";

        evaluation = "Precision\tRecall\tF-measure\tFound\tCorrect\tReference\n" +
                      prc + "\t\t" + rec + "\t" + fms + "\t\t" + found + " \t" + correct + "\t\t" + total;

    }

    public String getEvaluation() {
        return (evaluation);
    }

}
