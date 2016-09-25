package marg.match;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by marg27 on 14/06/16.
 */
public class Alignment {

    private OWLOntology sourceOnto = null;
    private OWLOntology targetOnto = null;
    private String sourceURI = null;
    private String targetURI = null;
    private ArrayList<Mapping> mappings;
    public Alignment(OWLOntology sourceOnto, OWLOntology targetOnto) {
        this.sourceOnto = sourceOnto;
        this.targetOnto = targetOnto;
        if(sourceOnto.getOntologyID().getOntologyIRI()!=null){
            sourceURI = sourceOnto.getOntologyID().getOntologyIRI().toString();
        }else{
            OWLClass owlClass = sourceOnto.getClassesInSignature().iterator().next();
            String uri = owlClass.getIRI().toURI().toString();
            sourceURI = uri;
            int pos = uri.lastIndexOf("_");
            if(pos>0) {
                sourceURI = uri.substring(0, pos);
            }
        }
        if(targetOnto.getOntologyID().getOntologyIRI()!=null){
            targetURI = targetOnto.getOntologyID().getOntologyIRI().toString();
        }else{
            OWLClass owlClass = targetOnto.getClassesInSignature().iterator().next();
            String uri = owlClass.getIRI().toURI().toString();
            targetURI = uri;
            int pos = uri.lastIndexOf("_");
            if(pos>0) {
                targetURI = uri.substring(0, pos);
            }
        }

        mappings = new ArrayList<Mapping>();
    }

    public Alignment(URL alignmentURL) {
        try {
            if (alignmentURL.getPath().endsWith(".rdf")) {
                mappings = new ArrayList<Mapping>();
                loadMappingsRDF(alignmentURL);
            }
        }catch(Exception e) {
            System.out.println("Error: Unrecognized alignment format");
        }
    }

    public ArrayList<Mapping> getMappings() {
        return(mappings);
    }

    public Mapping getMapping(String sourceId,String targetId) {
        if((sourceId!=null)&&(targetId!=null)){
            for(Mapping m: mappings){
                if((m.getSourceId().equals(sourceId))&&(m.getTargetId().equals(targetId))){
                    return(m);
                }
            }
        }
        return(null);
    }

    public boolean add(String sourceURI, String targetURI, double sim, MappingRelationship relationship, MappingStatus status) {
        Mapping mapping = new Mapping(sourceURI, targetURI, sim, relationship, status);
        if(!mappings.contains(mapping)){
            mappings.add(mapping);
            return true;
        }else{
            mapping = this.getMapping(sourceURI, targetURI);
            //we update its similarity
            boolean flag = false;
            if(mapping.getSimilarity()<sim) {
                mapping.setSimilarity(sim);
                flag = true;
            }
            //we update its relationship
            if(!mapping.getRelationship().equals(relationship)) {
                mapping.setRelationship(relationship);
                flag = true;
            }
            return flag;
        }
    }

    public boolean addEquivalenceMapping(String sourceURI, String targetURI, double sim) {
        return add(sourceURI, targetURI, sim, MappingRelationship.EQUIVALENCE, MappingStatus.UNKNOWN);
    }

    public boolean addSubclassMapping(String sourceURI, String targetURI, double sim) {
        return add(sourceURI,targetURI,sim,MappingRelationship.SUBCLASS,MappingStatus.UNKNOWN);
    }

    public boolean addSuperclassMapping(String sourceURI, String targetURI, double sim) {
        return add(sourceURI,targetURI,sim,MappingRelationship.SUPERCLASS,MappingStatus.UNKNOWN);
    }

    public boolean addAll(Collection<? extends Mapping> newMappings) {
        return(mappings.addAll(newMappings));
    }

    public boolean addAlignement(Alignment alignment){
        if((alignment!=null)&&(!alignment.getMappings().isEmpty())) {
            return(mappings.addAll(alignment.getMappings()));
        }
        return(false);
    }

