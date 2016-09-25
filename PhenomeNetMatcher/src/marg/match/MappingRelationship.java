package marg.match;

/**
 * Created by marg27 on 14/06/16.
 */
public enum MappingRelationship {
    EQUIVALENCE	("=","equivalence"),
    //SUPERCLASS	(">","superclass"),
    //SUBCLASS	("<","subclass"),
    SUPERCLASS	("&gt;","superclass"),
    SUBCLASS	("&lt;","subclass"),
    OVERLAP		("^","overlap"),
    UNKNOWN		("?","unknown");

    private String representation;
    private String label;

    private MappingRelationship(String rep, String l) {
        representation = rep;
        label = l;
    }

    public String getLabel() {
        return label;
    }

    public MappingRelationship inverse() {
        if(this.equals(SUBCLASS))
            return SUPERCLASS;
        else if(this.equals(SUPERCLASS))
            return SUBCLASS;
        else
            return this;
    }

    public String toString() {
        return representation;
    }

    public static MappingRelationship parseRelation(String relation)
    {
        if(relation.length() == 1)
        {
            for(MappingRelationship rel : MappingRelationship.values())
                if(relation.equals(rel.toString()))
                    return rel;
        }
        else
        {
            for(MappingRelationship rel : MappingRelationship.values())
                if(relation.equals(rel.getLabel()))
                    return rel;
        }
        return UNKNOWN;
    }
}