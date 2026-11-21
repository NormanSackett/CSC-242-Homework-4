import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.File;
import java.util.*;

public class bnInferencer {
	BayesNet network = new BayesNet();

	//args in the form of [xmlbif_file] [query_variable] [evidence_variable evidence_value]+
	public static void main(String[] args) throws Exception {
		bnInferencer bI = new bnInferencer();
		bI.parseXML(args[0]);
		String q = args[1];
		String[] e = new String[(args.length - 2) / 2];
		String[] v = new String[(args.length - 2) / 2];
		for (int i = 2; i < args.length; i += 2) {
			e[(i - 2) / 2] = args[i];
			v[(i - 2) / 2] = args[i + 1];
		}
		bI.inferExact(q, e, v);
	}
	
	//used https://github.com/kutschkem/Jayes to help with parsing and network setup
	public void parseXML(String filepath) throws Exception {
		//xml file parsing setup
		File f = new File(filepath);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(f);
		doc.getDocumentElement().normalize();
		
		//parses variables and their outcomes and adds them to the network
		NodeList vars = doc.getElementsByTagName("VARIABLE");
		for (int i = 0; i < vars.getLength(); i++) {
				Element var = (Element) vars.item(i);
				String name = tagValue("NAME", var);
				network.addNode(name);
				List<String> outcomes = tagValues("OUTCOME", var);
				network.setOutcomes(name, outcomes);
		}
		
		//parses definitions and adds probabilities to the network
		NodeList defs = doc.getElementsByTagName("DEFINITION");
		for (int i = 0; i < defs.getLength(); i++) {
			Element def = (Element) defs.item(i);
			String varName = tagValue("FOR", def);
			List<String> parents = tagValues("GIVEN", def);
			network.setParents(varName, parents);
			
			//create an array of doubles split along whitespace characters to construct table
			//since ordering in XMLBIF is standardized, one dimension is all that's needed
			String table = tagValue("TABLE", def);
			double[] probTable = Arrays.stream(table.split("\\s+")).mapToDouble(Double::parseDouble).toArray();
			network.setProbs(varName, probTable);
		}
	}
	
	//for tags with only one value
	public String tagValue(String tag, Element e) {
		NodeList l = e.getElementsByTagName(tag);
		if (l.getLength() > 0) {
			return l.item(0).getTextContent().trim();
		}
		return "";
	}
	
	//for tags with any number of values
	public List<String> tagValues(String tag, Element e) {
		NodeList l = e.getElementsByTagName(tag);
		List<String> vals = new ArrayList<String>();
		for (int i = 0; i < l.getLength(); i++) {
			vals.add(l.item(i).getTextContent().trim());
		}
		return vals;
	}
	
	public void inferExact(String query, String[] evidence, String[] vals) {
		String[] newEv = new String[evidence.length + 1];
		String[] newVals = new String[vals.length + 1];
		for (int i = 0; i < evidence.length; i++) {
			newEv[i] = evidence[i];
			newVals[i] = vals[i];
		}
		newEv[evidence.length] = query;
		int targNum = network.findNode(query);
		BayesNode targ = network.getNode(targNum);
		int numOut = targ.numOutcomes();
		double[] dist = new double[numOut];
		double sum = 0;
		for (int i = 0; i < numOut; i++) {
			newVals[vals.length] = targ.getOutcome(i);
			dist[i] = enumerateAll(newEv, newVals, 0);
			sum += dist[i];
		}
		for (int i = 0; i < numOut; i++) {
			dist[i] = dist[i] / sum;
		}
		printDist(dist, targ);
	}
	
	public double enumerateAll(String[] evidence, String[] vals, int varNum) {
		if (varNum >= network.size()) return 1.0;
		
		BayesNode firstE = network.getNode(varNum);
		int firstIndex = findString(firstE.varName, evidence);
		if (firstIndex != -1) {
			return firstE.getProb(firstE.getOutcome(vals[firstIndex]), evidence, vals) * enumerateAll(evidence, vals, varNum + 1);
		} else {
			double probSum = 0;
			String[] newEv = new String[evidence.length + 1];
			String[] newVals = new String[vals.length + 1];
			for (int j = 0; j < evidence.length; j++) {
				newEv[j] = evidence[j];
				newVals[j] = vals[j];
			}
			newEv[evidence.length] = firstE.varName;
			for (int i = 0; i < firstE.numOutcomes(); i++) {
				newVals[vals.length] = firstE.getOutcome(i);
				
				probSum += firstE.getProb(i, newEv, newVals) * enumerateAll(newEv, newVals, varNum + 1);
			}
			//System.out.println("probSum:" + probSum);
			return probSum;	
		}
	}
	
	public int findString(String s, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (s.equals(arr[i])) {
				return i;
			}
		}
		return -1;
	}
	
	public void printDist(double[] dist, BayesNode target) {
		for (int i = 0; i < dist.length; i++) {
			System.out.printf(target.getOutcome(i) + ": %.3f\n", dist[i]);
		}
	}
}
