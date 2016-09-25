package marg.match;


/**
 * Created by marg27 on 14/06/16.
 */
public class Mapping implements Comparable<Mapping> {
    private String sourceId;
    private String targetId;
    private double similarity;
    private MappingRelationship relationship;
    private MappingStatus status;

//Constructors

    public Mapping(String sourceId, String targetId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        similarity = 1.0;
        relationship = MappingRelationship.EQUIVALENCE;
        status = MappingStatus.UNKNOWN;
    }

    public Mapping(String sourceId, String targetId, double sim) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        similarity = Math.round(sim*10000)/10000.0;
        relationship = MappingRelationship.EQUIVALENCE;
        status = MappingStatus.UNKNOWN;
    }

    public Mapping(String sourceId, String targetId, double sim, MappingRelationship relationship) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        similarity = Math.round(sim*10000)/10000.0;
        this.relationship = relationship;
        status = MappingStatus.UNKNOWN;
    }

    public Mapping(String sourceId, String targetId, double sim, MappingRelationship relationship,MappingStatus status) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        similarity = Math.round(sim*10000)/10000.0;
        this.relationship = relationship;
        this.status = status;
    }

    public Mapping(Mapping mapping) {
        sourceId = mapping.getSourceId();
        targetId = mapping.getTargetId();
        similarity = mapping.getSimilarity();
        relationship = mapping.getRelationship();
        status = mapping.getStatus();
    }

    public int compareTo(Mapping object) {
        if(this.status.equals(object.status)) {
            double diff = this.similarity - object.similarity;
            if(diff < 0)
                return -1;
            if(diff > 0)
                return 1;
            return 0;
        }
        else return this.status.compareTo(object.status);
    }

    public boolean equals(Object o) {
        if(!(o instanceof Mapping)) {
            return false;
        }
        Mapping m = (Mapping)o;
        return (this.sourceId.equals(m.sourceId) && this.targetId.equals(m.targetId));
    }

    public MappingRelationship getRelationship() {
        return relationship;
    }

    public double getSimilarity() {
        return similarity;
    }

    public String getSimilarityPercent() {
        return (Math.round(similarity*10000) * 1.0 / 100) + "%";
    }

    public String getSourceId() {
        return sourceId;
    }

    public MappingStatus getStatus() {
        return status;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setRelationship(MappingRelationship relationship) {
        this.relationship = relationship;
    }

    public void setSimilarity(double similarity) {
        this.similarity = Math.round(similarity*10000)/10000.0;
    }

    public void setStatus(MappingStatus status) {
        this.status = status;
    }

    public String toRDF() {
        String out = "\t<map>\n" +
                "\t\t<Cell>\n" +
                "\t\t\t<entity1 rdf:resource=\""+sourceId+"\"/>\n" +
                "\t\t\t<entity2 rdf:resource=\""+targetId+"\"/>\n" +
                "\t\t\t<measure rdf:datatype=\"xsd:float\">"+ similarity +"</measure>\n" +
                "\t\t\t<relation>" + relationship.toString() + "</relation>\n";
        if(!status.equals(MappingStatus.UNKNOWN)) {
            out += "\t\t\t<status>" + status.toString() + "</status>\n";
        }
        out += "\t\t</Cell>\n" +
                "\t</map>\n";
        return out;
    }
}