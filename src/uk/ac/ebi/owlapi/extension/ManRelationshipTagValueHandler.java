package uk.ac.ebi.owlapi.extension;

import java.util.HashMap;

import org.coode.owlapi.manchesterowlsyntax.*;
import org.coode.owlapi.obo.parser.AbstractTagValueHandler;
import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;



public class ManRelationshipTagValueHandler extends AbstractTagValueHandler {
	
    private HashMap<String, ManOBORelation> relations;
	
    public ManRelationshipTagValueHandler(ManOBOConsumer consumer, HashMap<String, ManOBORelation>relations) {
		
	super(OBOVocabulary.RELATIONSHIP.getName(), consumer);
	this.relations = relations;
    }
	
    public void handle(String id, String value) {
	String namespace = null; 
	String relationId = value.substring(0, value.indexOf(' ')).trim();
		
	if (getConsumer().getCurrentNamespace() == null) {
	    namespace = getConsumer().getDefaultNamespace();
	}	else {
	    namespace = getConsumer().getCurrentNamespace();
	}
		
	
	OWLClassExpression toClass = null ;
		
	if (relations.containsKey(relationId) && (relations.get(relationId).getOwldef() != null)) {
			
	    String toTerm = value.substring(value.indexOf(' '), value.length()).trim();
	    OWLEntityChecker checker = new StupidEntityChecker(getDataFactory(), namespace);
			
	    OWLObjectProperty prop = getDataFactory().getOWLObjectProperty(getIRIFromValue(relationId));
	    applyChange(new AddAxiom(getOntology(), getDataFactory().getOWLDeclarationAxiom(prop)));
			
	    String s = relations.get(relationId).getOwldef();

	    while (s.indexOf("?Y")>=0) {
		String toTerm2 = toTerm.replaceAll(":","_") ;
		s = s.replace("?Y", toTerm2);
	    }
	    s = s.replaceAll("\"", "");
	    
	    ManchesterOWLSyntaxClassExpressionParser ms = new ManchesterOWLSyntaxClassExpressionParser(getDataFactory(), checker);
	    //          OWLAxiom axiom = ms.parse(s);
	    
	    try {
		toClass = ms.parse(s) ;
		OWLClass subCls = getDataFactory().getOWLClass(getIRIFromValue(id));
		applyChange(new AddAxiom(getOntology(), getDataFactory().getOWLSubClassOfAxiom(subCls,toClass))) ;
	    } catch (Exception E) {
		System.out.println ("Could not parse class expression "+s) ;
		E.printStackTrace() ;
	    }
			
	} else {
		
	    IRI propIRI = getIRIFromValue(value.substring(0, value.indexOf(' ')).trim());
	    IRI fillerIRI = getIRIFromValue(value.substring(value.indexOf(' '), value.length()).trim());
	        
	    OWLObjectProperty prop = getDataFactory().getOWLObjectProperty(propIRI);
	    OWLClass filler = getDataFactory().getOWLClass(fillerIRI);
	        
	    OWLClassExpression restriction = getDataFactory().getOWLObjectSomeValuesFrom(prop, filler);
	    OWLClass subCls = getDataFactory().getOWLClass(getIRIFromValue(id));

	    applyChange(new AddAxiom(getOntology(), getDataFactory().getOWLSubClassOfAxiom(subCls, restriction)));
	}
		
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
