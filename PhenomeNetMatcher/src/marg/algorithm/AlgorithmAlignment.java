package marg.algorithm;

import marg.match.Alignment;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Created by marg27 on 12/06/16.
 */
public abstract class AlgorithmAlignment {

    protected OWLOntology sourceOnto = null;
    protected OWLOntology targetOnto = null;
    protected OWLOntology phenomeNetOnto = null;

    public AlgorithmAlignment(OWLOntology sourceOnto, OWLOntology targetOnto,OWLOntology phenomeNetOnto){
        this.sourceOnto = sourceOnto;
        this.targetOnto = targetOnto;
        this.phenomeNetOnto = phenomeNetOnto;
    }
    public abstract Alignment applyMatch();

    public OWLOntology getTargetOnto() {
        return targetOnto;
    }

    public void setTargetOnto(OWLOntology targetOnto) {
        this.targetOnto = targetOnto;
    }

    public OWLOntology getPhenomeNetOnto() {
        return phenomeNetOnto;
    }

    public void setPhenomeNetOnto(OWLOntology phenomeNetOnto) {
        this.phenomeNetOnto = phenomeNetOnto;
    }

    public OWLOntology getSourceOnto() {

        return sourceOnto;
    }

    public void setSourceOnto(OWLOntology sourceURI) {
        this.sourceOnto = sourceOnto;
    }
}
