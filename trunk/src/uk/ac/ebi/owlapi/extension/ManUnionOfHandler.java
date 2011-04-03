package uk.ac.ebi.owlapi.extension;

import org.coode.owlapi.manchesterowlsyntax.*;
import org.coode.owlapi.obo.parser.* ;
import org.semanticweb.owlapi.model.* ;
import org.semanticweb.owlapi.expression.* ;
import java.util.* ;

public class ManUnionOfHandler extends AbstractTagValueHandler {

    HashMap<String, ManOBORelation> relations = null ;

    public ManUnionOfHandler(OBOConsumer consumer, HashMap<String, ManOBORelation>relations) {
        super("union_of", consumer);
	this.relations = relations ;
    }

    private OWLClassExpression getNewOWLClassOrRestriction(String termList) {
	StringTokenizer tok = new StringTokenizer(termList, " ", false);
        String id0 = null;
        String id1 = null;
        id0 = tok.nextToken();
        if (tok.hasMoreTokens()) {
            id1 = tok.nextToken();
        }
        if (id1 == null) {
            return getDataFactory().getOWLClass(getIdIRI(id0));
	    // Use standard pattern if no OWLDEF available; otherwise use OWLDEF pattern
        } else {

	    OWLClassExpression toClass = null ; 
	    if (relations.containsKey(id0) && (relations.get(id0).getOwldef() != null)) {
		
		String namespace = null; 
		if (getConsumer().getCurrentNamespace() == null) {
		    namespace = getConsumer().getDefaultNamespace();
		}	else {
		    namespace = getConsumer().getCurrentNamespace();
		}
		String toTerm = id1.trim() ;
		OWLEntityChecker checker = new StupidEntityChecker(getDataFactory(), namespace);
		
		OWLObjectProperty prop = getDataFactory().getOWLObjectProperty(getIdIRI(id0));
		applyChange(new AddAxiom(getOntology(), getDataFactory().getOWLDeclarationAxiom(prop)));
		
		String s = relations.get(id0).getOwldef();
		
		while (s.indexOf("?Y")>=0) {
		    String toTerm2 = toTerm.replaceAll(":","_") ;
		    s = s.replace("?Y", toTerm2);
		}
		s = s.replaceAll("\"", "");
		
		ManchesterOWLSyntaxClassExpressionParser ms = new ManchesterOWLSyntaxClassExpressionParser(getDataFactory(), checker);
		//          OWLAxiom axiom = ms.parse(s);
		
		try {
		    toClass = ms.parse(s) ;
		} catch (Exception E) {
		    System.out.println ("Could not parse class expression "+s) ;
		    E.printStackTrace() ;
		}
		return toClass ;
	    } else {
		OWLObjectProperty prop = getDataFactory().getOWLObjectProperty(getIdIRI(id0));
		OWLClass filler = getDataFactory().getOWLClass(getIdIRI(id1));
		return getDataFactory().getOWLObjectSomeValuesFrom(prop, filler);
	    }
	}
	
    }
    
  public void handle(String id, String value, String comment) {
        getConsumer().addUnionOfOperand(getNewOWLClassOrRestriction(value)) ;
    }

    private static class StupidEntityChecker implements OWLEntityChecker {
        private OWLDataFactory factory;
        private String namespace;
        
        public StupidEntityChecker(OWLDataFactory factory, String _namespace) {
            this.factory = factory;
            this.namespace = _namespace;
        }
        


        public OWLClass getOWLClass(String name) {
            if (Character.isUpperCase(name.toCharArray()[0])) {
                return factory.getOWLClass(IRI.create(namespace + name));
            }
            else {
                return null;
            }
        }
        
        public OWLObjectProperty getOWLObjectProperty(String name) {
            if (!Character.isUpperCase(name.toCharArray()[0])) {
                return factory.getOWLObjectProperty(IRI.create(namespace + name));
            }
            else {
                return null;
            }
        }
        
        public OWLAnnotationProperty getOWLAnnotationProperty(String name) {
            return null;
        }

        public OWLDataProperty getOWLDataProperty(String name) {
            return null;
        }

        public OWLDatatype getOWLDatatype(String name) {
            return null;
        }

        public OWLNamedIndividual getOWLIndividual(String name) {
            return null;
        }
        
    }

}