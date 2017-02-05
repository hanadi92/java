/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelrdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
//import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.java.megalib.checker.services.ModelLoader;
import org.java.megalib.models.MegaModel;
import org.java.megalib.models.Relation;
/**
 *
 * @author Hanadi
 */
public class ModelRDF {
    
    public static String prefix = "XML";
    public static String namespace = "https://en.wikipedia.org/wiki/XML/";
    public static String fileName = "rdfmodel.ttl";
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
//        String path = "../models/XML.megal";
//        MegaModel megaModel = createMegaModel(path);
//        ModelBuilder RdfModel = createRDFModel();
//        ModelBuilder theRDFModel = extractRelation(megaModel, RdfModel);
//        Model modelRDF = theRDFModel.build();
//        printOutRDFModel(modelRDF);
        createDatabase();
    }

    private static void createDatabase() throws RDFParseException, RepositoryException, IOException {
    	// Create a new Repository. Here, we choose a database implementation
    	// that simply stores everything in main memory.
    	Repository db = new SailRepository(new MemoryStore());
    	db.initialize();
    	// Open a connection to the database
    	try (RepositoryConnection conn = db.getConnection()) {
    	    try (InputStream input = new FileInputStream(fileName)) {
    		// add the RDF data from the input stream directly to our database
    		conn.add(input, "", RDFFormat.TURTLE );
    	    }
    		// let's check that our data is actually in the database
    		try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
    			while (result.hasNext()) {
    				Statement st = result.next();
//    				System.out.println("db contains: " + st);
    			}
    		}
    		// We do a simple SPARQL SELECT-query that retrieves all resources of
    		// type `ex:Artist`, and their first names.
    		String queryString  = "prefix XML: <https://en.wikipedia.org/wiki/XML/>\n";
    		queryString  += "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n";
    		queryString += "SELECT ?x ?y WHERE { ?x XML:aspectOf ?y }";
    	    TupleQuery query = conn.prepareTupleQuery(queryString);
    	    // A QueryResult is also an AutoCloseable resource, so make sure it gets 
    	    // closed when done.
    	    try (TupleQueryResult result = query.evaluate()) {
    		// we just iterate over all solutions in the result...
	    		while (result.hasNext()) {
	    		    BindingSet solution = result.next();
	    		    // ... and print out the value of the variable bindings
	    		    System.out.println(solution);
	    		    System.out.println("?s = " + solution.getValue("s"));
	    		    System.out.println("?n = " + solution.getValue("n"));
	    		}
    	    }
    	}
    	finally {
    		// before our program exits, make sure the database is properly shut down.
    		db.shutDown();
    	}
	}

	private static void printOutRDFModel(Model theRDFModel) {
    	try{
    	    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
    	    Rio.write(theRDFModel, writer, RDFFormat.TURTLE);
    	    Rio.write(theRDFModel, System.out, RDFFormat.TURTLE);
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println("Something went wrong in writing the ttl file.");
    	}
//        for(Statement statement: theRDFModel) {
//            System.out.println(statement);
//        }
    }

    public static MegaModel createMegaModel (String filepath) throws IOException {
        // getting into mega models
        ModelLoader ml = new ModelLoader();
        ml.loadFile(filepath);
        System.out.println("done loading file");
        MegaModel mM = ml.getModel();
        System.out.println("done getting mega model");
        return mM;
    }
	
    public static ModelBuilder createRDFModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace(prefix, namespace);
//      Model model = new TreeModel();
//      return model;
		System.out.println("done creating RDF model");
        return builder;
    }
	
    public static ModelBuilder extractRelation(MegaModel mm, ModelBuilder rdfModel) {
        // Getting Subject - Predicate - Object
        for (Entry<String, Set<Relation>> entry : 
            mm.getRelationshipInstanceMap().entrySet()) {
            // get String keys
            String key = entry.getKey();
            // get Relation values
            Set<Relation> megaRl = entry.getValue();
            for(Relation i: megaRl) {
                _addRelations(rdfModel, i.getSubject(), key, i.getObject());
            }
        }
        System.out.println("done adding relations");
        return rdfModel;
    }
	
    public static void _addRelations(ModelBuilder rdfModel, String subject, String predicate, String object) {
        
        // TODO make them proper IRIs
        // should they be IRI 
//    	ValueFactory vf = SimpleValueFactory.getInstance();
//    	IRI sub = vf.createIRI(prefix, subject);
//        Literal obj = vf.createLiteral(object);
//        IRI pre = vf.createIRI(prefix, predicate);
//      // or prefixed names...
//        subject = prefix + subject;
//        object = prefix + object;
//        predicate = prefix + predicate;
    	rdfModel.subject("XML:" + subject).add("XML:" + predicate, object);
//    	rdfModel.add(sub, pre, obj);
    }
    
}



















