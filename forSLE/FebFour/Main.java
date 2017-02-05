package febFour;

import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.impl.TreeModel;
import org.java.megalib.checker.services.ModelLoader;
import org.java.megalib.models.MegaModel;
import org.java.megalib.models.Relation;

public class Main {
	
	public static String prefix = "XML";
	public static String namespace = "https://en.wikipedia.org/wiki/XML";
	
	public static void main(String[] args) throws IOException {
		String path = "";
		MegaModel megaModel = createMegaModel(path);
		Model RdfModel = createRDFModel();
		extractRelation(megaModel, RdfModel);
		printOutRDFModel(RdfModel);
	}
	
	private static void printOutRDFModel(Model rdfModel) {
		for (Statement statement: rdfModel) {
			System.out.println(statement);
		}
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
	
	public static Model createRDFModel() {
		Model model = new TreeModel();
		System.out.print("done creating RDF model");
		return model;
	}
	
	public static Model extractRelation(MegaModel mm, Model RDFM) {
		// Getting Subject - Predicate - Object
		for (Entry<String, Set<Relation>> entry : 
			mm.getRelationshipInstanceMap().entrySet()) {
			// get String keys
			String key = entry.getKey();
//			System.out.println("Predicate: " + (key));
			// get Relation values
			Set<Relation> megaRl = entry.getValue();
			for(Relation i: megaRl) {
//				System.out.println("Subject: " + (i.getSubject()));
//				System.out.println("Object: " + (i.getObject()));
				_addRelations(RDFM, i.getSubject(), key, i.getObject());
			}
		}
		System.out.print("done adding relations");
		return RDFM;
	}
	
	public static void _addRelations(Model model, String subject, String predicate, String object) {
		try {
			ValueFactory vf = SimpleValueFactory.getInstance();
			IRI sub = vf.createIRI(subject);
			IRI obj = vf.createIRI(object);
			IRI pre = vf.createIRI(predicate);
			model.add(sub, obj, pre);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(subject);
		}
	}
	

}
