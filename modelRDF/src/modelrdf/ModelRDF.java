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
import java.util.Scanner;
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
    
    public static String prefix;
    public static String namespace;
    public static String fileName;
    public static String query;
    public static String updateFile;
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
    	
    	// taking in user input
        String moduleName = userInput("module");
        String path = "../models/" + moduleName + ".megal";
        prefix = moduleName;
        namespace = userInput("module link");
        
        // creating the megaModel and RDFModel
        MegaModel megaModel = createMegaModel(path);
        ModelBuilder RdfModel = createRDFModel();
        
        // extracting and adding relations to the builder
        extractRelationshipInstance(megaModel, RdfModel);
        extractInstanceOf(megaModel, RdfModel);
        extractRelationshipDeclaration(megaModel, RdfModel);
        extractLinkMap(megaModel, RdfModel);
        extractSubtypes(megaModel, RdfModel);
        
        // build the model builder...
        Model modelRDF = RdfModel.build();
        System.out.println("done building the RDF model");
        
        // printing out the RDF Model in a file
        fileName = userInput("filename to save the turtle RDF in") + ".ttl";
        printOutRDFModel(modelRDF, fileName);
        
        // handles SELECT queries
        query = userInput("SELECT Query");
        selectRDF(query);
        
        // handles UPDATE queries
        query = userInput("UPDATE Query");
        updateFile = userInput("file name to save the updated turtle RDF in") + ".ttl";
        updateRDF(query, updateFile);
}
    /**
     * @param p String for specific input
     * @return s userInput
     */
	private static String userInput(String p) {
    	@SuppressWarnings("resource")
		Scanner scan = new Scanner(System.in);
    	System.out.println("Please enter the chosen " + p +" : ");
        String s = scan.nextLine();
		return s;
	}
	/**
	 * @param filepath
	 * @return mM MegaModel
	 * @throws IOException
	 */
	public static MegaModel createMegaModel (String filepath) throws IOException {
        ModelLoader ml = new ModelLoader();
        ml.loadFile(filepath);
        System.out.println("done loading file");
        MegaModel mM = ml.getModel();
        System.out.println("done getting mega model");
        return mM;
    }
    /**
     * @return builder ModelBuilder
     */
    public static ModelBuilder createRDFModel() {
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace(prefix, namespace);
		System.out.println("done creating RDF model");
        return builder;
    }
    
    /**
     * getting RelationshipInstanceMap x : z y
     * @param mm MegaModel
     * @param rdfModel
     */
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
	
    /**
     * getting instanceOfMap x : y
     * @param mm
     * @param rdfModel
     */
    public static void extractInstanceOf(MegaModel mm, ModelBuilder rdfModel) {
    	for (Entry<String, String> entry :
    		mm.getInstanceOfMap().entrySet()) {
    		String subject = entry.getKey();
    		String object = entry.getValue();
    		_addRelations(rdfModel, subject, "instanceOf", object);
    		
    	}
    	System.out.println("done adding Instances of relationships");
    }
    
    /**
     * getting RelationshipDeclarationMap z < x # y
     * @param mm
     * @param rdfModel
     */
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
    
    /**
     * getting LinkMap z = ""
     * @param mm
     * @param rdfModel
     */
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
    
    /**
     * geting Subtypes x < y
     * @param mm
     * @param rdfModel
     */
    public static void extractSubtypes(MegaModel mm, ModelBuilder rdfModel) {
    	for (Entry<String, String> entry:
    		mm.getSubtypesMap().entrySet()) {
    		String subject = entry.getKey();
    		String object = entry.getValue();
    		_addRelations(rdfModel, subject, "SubTypeOf", object);
    	}
    	System.out.println("done adding subtypes");    	
    }

    /**
     * adding statements from relations
     * @param rdfModel
     * @param subject
     * @param predicate
     * @param object
     */
    private static void _addRelations(ModelBuilder rdfModel, String subject, String predicate, String object) {
    	rdfModel.subject("XML:" + subject).add("XML:" + predicate, object);
    }
    
    /**
     * printing out RDFModels in data.ttl
     * @param theRDFModel
     */
	private static void printOutRDFModel(Model theRDFModel, String filenm) {
    	try{
    	    PrintWriter writer = new PrintWriter(filenm, "UTF-8");
    	    Rio.write(theRDFModel, writer, RDFFormat.TURTLE);
    	    System.out.println("done writing RDF model into " + filenm);
    	    writer.close();
    	} catch (IOException e) {
    		System.out.println("Something went wrong in writing the ttl file.\n");
    	}
    }
	
	/**
	 * executing SELECT queries
	 * @param selectQuery
	 * @throws RDFParseException
	 * @throws RepositoryException
	 * @throws IOException
	 */
    private static void selectRDF(String selectQuery) throws RDFParseException, RepositoryException, IOException {
    	// Create a new Repository, and a database implementation that stores everything in main memory
    	Repository db = new SailRepository(new MemoryStore());
    	db.initialize();
    	try (RepositoryConnection conn = db.getConnection()) {
    	    try (InputStream input = new FileInputStream(fileName)) {
    	    	conn.add(input, "", RDFFormat.TURTLE );
    	    }
    		String queryString = "prefix " + prefix + ": " + "<" + namespace + ">\n";
    		queryString += selectQuery;
    	    TupleQuery query = conn.prepareTupleQuery(queryString);
    	    try (TupleQueryResult result = query.evaluate()) {
    	    	System.out.println("\nThe Query:\n" + queryString + "\n");
	    		while (result.hasNext()) {
	    		    BindingSet solution = result.next();
	    		    System.out.println(solution);
	    		    //System.out.println("?x = " + solution.getValue("x"));
	    		    //System.out.println("?y = " + solution.getValue("y"));    		    	
	    		}
    	    }
    	}
    	finally {
    		db.shutDown();
    	}
	}
    
    /**
     * executing UPDATE queries
     * @param updateQuery
     * @throws RDFParseException
     * @throws RepositoryException
     * @throws IOException
     */
    private static void updateRDF(String updateQuery, String filenm) throws RDFParseException, RepositoryException, IOException {
    	Repository db = new SailRepository(new MemoryStore());
    	db.initialize();
    	try (RepositoryConnection conn = db.getConnection()) {
    	    try (InputStream input = new FileInputStream(fileName)) {
    	    	conn.add(input, "", RDFFormat.TURTLE );
    	    }
    	    String queryString = "prefix " + prefix + ": " + "<" + namespace + ">\n";
    		queryString += updateQuery;
    	    Update query = conn.prepareUpdate(queryString);
    	    query.execute();
    		try (RepositoryResult<Statement> result = conn.getStatements(null, null, null);) {
    			PrintWriter writer = new PrintWriter(filenm, "UTF-8");
				while (result.hasNext()) {
					Statement st = result.next();
					// System.out.println("db contains: " + st);
					Rio.write(st, writer, RDFFormat.TURTLE);
				}
    		}
    		System.out.println("done executing update query please check file " + filenm);
    	}
    	finally {
    		db.shutDown();
    	}
	}    
}
// XML
// https://en.wikipedia.org/wiki/XML/
// SELECT ?x ?y WHERE { ?x XML:implements ?y }
// INSERT {?x rdfs:label ?y . } WHERE {?x XML:implements ?y }




