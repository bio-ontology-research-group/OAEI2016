package marg.algorithm;


import marg.match.Alignment;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import java.util.Set;


/**
 * Created by marg27 on 12/06/16.
 */
public class AxiomAlignmentAlgorithm extends AlgorithmAlignment{

    public AxiomAlignmentAlgorithm(OWLOntology sourceOnto, OWLOntology targetOnto, OWLOntology phenomeNetOnto){
        super(sourceOnto,targetOnto,phenomeNetOnto);
    }
    public Alignment applyMatch(){
        Alignment alignment = new Alignment(sourceOnto,targetOnto);
        if((sourceOnto!=null)&&(targetOnto!=null)&&(phenomeNetOnto!=null)){
            try {

                int eqClassesCounter =0;
                int subEqClassesCounter = 0;
                int superEqClassesCounter=0;
                int hpClasses = 0;
                int mpClasses = 0;
                int doidClasses = 0;
                int ordoClasses = 0;
                int rightSideNoNamedClass =0;

                ElkReasonerFactory reasonerFactory = new ElkReasonerFactory();
                OWLReasoner phenomeReasoner = reasonerFactory.createReasoner(phenomeNetOnto);

                OWLClass nothing = sourceOnto.getOWLOntologyManager().getOWLDataFactory().getOWLNothing();
                OWLClass thing = sourceOnto.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
                Set<OWLClass> sourceClasses = sourceOnto.getClassesInSignature();
                Set<OWLClass> targetClasses = targetOnto.getClassesInSignature();
                sourceClasses.remove(thing);
                sourceClasses.remove(nothing);

                for (OWLClass clazz : sourceClasses) {
                    Node<OWLClass> node = phenomeReasoner.getEquivalentClasses(clazz);
                    if (!node.getRepresentativeElement().isOWLNothing()) {
                        Set<OWLClass> entities = node.getEntities();
                        entities.remove(nothing);
                        entities.remove(thing);
                        //we check the equivalent classes
                        for (OWLClass entity : entities) {
                            if (targetClasses.contains(entity)) {
                                alignment.addEquivalenceMapping(clazz.getIRI().toURI().toString(), entity.getIRI().toURI().toString(), 1.0);
                                eqClassesCounter++;
                            }
                        }
                    }
                    Set<OWLClass> subClasses = phenomeReasoner.getSubClasses(clazz, true).getFlattened();
                    subClasses.remove(nothing);
                    subClasses.remove(thing);
                    for (OWLClass subClass : subClasses) {
                        //we check the subclasses
                        if (targetClasses.contains(subClass)) {
                            alignment.addSubclassMapping(clazz.getIRI().toURI().toString(), subClass.getIRI().toURI().toString(), 1.0);
                            subEqClassesCounter++;
                        }
                    }

                    Set<OWLClass> superClasses = phenomeReasoner.getSuperClasses(clazz, true).getFlattened();
                    superClasses.remove(nothing);
                    superClasses.remove(thing);
                    for (OWLClass superClass : superClasses) {
                        //we check the superclasses
                        if (targetClasses.contains(superClass)) {
                            alignment.addSuperclassMapping(clazz.getIRI().toURI().toString(), superClass.getIRI().toURI().toString(), 1.0);
                            superEqClassesCounter++;

                        }
                    }
                }

                Set<OWLClass> phenomeNetClasses = phenomeNetOnto.getClassesInSignature();

                for(OWLClass clazz : phenomeNetClasses){
                    if(clazz.getIRI().getFragment().contains("HP_")){
                        hpClasses++;
                    }else if(clazz.getIRI().getFragment().contains("MP_")){
                        mpClasses++;
                    }else if(clazz.getIRI().getFragment().contains("DOID_")){
                        doidClasses++;
                    }else if(clazz.getIRI().getFragment().contains("Orphanet_")){
                        ordoClasses++;
                    }
                    Set<OWLClass> entities = phenomeReasoner.getEquivalentClasses(clazz).getEntities();
                    for(OWLClass entity : entities){
                        if(entity.getIRI().getFragment().contains("Orphanet_")){
                            ordoClasses++;
                        }
                    }
                }

                phenomeNetClasses = phenomeNetOnto.getClassesInSignature();
                for(OWLClass clazz : phenomeNetClasses){
                    Set<OWLEquivalentClassesAxiom> equivalentClassesAxioms = phenomeNetOnto.getEquivalentClassesAxioms(clazz);
                    boolean flag = false;
                    for (OWLEquivalentClassesAxiom eq1 : equivalentClassesAxioms){
                        for (OWLClassExpression expression : eq1.getClassExpressions()) {
                            if(expression.getClassExpressionType()==ClassExpressionType.OBJECT_INTERSECTION_OF){
                                flag=true;
                            }
                            if(expression.getClassExpressionType()== ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
                                flag = true;
                            }
                        }
                    }
                    if(flag){
                        rightSideNoNamedClass++;
                    }
                }

                String sourceName = ((OWLClass)sourceClasses.toArray()[0]).getIRI().getFragment();
                sourceName = sourceName.substring(0,sourceName.indexOf("_"));
                String targetName = ((OWLClass)targetClasses.toArray()[0]).getIRI().getFragment();
                targetName = targetName.substring(0,targetName.indexOf("_"));

                System.out.println("Total number of equivalent classes identified:"+eqClassesCounter);
                System.out.println("Total number of subclasses identified when no equivalent class:"+subEqClassesCounter);
                System.out.println("Total number of superclasses identified when no equivalent class:"+superEqClassesCounter);
                System.out.println("source:"+sourceName);
                System.out.println("\tTotal number of classes in the "+sourceName+" ontology:"+sourceOnto.getClassesInSignature().size());
                System.out.println("\tTotal number of axioms in the "+sourceName+" ontology:"+sourceOnto.getAxioms().size());
                System.out.println("target:"+targetName);
                System.out.println("\tTotal number of classes in the "+targetName+" ontology:"+targetOnto.getClassesInSignature().size());
                System.out.println("\tTotal number of axioms in the "+targetName+" ontology:"+targetOnto.getAxioms().size());
                System.out.println("PhenomeNet");
                System.out.println("Total number of equivalent classes identified:"+phenomeNetOnto.getOntologyID().getOntologyIRI());
                System.out.println("\tTotal number of classes in the PhenomeNET ontology:"+phenomeNetOnto.getClassesInSignature().size());
                System.out.println("\tTotal number of axioms in the PhenomeNET ontology:"+phenomeNetOnto.getAxioms().size());
                System.out.println("\tTotal number of axioms in which the right side is not a named class:"+rightSideNoNamedClass);
                System.out.println("\tTotal number of phenotype classes (HP) in the PhenomeNET ontology:"+hpClasses);
                System.out.println("\tTotal number of phenotype classes (MP) in the PhenomeNET ontology:"+mpClasses);
                System.out.println("\tTotal number of phenotype classes (DOID) in the PhenomeNET ontology:"+doidClasses);
                System.out.println("\tTotal number of phenotype classes (ORDO) in the PhenomeNET ontology:"+ordoClasses);

            }catch (Exception e){
                e.printStackTrace();
                System.out.println("Error: Alignment not applied");
            }
        }
        return(alignment);
    }
}
