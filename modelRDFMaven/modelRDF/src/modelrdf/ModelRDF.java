/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelrdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
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
import org.java.megalib.models.Function;
import org.java.megalib.models.MegaModel;
import org.java.megalib.models.Relation;

/**
 * 
 * @author Hanadi
 */
public class ModelRDF {

	public static String prefix = "megal";
	public static String namespace = "http://megal.org/";
	public static final String fileName = "Megals.ttl";
	public static String query;
	public static String updateFile;

	public static void main(String[] args) throws IOException {
		File f = null;
		if (1 < args.length) {
			f = new File(args[0]);
			if (!f.exists()) {
				System.err.println("The file '" + args[1] + "' does not exist.");
				return;
			}
			
			// if megals.ttl exists then just execute queries else create it
			File varTmpDir = new File("Megals.ttl");
			if(!varTmpDir.exists()) {
				System.out.println("megals.ttl does not exist");
				// create an empty RDF model
				ModelBuilder RdfModel = createRDFModel(prefix, namespace);

				// get all mega model paths
				List<String> modelNames = getAllModels("../models");

				// create mega models and fill RDF with relations
				for (String m : modelNames) {
					String[] strings = m.split("/");
					String[] parts = strings[strings.length - 1].split(Pattern
							.quote("."));
					String moduleName = parts[0];
					prefix = moduleName;
					namespace = "http://" + moduleName + ".org/";
					MegaModel megaModel = createMegaModel(m);

					extractRelationshipInstance(megaModel, RdfModel);
					extractInstanceOf(megaModel, RdfModel);
					extractRelationshipDeclaration(megaModel, RdfModel);
					extractLinkMap(megaModel, RdfModel);
					extractSubtypes(megaModel, RdfModel);
					extractFunctions(moduleName, megaModel, RdfModel, true);
					extractFunctions(moduleName, megaModel, RdfModel, false);
				}

				// build the rdf model
				Model modelRDF = RdfModel.build();

				// printing out the RDF Model in a file
				printOutRDFModel(modelRDF, fileName);
			}
			
			query = readQueryFile(f);
			if (args[1].contains("select")) {
				selectRDF(query);
			}
			if (args[1].contains("update")) {
				updateRDF(query, f.getName() + "_updated.ttl");
			}
		} else {
			System.err.println("Invalid arguments count:" + args.length);
			return;
		}

	}

	public static String readQueryFile(File f) throws FileNotFoundException {
		StringBuilder fileContents = new StringBuilder((int) f.length());
		Scanner scanner = new Scanner(f);
		String lineSeparator = System.getProperty("line.separator");
		try {
			while (scanner.hasNextLine()) {
				fileContents.append(scanner.nextLine() + lineSeparator);
			}
			return fileContents.toString();
		} finally {
			scanner.close();
		}
	}

