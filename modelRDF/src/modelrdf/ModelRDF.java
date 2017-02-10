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

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
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
        String path = "../models/XML.megal";
        MegaModel megaModel = createMegaModel(path);
        ModelBuilder RdfModel = createRDFModel();
        extractRelationshipInstance(megaModel, RdfModel);
        extractInstanceOf(megaModel, RdfModel);
        extractRelationshipDeclaration(megaModel, RdfModel);
        extractLinkMap(megaModel, RdfModel);
        extractSubtypes(megaModel, RdfModel);
        Model modelRDF = RdfModel.build();
        System.out.println("done building the RDF model");
        printOutRDFModel(modelRDF);
        updateRDF();
        selectRDF();
}
    
    private static void updateRDF() throws RDFParseException, RepositoryException, IOException {
    	Repository db = new SailRepository(new MemoryStore());
    	db.initialize();
    	try (RepositoryConnection conn = db.getConnection()) {
    	    try (InputStream input = new FileInputStream(fileName)) {
    		conn.add(input, "", RDFFormat.TURTLE );
    	    }
    	    String queryString  = "prefix XML: <https://en.wikipedia.org/wiki/XML/>\n";
    		queryString += "INSERT {?x rdfs:label ?y . } WHERE {?x XML:implements ?y }";
    	    Update query = conn.prepareUpdate(queryString);
    	    query.execute();
    	    System.out.println("done executing update query");
    		try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
    			PrintWriter writer = new PrintWriter("update.ttl", "UTF-8");
				while (result.hasNext()) {
					Statement st = result.next();
//					System.out.println("db contains: " + st);
					Rio.write(st, writer, RDFFormat.TURTLE);
				}
    		}
    	    
    	}
    	finally {
    		db.shutDown();
    	}
	}

	private static void selectRDF() throws RDFParseException, RepositoryException, IOException {
    	// Create a new Repository, and a database implementation that stores everything in main memory
    	Repository db = new SailRepository(new MemoryStore());
    	db.initialize();
    	try (RepositoryConnection conn = db.getConnection()) {
    	    try (InputStream input = new FileInputStream(fileName)) {
    		conn.add(input, "", RDFFormat.TURTLE );
    	    }
//    		try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
//    			while (result.hasNext()) {
//    				Statement st = result.next();
//    				System.out.println("db contains: " + st);
//    			}
//    		}
    		String queryString  = "prefix XML: <https://en.wikipedia.org/wiki/XML/>\n";
    		queryString  += "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n";
    		queryString += "SELECT ?x ?y WHERE { ?x XML:implements ?y }";
    	    TupleQuery query = conn.prepareTupleQuery(queryString);
    	    try (TupleQueryResult result = query.evaluate()) {
    	    	System.out.println("\nThe Query: " + queryString + "\n");
	    		while (result.hasNext()) {
	    		    BindingSet solution = result.next();
	    		    System.out.println(solution);
//	    		    System.out.println("?x = " + solution.getValue("x"));
//	    		    System.out.println("?y = " + solution.getValue("y"));
	    		}
    	    }
    	}
    	finally {
    		db.shutDown();
    	}
	}

	private static void printOutRDFModel(Model theRDFModel) {
    	try{
    	    PrintWriter writer = new PrintWriter(fileName, "UTF-8");
    	    Rio.write(theRDFModel, writer, RDFFormat.TURTLE);
//    	    Rio.write(theRDFModel, System.out, RDFFormat.TURTLE);
    	    System.out.println("done writing RDF model into a file");
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println("Something went wrong in writing the ttl file.");
    	}
    }

    public static MegaModel createMegaModel (String filepath) throws IOException {
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
		System.out.println("done creating RDF model");
        return builder;
    }
	
    // getting RelationshipInstanceMap x : z y
    public static void extractRelationshipInstance(MegaModel mm, ModelBuilder rdfModel) {
        for (Entry<String, Set<Relation>> entry : 
            mm.getRelationshipInstanceMap().entrySet()) {
            String predicate = entry.getKey();
            Set<Relation> megaRl = entry.getValue();
            for(Relation i: megaRl) {
            	String subject = i.getSubject();
            	String object = i.getObject();
                _addRelations(rdfModel, subject, predicate, object);
            }
        }
        System.out.println("done adding relationship instances");
    }
	
    // getting instanceOfMap x : y
    public static void extractInstanceOf(MegaModel mm, ModelBuilder rdfModel) {
    	for (Entry<String, String> entry :
    		mm.getInstanceOfMap().entrySet()) {
    		String subject = entry.getKey();
    		String object = entry.getValue();
    		_addRelations(rdfModel, subject, "instanceOf", object);
    		
    	}
    	System.out.println("done adding Instances of relationships");
    }
    
    // getting RelationshipDeclarationMap z < x # y
    public static void extractRelationshipDeclaration(MegaModel mm, ModelBuilder rdfModel) {   	
    	for (Entry<String, Set<Relation>> entry:
    		mm.getRelationshipDeclarationMap().entrySet()) {
    		String predicate = entry.getKey();
    		Set<Relation> megaRl = entry.getValue();
    		for (Relation i: megaRl) {
    			String subject = i.getSubject();
    			String object = i.getObject();
    			_addRelations(rdfModel, subject, predicate, object);
    		}
    	}
    	System.out.println("done adding relationship declarations");
    }
    
    // getting LinkMap z = ""
    public static void extractLinkMap(MegaModel mm, ModelBuilder rdfModel) {
    	for (Entry<String, Set<String>> entry:
    		mm.getLinkMap().entrySet()) {
    		String subject = entry.getKey();
    		Set<String> link = entry.getValue();
    		for (String i: link) {
    			String object = i;
    			_addRelations(rdfModel, subject, "Link", object);
    		}
    	}
    	System.out.println("done adding links");
    }
    
    // geting Subtypes x < y
    public static void extractSubtypes(MegaModel mm, ModelBuilder rdfModel) {
    	for (Entry<String, String> entry:
    		mm.getSubtypesMap().entrySet()) {
    		String subject = entry.getKey();
    		String object = entry.getValue();
    		_addRelations(rdfModel, subject, "SubTypeOf", object);
    	}
    	System.out.println("done adding subtypes");    	
    }

    // adding statements
    public static void _addRelations(ModelBuilder rdfModel, String subject, String predicate, String object) {
    	rdfModel.subject("XML:" + subject).add("XML:" + predicate, object);
    }
    
}



















