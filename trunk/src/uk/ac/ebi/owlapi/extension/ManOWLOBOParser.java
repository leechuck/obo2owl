package uk.ac.ebi.owlapi.extension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import org.coode.owlapi.obo.parser.OBOConsumer;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.coode.owlapi.obo.parser.OBOParser;
import org.coode.owlapi.obo.parser.ParseException;
import org.coode.owlapi.obo.parser.TokenMgrError;
import org.semanticweb.owlapi.io.AbstractOWLParser;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.*;


public class ManOWLOBOParser extends AbstractOWLParser {


    public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource, OWLOntology ontology) throws OWLParserException, IOException, UnloadableImportException {
        return parse(documentSource, ontology, new OWLOntologyLoaderConfiguration());
    }

  public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource, OWLOntology ontology, OWLOntologyLoaderConfiguration configuration) throws OWLParserException, IOException {
        OBOParser parser;
        Reader reader;
        HashMap<String, ManOBORelation> relations = null;

        if (documentSource.isReaderAvailable()) {
            parser = new OBOParser(documentSource.getReader());
            reader = documentSource.getReader();
        } else if (documentSource.isInputStreamAvailable()) {
            parser = new OBOParser(documentSource.getInputStream());
            reader = new InputStreamReader(documentSource.getInputStream());
        } else {
            parser = new OBOParser(getInputStream(documentSource.getDocumentIRI()));
            reader = new InputStreamReader(getInputStream(documentSource.getDocumentIRI()));
        }

        relations = readRelations(reader);
        parser.setHandler(new ManOBOConsumer(ontology.getOWLOntologyManager(), ontology, configuration, relations));

        try {
            parser.parse();
        } catch (ParseException e) {
            throw new OWLParserException(e, e.currentToken.beginLine, e.currentToken.beginColumn);
        } catch (TokenMgrError e) {
            throw new OWLParserException(e);
        }
        return new OBOOntologyFormat();
    }

    public HashMap<String, ManOBORelation> readRelations(Reader _reader) {
        HashMap<String, ManOBORelation> relations = new HashMap<String, ManOBORelation>();
        BufferedReader br = new BufferedReader(_reader);
        String fileData = null;

        // System.out.println("1");

        try {
            while ((fileData = br.readLine()) != null) {

                if (fileData.equals("[Typedef]")) {

                    String id = null;
                    String name = null;
                    String owldef = null;
                    String reldef = null;

                    while ((fileData = br.readLine()) != null) {


                        // System.out.println("2");


                        if (fileData.startsWith("id: ")) {
                            id = fileData.substring(4).trim();
                            // System.out.println("4");

                            if (id.indexOf("!") != -1) {
                                id = id.substring(0, id.indexOf("!")).trim();
                            }
                        }

                        if (fileData.startsWith("name: ")) {
                            //							System.out.println("3");
                            name = fileData.substring(6).trim();

                        }

                        if (fileData.startsWith("owldef: ")) {
                            // System.out.println("3");
                            owldef = fileData.substring(8).trim();

                            if (id != null) {
                                relations.put(id, new ManOBORelation(id, name, owldef));
                            }
                        }
                        if (fileData.startsWith("reldef: ")) {
                            // System.out.println("3");
                            owldef = fileData.substring(8).trim();

                            if (id != null) {
                                relations.put(id, new ManOBORelation(id, name, reldef));
                            }
                        }


                    }

                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return relations;
    }

}
