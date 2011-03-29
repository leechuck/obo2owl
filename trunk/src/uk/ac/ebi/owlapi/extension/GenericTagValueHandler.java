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
 *         Time: 16:01:54
 */
public class GenericTagValueHandler extends AbstractTagValueHandler {
    private final OWLRDFVocabulary owlTag;

    public GenericTagValueHandler(OBOConsumer consumer, OBOVocabulary oboTag, OWLRDFVocabulary owlTag) {
        super(oboTag.getName(), consumer);
        this.owlTag = owlTag;
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
        OWLLiteral con = getDataFactory().getOWLLiteral(value);
        OWLAxiom ax = getDataFactory().getOWLAnnotationAssertionAxiom(getDataFactory().getOWLAnnotationProperty(owlTag.getIRI()), ent.getIRI(), con);
        applyChange(new AddAxiom(getOntology(), ax));
    }
}
