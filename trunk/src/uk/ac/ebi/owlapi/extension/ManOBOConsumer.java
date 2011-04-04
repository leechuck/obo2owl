package uk.ac.ebi.owlapi.extension;

import org.coode.owlapi.obo.parser.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ManOBOConsumer extends OBOConsumer {

    private static final Logger logger = Logger.getLogger(OBOConsumer.class.getName());

    private static final String IMPORT_TAG_NAME = "import";

    private OWLOntologyLoaderConfiguration configuration;

    private OWLOntologyManager owlOntologyManager;

    private OWLOntology ontology;

    private boolean inHeader;

    private String currentId;

    private Map<String, TagValueHandler> handlerMap;

    private String defaultNamespace;

    private String currentNamespace;

    private String stanzaType;

    private boolean termType;

    private boolean typedefType;

    private boolean instanceType;

    private Set<OWLClassExpression> intersectionOfOperands;

    private Set<OWLClassExpression> unionOfOperands;

    private Map<String, IRI> uriCache;

    public HashMap<String, ManOBORelation> relations;


  public ManOBOConsumer(OWLOntologyManager owlOntologyManager, OWLOntology ontology, OWLOntologyLoaderConfiguration configuration, HashMap<String, ManOBORelation> _relations) {
    super(owlOntologyManager, ontology, configuration);
    this.configuration = configuration;
    this.relations = _relations;
    this.owlOntologyManager = owlOntologyManager;
    this.ontology = ontology;
    defaultNamespace = OBOVocabulary.ONTOLOGY_URI_BASE;
    intersectionOfOperands = new HashSet<OWLClassExpression>();
    unionOfOperands = new HashSet<OWLClassExpression>();
    uriCache = new HashMap<String, IRI>();
    loadBuiltinURIs();
    setupTagHandlers();
  }

  private void setupTagHandlers() {
        handlerMap = new HashMap<String, TagValueHandler>();
        addTagHandler(new OntologyTagValueHandler(this));
        addTagHandler(new IDTagValueHandler(this));
        addTagHandler(new NameTagValueHandler(this));
        addTagHandler(new IsATagValueHandler(this));
	//        addTagHandler(new PartOfTagValueHandler(this));
        addTagHandler(new TransitiveTagValueHandler(this));
        addTagHandler(new SymmetricTagValueHandler(this));
	//        addTagHandler(new RelationshipTagValueHandler(this));
	//        addTagHandler(new UnionOfHandler(this));
	//        addTagHandler(new IntersectionOfHandler(this));
        addTagHandler(new DisjointFromHandler(this));
        addTagHandler(new AsymmetricHandler(this));
        addTagHandler(new InverseHandler(this));
        addTagHandler(new ReflexiveHandler(this));
        addTagHandler(new TransitiveOverHandler(this));
        addTagHandler(new DefaultNamespaceTagValueHandler(this));
        addTagHandler(new ManRelationshipTagValueHandler(this, relations));
        addTagHandler(new ManUnionOfHandler(this, relations));
        addTagHandler(new ManIntersectionOfHandler(this, relations));

        // tag handlers for replacing obo tags with analogous owl ones.
        addTagHandler(new DefTagValueHandler(this));
        addTagHandler(new GenericTagValueHandler(this, OBOVocabulary.IS_OBSOLETE, OWLRDFVocabulary.OWL_DEPRECATED));
        addTagHandler(new GenericTagValueHandler(this, OBOVocabulary.XREF, OWLRDFVocabulary.RDFS_SEE_ALSO));
        addTagHandler(new ManSynonymTagValueHandler(this, OBOVocabulary.EXACT_SYNONYM));
        addTagHandler(new ManSynonymTagValueHandler(this, OBOVocabulary.RELATED_SYNONYM));
        addTagHandler(new ManSynonymTagValueHandler(this, OBOVocabulary.BROAD_SYNONYM));
        addTagHandler(new ManSynonymTagValueHandler(this, OBOVocabulary.NARROW_SYNONYM));
        addTagHandler(new ManSynonymTagValueHandler(this, OBOVocabulary.SYNONYM));


    }

    public OWLOntologyManager getOWLOntologyManager() {
        return owlOntologyManager;
    }


    public OWLOntology getOntology() {
        return ontology;
    }


    public String getCurrentId() {
        return currentId;
    }


    public String getDefaultNamespace() {
        return defaultNamespace;
    }


    public void setDefaultNamespace(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
    }


    public String getCurrentNamespace() {
        return currentNamespace;
    }


    public void setCurrentNamespace(String currentNamespace) {
        this.currentNamespace = currentNamespace;
    }


    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }


    public void addUnionOfOperand(OWLClassExpression classExpression) {
        unionOfOperands.add(classExpression);
    }


    public void addIntersectionOfOperand(OWLClassExpression classExpression) {
        intersectionOfOperands.add(classExpression);
    }


    public String getStanzaType() {
        return stanzaType;
    }


    public boolean isTerm() {
        return termType;
    }


    public boolean isTypedef() {
        return typedefType;
    }


    public boolean isInstanceType() {
        return instanceType;
    }

    private void loadBuiltinURIs() {
        for (OBOVocabulary v : OBOVocabulary.values()) {
            uriCache.put(v.getName(), v.getIRI());
        }
    }


    private void addTagHandler(TagValueHandler handler) {
        handlerMap.put(handler.getTag(), handler);
    }


    public void startHeader() {
        inHeader = true;
    }


    public void endHeader() {
        inHeader = false;
    }


    public void startStanza(String name) {
        currentId = null;
        currentNamespace = null;
        stanzaType = name;
        termType = stanzaType.equals(OBOVocabulary.TERM.getName());
        typedefType = false;
        instanceType = false;
        if (!termType) {
            typedefType = stanzaType.equals(OBOVocabulary.TYPEDEF.getName());
            if (!typedefType) {
                instanceType = stanzaType.equals(OBOVocabulary.INSTANCE.getName());
            }
        }
    }


    public void endStanza() {
        if (!unionOfOperands.isEmpty()) {
            createUnionEquivalentClass();
            unionOfOperands.clear();
        }

        if (!intersectionOfOperands.isEmpty()) {
            createIntersectionEquivalentClass();
            intersectionOfOperands.clear();
        }
    }


    private void createUnionEquivalentClass() {
        OWLClassExpression equivalentClass;
        if (unionOfOperands.size() == 1) {
            equivalentClass = unionOfOperands.iterator().next();
        }
        else {
            equivalentClass = getDataFactory().getOWLObjectUnionOf(unionOfOperands);
        }
        createEquivalentClass(equivalentClass);
    }


    private void createIntersectionEquivalentClass() {
        OWLClassExpression equivalentClass;
        if (intersectionOfOperands.size() == 1) {
            equivalentClass = intersectionOfOperands.iterator().next();
        }
        else {
            equivalentClass = getDataFactory().getOWLObjectIntersectionOf(intersectionOfOperands);
        }
        createEquivalentClass(equivalentClass);
    }


    private void createEquivalentClass(OWLClassExpression classExpression) {
        OWLAxiom ax = getDataFactory().getOWLEquivalentClassesAxiom(CollectionFactory.createSet(getCurrentClass(), classExpression));
        getOWLOntologyManager().applyChange(new AddAxiom(ontology, ax));
    }


    public void handleTagValue(String tag, String value, String comment) {
        try {
            TagValueHandler handler = handlerMap.get(tag);
            if (handler != null) {
                handler.handle(currentId, value, comment);
            }
            else if (inHeader) {
                if (tag.equals(IMPORT_TAG_NAME)) {
                    IRI uri = IRI.create(value.trim());
                    OWLImportsDeclaration decl = owlOntologyManager.getOWLDataFactory().getOWLImportsDeclaration(uri);
                    owlOntologyManager.makeLoadImportRequest(decl, configuration);
                    owlOntologyManager.applyChange(new AddImport(ontology, decl));
                }
                else {
                    // Ontology annotations
                    OWLLiteral con = getDataFactory().getOWLLiteral(value);
                    OWLAnnotationProperty property = getDataFactory().getOWLAnnotationProperty(getIRI(tag));
                    OWLAnnotation anno = getDataFactory().getOWLAnnotation(property, con);
                    owlOntologyManager.applyChange(new AddOntologyAnnotation(ontology, anno));
                }
            }
            else if (currentId != null) {
                // Add as annotation
                if (configuration.isLoadAnnotationAxioms()) {
                    IRI subject = getIRI(currentId);
                    OWLLiteral con = getDataFactory().getOWLLiteral(value, "");
                    OWLAnnotationProperty property = getDataFactory().getOWLAnnotationProperty(getIRI(tag));
                    OWLAnnotation anno = getDataFactory().getOWLAnnotation(property, con);
                    OWLAnnotationAssertionAxiom ax = getDataFactory().getOWLAnnotationAssertionAxiom(subject, anno);
                    owlOntologyManager.addAxiom(ontology, ax);
                    OWLDeclarationAxiom annotationPropertyDeclaration = getDataFactory().getOWLDeclarationAxiom(property);
                    owlOntologyManager.addAxiom(ontology, annotationPropertyDeclaration);
                }
            }

        }
        catch (UnloadableImportException e) {
            logger.severe(e.getMessage());
        }
    }


    private OWLDataFactory getDataFactory() {
        return getOWLOntologyManager().getOWLDataFactory();
    }


    public OWLClass getCurrentClass() {
        return getDataFactory().getOWLClass(getIRI(currentId));
    }

    public OWLEntity getCurrentEntity() {
        if (isTerm()) {
            return getCurrentClass();
        }
        else if (isTypedef()) {
            return getDataFactory().getOWLObjectProperty(getIRI(currentId));
        }
        else {
            return getDataFactory().getOWLNamedIndividual(getIRI(currentId));
        }
    }

    public IRI getTagIRI(String tagName) {
        return getIRI(tagName);
    }

    public IRI getIdIRI(String identifier) {
        if(identifier == null) {
            Thread.dumpStack();
        }
        if(identifier.indexOf(":") != -1) {
            return getIRI(identifier);
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append(defaultNamespace);
            sb.append(":");
            sb.append(identifier);
            return getIRI(sb.toString());
        }
    }

    private IRI getIRI(String s) {
        IRI iri = uriCache.get(s);
        if (iri != null) {
            return iri;
        }
        String escapedString = s.replace(" ", "%20");
        iri = OBOVocabulary.ID2IRI(escapedString);
        uriCache.put(s, iri);
        return iri;
    }

}
