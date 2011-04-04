package uk.ac.ebi.owlapi.extension;

import org.coode.owlapi.obo.parser.AbstractTagValueHandler;
//import org.coode.owlapi.obo.parser.OBOConsumer;
import org.coode.owlapi.obo.parser.OBOVocabulary;
import org.semanticweb.owlapi.model.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author zhutchok
 *         Date: 29-Mar-2011
 *         Time: 16:51:25
 */
public class ManSynonymTagValueHandler extends AbstractTagValueHandler {

    private OBOVocabulary synonymTag;
    private final Map<OBOVocabulary, OWLLiteral> tagToType = new HashMap<OBOVocabulary, OWLLiteral>();
    {
        tagToType.put(OBOVocabulary.EXACT_SYNONYM, getDataFactory().getOWLLiteral("EXACT"));
        tagToType.put(OBOVocabulary.BROAD_SYNONYM, getDataFactory().getOWLLiteral("BROAD"));
        tagToType.put(OBOVocabulary.NARROW_SYNONYM, getDataFactory().getOWLLiteral("NARROW"));
        tagToType.put(OBOVocabulary.RELATED_SYNONYM, getDataFactory().getOWLLiteral("RELATED"));
        tagToType.put(OBOVocabulary.SYNONYM, getDataFactory().getOWLLiteral("RELATED"));
    }

    public ManSynonymTagValueHandler(ManOBOConsumer consumer, OBOVocabulary synonymTag) {
        super(synonymTag == null ? OBOVocabulary.SYNONYM.getName() : synonymTag.getName(), consumer);
        this.synonymTag = synonymTag == null ? OBOVocabulary.SYNONYM : synonymTag;
    }

    public ManSynonymTagValueHandler(ManOBOConsumer consumer) {
        this(consumer, null);
    }


  public void handle(String id, String value, String comment) {
        OWLEntity ent;
        if (getConsumer().isTerm()) {
            ent = getDataFactory().getOWLClass(getIdIRI(id));
        } else if (getConsumer().isTypedef()) {
            ent = getDataFactory().getOWLObjectProperty(getIdIRI(id));
        } else {
            ent = getDataFactory().getOWLNamedIndividual(getIdIRI(id));
        }
        OWLLiteral con = getDataFactory().getOWLLiteral(value);
        Set<OWLAnnotation> annotations = new HashSet<OWLAnnotation>();
        OWLLiteral type = tagToType.get(synonymTag);
        if (type != null) {
            annotations.add(getDataFactory().getOWLAnnotation(getDataFactory().getOWLAnnotationProperty(
                            getConsumer().getTagIRI("synonym_type")), type));
        }
        OWLAxiom ax = getDataFactory().getOWLAnnotationAssertionAxiom(
                getDataFactory().getOWLAnnotationProperty(OBOVocabulary.SYNONYM.getIRI()),
                ent.getIRI(), con, annotations);
        applyChange(new AddAxiom(getOntology(), ax));
    }
}
