import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import java.io.File;
import java.util.*;

public class bnApproxInferencer {
	BayesNet network = new BayesNet();

	public static void main(String[] args) throws Exception {
		bnApproxInferencer bAI = new bnApproxInferencer();
		int samples = Integer.valueOf(args[0]);
		bAI.parseXML(args[1]);
		String q = args[2];
		String[] e = new String[(args.length - 3) / 2];
		String[] v = new String[(args.length - 3) / 2];
		for (int i = 3; i < args.length; i += 2) {
			e[(i - 2) / 2] = args[i];
			v[(i - 2) / 2] = args[i + 1];
		}
		bAI.rejectionSampling(q, e, v, samples);
	}
	
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
	
	public int[] priorSample(BayesNode[] order, BayesNode targ) {
		int[] x = new int[order.length];
//		for (int i = 0; i < x.length; i++) {
//			x[i] = -1;
//		}
		// foreach variable X<sub>i</sub> in X<sub>1</sub>,...,X<sub>n</sub> do
		for (int i = 0; i < order.length; i++) {
			// x[i] <- a random sample from
			// <b>P</b>(X<sub>i</sub> | parents(X<sub>i</sub>))
			//x.put(Xi, ProbUtil.randomSample(bn.getNode(Xi), x, randomizer));
			x[i] = order[i].randEvent(order, x);
		}
		return x;
	}

	public void rejectionSampling(String query, String[] evidence, String[] vals, int samples) {
		BayesNode targ = network.getNode(network.findNode(query));
		BayesNode[] ordering = network.topologicalOrder();
		int targOrderIndex = 0;
		for (int i = 0; i < ordering.length; i++) {
			if (ordering[i].equals(targ)) {
				targOrderIndex = i;
			}
		}
		int numOut = targ.numOutcomes();
		double[] dist = new double[numOut];
		double sum = 0;
		//double[] N = new double[ProbUtil.expectedSizeOfCategoricalDistribution(X)];

		// for j = 1 to N do
		for (int j = 0; j < samples; j++) {
			// <b>x</b> <- PRIOR-SAMPLE(bn)
			int[] x = priorSample(ordering, targ);
			// if <b>x</b> is consistent with e then
			if (isConsistent(x, evidence, vals)) {
				// <b>N</b>[x] <- <b>N</b>[x] + 1
				// where x is the value of X in <b>x</b>
				//N[ProbUtil.indexOf(X, x)] += 1.0;
				dist[x[targOrderIndex]] += 1.0;
				sum += 1.0;
			}
		}
		for (int i = 0; i < numOut; i++) {
			dist[i] = dist[i] / sum;
		}
		printDist(dist, targ);
	}
	
	public boolean isConsistent(int[] x, String[] evidence, String[] vals) {
		for (int i = 0; i < evidence.length; i++) {
//			if (!ap.getValue().equals(x.get(ap.getTermVariable()))) {
//				return false;
//			}
			
			if (network.getNode(network.findNode(evidence[i])).getOutcome(vals[i]) != x[i]) {
				return false;
			}
		}
		return true;
	}
	
	public void printDist(double[] dist, BayesNode target) {
		for (int i = 0; i < dist.length; i++) {
			System.out.printf(target.getOutcome(i) + ": %.3f\n", dist[i]);
		}
	}
}
