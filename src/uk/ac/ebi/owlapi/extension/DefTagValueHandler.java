package uk.ac.ebi.owlapi.extension;

import org.coode.owlapi.obo.parser.AbstractTagValueHandler;
import org.coode.owlapi.obo.parser.OBOConsumer;
import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author zhutchok
 *         Date: 29-Mar-2011
 *         Time: 16:17:58
 */
public class DefTagValueHandler extends AbstractTagValueHandler {

    public DefTagValueHandler(OBOConsumer consumer) {
        super(OBOVocabulary.DEF.getName(), consumer);
    }


    public void handle(String id, String value) {
        OWLEntity ent;
        if (getConsumer().isTerm()) {
            ent = getDataFactory().getOWLClass(getIRIFromValue(id));
        } else if (getConsumer().isTypedef()) {
            ent = getDataFactory().getOWLObjectProperty(getIRIFromValue(id));
        } else {
            ent = getDataFactory().getOWLNamedIndividual(getIRIFromValue(id));
        }
        value = normalize(value);
        if (value.endsWith("]") && value.indexOf('[') > 0) {
            String ref = value.substring(value.lastIndexOf('['), value.length());
            value = value.substring(0, value.lastIndexOf('['));
            value = normalize(value);
            OWLLiteral con = getDataFactory().getOWLLiteral(ref);
            OWLAxiom ax = getDataFactory().getOWLAnnotationAssertionAxiom(getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI()), ent.getIRI(), con);
            applyChange(new AddAxiom(getOntology(), ax));
        }
        OWLLiteral con = getDataFactory().getOWLLiteral(value);
        OWLAxiom ax = getDataFactory().getOWLAnnotationAssertionAxiom(getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()), ent.getIRI(), con);
        applyChange(new AddAxiom(getOntology(), ax));
    }

    private String normalize(String value) {
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        } else if (value.startsWith("''") && value.endsWith("''")) {
            value = value.substring(2, value.length() - 2);
        }
        value = value.trim();
        return value;
    }
}