	public static List<String> getAllModels(String directory) {
		File folder = new File(directory);
		File[] listOfFiles = folder.listFiles();
		List<String> fileNames = new ArrayList<String>();
		try {
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					String path = listOfFiles[i].getPath();
					if (!path.endsWith("md")) {
						fileNames.add(path);
					}
				} else if (listOfFiles[i].isDirectory()) {
					String dir = listOfFiles[i].getName();
					String newDirectory = directory + "/" + dir;
					getAllModels(newDirectory);
				}
			}
		} catch (NullPointerException e) {
			System.out.println(e);
		}
		return fileNames;
	}

	public static MegaModel createMegaModel(String filepath) throws IOException {
		ModelLoader ml = new ModelLoader();
		ml.loadFile(filepath);
		// System.out.println("done loading file");
		MegaModel mM = ml.getModel();
		// System.out.println("done getting mega model");
		return mM;
	}

	public static ModelBuilder createRDFModel(String prfx, String nmspace) {
		ModelBuilder builder = new ModelBuilder();
		builder.setNamespace(prfx, nmspace);
		// System.out.println("done creating RDF model");
		return builder;
	}

	public static void extractRelationshipInstance(MegaModel mm,
			ModelBuilder rdfModel) {
		for (Entry<String, Set<Relation>> entry : mm
				.getRelationshipInstanceMap().entrySet()) {
			String predicate = entry.getKey();
			Set<Relation> megaRl = entry.getValue();
			for (Relation i : megaRl) {
				String subject = i.getSubject();
				String object = i.getObject();
				_addRelations(rdfModel, subject, predicate, object, false);
			}
		}
		// System.out.println("done adding relationship instances");
	}

	public static void extractInstanceOf(MegaModel mm, ModelBuilder rdfModel) {
		for (Entry<String, String> entry : mm.getInstanceOfMap().entrySet()) {
			String subject = entry.getKey();
			String object = entry.getValue();
			_addRelations(rdfModel, subject, "instanceOf", object, false);

		}
		// System.out.println("done adding Instances of relationships");
	}

	public static void extractRelationshipDeclaration(MegaModel mm,
			ModelBuilder rdfModel) {
		for (Entry<String, Set<Relation>> entry : mm
				.getRelationshipDeclarationMap().entrySet()) {
			String predicate = entry.getKey();
			Set<Relation> megaRl = entry.getValue();
			for (Relation i : megaRl) {
				String subject = i.getSubject();
				String object = i.getObject();
				_addRelations(rdfModel, subject, predicate, object, false);
			}
		}
		// System.out.println("done adding relationship declarations");
	}

	public static void extractLinkMap(MegaModel mm, ModelBuilder rdfModel) {
		for (Entry<String, Set<String>> entry : mm.getLinkMap().entrySet()) {
			String subject = entry.getKey();
			Set<String> link = entry.getValue();
			for (String i : link) {
				String object = i;
				_addRelations(rdfModel, subject, "Link", object, false);
			}
		}
		// System.out.println("done adding links");
	}

	public static void extractSubtypes(MegaModel mm, ModelBuilder rdfModel) {
		for (Entry<String, String> entry : mm.getSubtypesMap().entrySet()) {
			String subject = entry.getKey();
			String object = entry.getValue();
			_addRelations(rdfModel, subject, "SubTypeOf", object, false);
		}
		// System.out.println("done adding subtypes");
	}

	public static void extractFunctions(String modelname, MegaModel mm,
			ModelBuilder rdfModel, boolean flag) {
		ValueFactory vf = SimpleValueFactory.getInstance();
		String subject = modelname;

		if (flag) {
			for (Entry<String, Set<Function>> entry : mm
					.getFunctionDeclarations().entrySet()) {
				String predicate = "hasFunctionDec";
				String object = entry.getKey();
				_addRelations(rdfModel, subject, predicate, object, true); // model hasFunction function
				Resource inputNode = vf.createBNode();
				rdfModel.subject(prefix + ":" + object).add(
						prefix + ":hasRange", inputNode); // function hasRange _blankNode
				List<Literal> inputs = new ArrayList<Literal>();
				List<Literal> outputs = new ArrayList<Literal>();
				Set<Function> megaRl = entry.getValue();
				for (Function i : megaRl) {
					for (String x : i.getInputs()) {
						inputs.add(vf.createLiteral(x));
					}
					for (String z : i.getOutputs()) {
						outputs.add(vf.createLiteral(z));
					}
				}
				Model inputsMod = RDFCollections.asRDF(inputs, inputNode,
						new LinkedHashModel());
				for (Statement s : inputsMod) {
					rdfModel.subject(s.getSubject()).add(s.getPredicate(),
							s.getObject());
				}
				Resource outputNode = vf.createBNode();
				rdfModel.subject(prefix + ":" + object).add(
						prefix + ":hasDomain", outputNode); // function hasDomain _blankNode
				Model outputsMod = RDFCollections.asRDF(outputs, outputNode,
						new LinkedHashModel());
				for (Statement s : outputsMod) {
					rdfModel.subject(s.getSubject()).add(s.getPredicate(),
							s.getObject());
				}
			}
			// System.out.println("done adding function declarations");
		} else {
			for (Entry<String, Set<Function>> entry : mm
					.getFunctionApplications().entrySet()) {
				String predicate = "hasFunctionApp";
				String object = entry.getKey();
				_addRelations(rdfModel, subject, predicate, object, true); // model hasFunction function
				Resource inputNode = vf.createBNode();
				rdfModel.subject(prefix + ":" + object).add(
						prefix + ":hasRange", inputNode); // function hasRange _blankNode
				List<Literal> inputs = new ArrayList<Literal>();
				List<Literal> outputs = new ArrayList<Literal>();
				Set<Function> megaRl = entry.getValue();
				for (Function i : megaRl) {
					for (String x : i.getInputs()) {
						inputs.add(vf.createLiteral(x));
					}
					for (String z : i.getOutputs()) {
						outputs.add(vf.createLiteral(z));
					}
				}
				Model inputsMod = RDFCollections.asRDF(inputs, inputNode,
						new LinkedHashModel());
				for (Statement s : inputsMod) {
					rdfModel.subject(s.getSubject()).add(s.getPredicate(),
							s.getObject());
				}
				Resource outputNode = vf.createBNode();
				rdfModel.subject(prefix + ":" + object).add(
						prefix + ":hasDomain", outputNode); // function hasDomain _blankNode
				Model outputsMod = RDFCollections.asRDF(outputs, outputNode,
						new LinkedHashModel());
				for (Statement s : outputsMod) {
					rdfModel.subject(s.getSubject()).add(s.getPredicate(),
							s.getObject());
				}
			}
			// System.out.println("done adding function applications");
		}

	}

	private static void _addRelations(ModelBuilder rdfModel, String subject,
			String predicate, String object, boolean flag) {
		if (flag) {
			ValueFactory vf = SimpleValueFactory.getInstance();
			IRI object2 = vf.createIRI(namespace + object);
			IRI subject2 = vf.createIRI(namespace + subject);
			IRI predicate2 = vf.createIRI(namespace + predicate);
			rdfModel.subject(subject2).add(predicate2, object2);
		} else {
			rdfModel.subject(prefix + ":" + subject).add(
					prefix + ":" + predicate, object);
		}
	}

	private static void printOutRDFModel(Model theRDFModel, String filenm) {
		try {
			PrintWriter writer = new PrintWriter(filenm, "UTF-8");
			Rio.write(theRDFModel, writer, RDFFormat.TURTLE);
			System.out.println("Triple store created in: " + filenm);
			writer.close();
		} catch (IOException e) {
			System.out.println("Something went wrong while making the triple store ttl file.\n");
		}
	}

	private static void selectRDF(String selectQuery) throws RDFParseException,
			RepositoryException, IOException {
		// Create a new Repository, and a database implementation that stores everything in main memory
		Repository db = new SailRepository(new MemoryStore());
		db.initialize();
		try (RepositoryConnection conn = db.getConnection()) {
			try (InputStream input = new FileInputStream(fileName)) {
				conn.add(input, "", RDFFormat.TURTLE);
			}
			FileOutputStream out = new FileOutputStream("selectResults");
			String queryString = "prefix " + prefix + ": " + "<" + namespace
					+ ">\n";
			queryString += selectQuery;
			conn.prepareTupleQuery(queryString).evaluate(new SPARQLResultsCSVWriter(System.out));
			conn.prepareTupleQuery(queryString).evaluate(new SPARQLResultsCSVWriter(out));
			System.out.println("Check file \"selectResults\" for results");
		} finally {
			db.shutDown();
		}
	}

	private static void updateRDF(String updateQuery, String filenm)
			throws RDFParseException, RepositoryException, IOException {
		Repository db = new SailRepository(new MemoryStore());
		db.initialize();
		try (RepositoryConnection conn = db.getConnection()) {
			try (InputStream input = new FileInputStream(fileName)) {
				conn.add(input, "", RDFFormat.TURTLE);
			}
			String queryString = "prefix " + prefix + ": " + "<" + namespace
					+ ">\n";
			queryString += updateQuery;
			Update query = conn.prepareUpdate(queryString);
			query.execute();
			try (RepositoryResult<Statement> result = conn.getStatements(null,
					null, null);) {
				PrintWriter writer = new PrintWriter(filenm, "UTF-8");
				while (result.hasNext()) {
					Statement st = result.next();
					// System.out.println("db contains: " + st);
					Rio.write(st, writer, RDFFormat.TURTLE);
				}
			}
			System.out.println("done executing update query please check file "
					+ filenm);
		} finally {
			db.shutDown();
		}
	}
}