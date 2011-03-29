import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.* ;
import org.semanticweb.owlapi.model.*;
import org.coode.owlapi.manchesterowlsyntax.*;
import java.net.URI;
import org.coode.owlapi.obo.parser.*;
import java.io.*;
import uk.ac.ebi.owlapi.extension.*;

public class OBOTester {

    public static void main(String argv[]) throws Exception {
	String infile = argv[0] ;
	String outfile = argv[1] ;
	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	File f = new File(infile);
	FileDocumentSource fds = new FileDocumentSource(f);
	IRI ontologyIRI = IRI.create("http://www.co-ode.org/ontologies/testont.owl");
	
	OWLOntology ontology = manager.createOntology(ontologyIRI);
	
	ManOWLOBOParser parser = new ManOWLOBOParser();
	parser.setOWLOntologyManager(manager);
	parser.parse(fds,ontology);
	IRI documentIRI2 = IRI.create("file:"+outfile);
	manager.saveOntology(ontology, documentIRI2);
	manager.removeOntology(ontology);
    }
}