    public int[] evaluate(Alignment ref) {
        int[] count = new int[2];
        for(Mapping m : mappings)
        {
            if(ref.getMappings().contains(m)) {
                count[0]++;
                m.setStatus(MappingStatus.CORRECT);
                //aux.remove(m);
            } else if(ref.contains(m.getSourceId(), m.getTargetId(), MappingRelationship.UNKNOWN)) {
                count[1]++;
                m.setStatus(MappingStatus.UNKNOWN);
            } else {
                m.setStatus(MappingStatus.INCORRECT);
            }
        }
        return count;
    }

    private void loadMappingsRDF(URL alignmentURL) {
        try {
            //Open the Alignment file using SAXReader
            SAXReader reader = new SAXReader();
            File f = new File(alignmentURL.toURI());

            Document doc = reader.read(f);
            //Read the root, then go to the "Alignment" element
            Element root = doc.getRootElement();
            Element align = root.element("Alignment");
            Element onto1 = align.element("onto1");
            sourceURI = onto1.getStringValue();
            Element onto2 = align.element("onto2");
            targetURI = onto2.getStringValue();
            if (sourceURI == null) {
                Element uri1 = align.element("uri1");
                sourceURI = uri1.getStringValue();
            }
            if (targetURI == null) {
                Element uri2 = align.element("uri2");
                targetURI = uri2.getStringValue();
            }
            //Get an iterator over the mappings
            Iterator<?> map = align.elementIterator("map");
            while (map.hasNext()) {
                //Get the "Cell" in each mapping
                Element e = ((Element) map.next()).element("Cell");
                if (e == null)
                    continue;
                //Get the source class
                String sourceURI = e.element("entity1").attributeValue("resource");
                //Get the target class
                String targetURI = e.element("entity2").attributeValue("resource");
                //Get the similarity measure
                String measure = e.elementText("measure");
                //Parse it, assuming 1 if a valid measure is not found
                double similarity = 1;
                if (measure != null) {
                    try {
                        similarity = Double.parseDouble(measure);
                        if (similarity < 0 || similarity > 1)
                            similarity = 1;
                    } catch (Exception ex) {/*Do nothing - use the default value*/}
                    ;
                }
                //Get the relation
                String r = e.elementText("relation");
                if (r == null)
                    r = "?";
                MappingRelationship rel = MappingRelationship.parseRelation(r);
                //Get the status
                String s = e.elementText("status");
                if (s == null)
                    s = "?";
                MappingStatus st = MappingStatus.parseStatus(s);
                add(sourceURI, targetURI, similarity, rel, st);
            }
        }catch(Exception e){
            System.out.println("Error: It has not been able to load the mapping file "+e.getMessage());
        }
    }

    public void saveRDF(String file) {
        try {
            PrintWriter outStream = new PrintWriter(new FileOutputStream(file));
            outStream.println("<?xml version='1.0' encoding='utf-8'?>");
            outStream.println("<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment'");
            outStream.println("\t xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#' ");
            outStream.println("\t xmlns:xsd='http://www.w3.org/2001/XMLSchema#'>\n");
            outStream.println("<Alignment>");
            outStream.println("\t<xml>yes</xml>");
            outStream.println("\t<level>0</level>");
            outStream.println("\t<type>??</type>");
            outStream.println("\t<onto1>" + sourceURI + "</onto1>");
            outStream.println("\t<onto2>" + targetURI + "</onto2>");
            outStream.println("\t<uri1>" + sourceURI + "</uri1>");
            outStream.println("\t<uri2>" + targetURI + "</uri2>");
            for (Mapping m : mappings)
                outStream.println(m.toRDF());
            outStream.println("</Alignment>");
            outStream.println("</rdf:RDF>");
            outStream.close();
        }catch(Exception e){
            System.out.println("Error: The output file has not been generated "+e.getMessage());
        }
    }

    public boolean contains(String sourceId, String targetId, MappingRelationship relationship) {
        for(Mapping mapping : mappings) {
            if((mapping.getSourceId().equals(sourceId))&&(mapping.getTargetId().equals(targetId))&&(
                    mapping.getRelationship()==relationship)){
                return(true);
            }
        }
        return(false);
    }
}
