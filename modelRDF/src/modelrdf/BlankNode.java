package modelrdf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.ModelBuilder;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

public class BlankNode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		

		
		
		String ns = "http://example.org/";
		ValueFactory vf = SimpleValueFactory.getInstance();
		// IRI for ex:favoriteLetters
		IRI favoriteLetters = vf.createIRI(ns, "favoriteLetters");
		// IRI for ex:John
		IRI john = vf.createIRI(ns, "John");
		// create a list of letters
		
		List<Literal> list1 = new ArrayList<>();
		
		list1.add(vf.createLiteral("F"));
		list1.add(vf.createLiteral("D"));
		list1.add(vf.createLiteral("E"));
		
//		Literal[] literals = new Literal[] {list1};
		
		
		List<Literal> letters = Arrays.asList(new Literal[] { vf.createLiteral("A"), vf.createLiteral("B"), vf.createLiteral("C") });
		// create a head resource for our list
		Resource head = vf.createBNode();
		// convert our list and add it to a newly-created Model
		Model aboutJohn = RDFCollections.asRDF(list1, head, new LinkedHashModel());
		// set the ex:favoriteLetters property to link to the head of the list
		aboutJohn.add(john, favoriteLetters, head);
		
		Rio.write(aboutJohn, System.out, RDFFormat.TURTLE);
	}

}
