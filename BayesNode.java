import java.util.ArrayList;
import java.util.List;

public class BayesNode {
	public String varName;
	//child and parents nodes
	private List<BayesNode> children = new ArrayList<BayesNode>();
	private List<BayesNode> parents = new ArrayList<BayesNode>();
	
	//the names of possible outcomes and probabilities of those associated outcomes
	private List<String> outcomeVals = new ArrayList<String>();
	private List<Double> probs = new ArrayList<Double>();
	
	public BayesNode(String name) {
		varName = name;
	}
	
	public void setName(String n) {
		varName = n;
	}
	
	public int numOutcomes() {
		return outcomeVals.size();
	}
	
	public String getOutcome(int i) {
		return outcomeVals.get(i);
	}
	
	public int getOutcome(String val) {
		return outcomeVals.indexOf(val);
	}
	
	public List<BayesNode> getParents() {
		return parents;
	}
	
	public void addParent(BayesNode p) {
		parents.add(p);
		p.addChild(this);
	}
	
	public void addChild(BayesNode c) {
		children.add(c);
	}
	
	public void setOutcomes(List<String> outcomes) {
		for (int i = 0; i < outcomes.size(); i++) {
			outcomeVals.add(outcomes.get(i));
		}
	}
	
	public void setProbs(double[] probabilities) {
		int tableSize = outcomeVals.size();
		for (int i = 0; i < parents.size(); i++) {
			tableSize *= parents.get(i).numOutcomes();
		}
		for (int i = 0; i < tableSize; i++) {
			probs.add(probabilities[i]);
		}
	}
	
	public double getProb(String val) {
			//gets probability of a particular value for variable
			//xmlbif has the variable value changes quickest, so the value in question will step by the number of possible values
		int outcomeNum = outcomeVals.indexOf(val);
		int totalProbs = probs.size()/outcomeVals.size();
		double answer = 0;
		for (int i = outcomeNum; i < probs.size(); i += outcomeVals.size()) {
			answer += probs.get(i);
		}
		return answer/totalProbs;
	}
	
	public double getProb(int outcomeNum) {
		int totalProbs = probs.size()/outcomeVals.size();
		double answer = 0;
		for (int i = outcomeNum; i < probs.size(); i += outcomeVals.size()) {
			answer += probs.get(i);
		}
		return answer/totalProbs;
	}
	
	public double getProb(int outcomeNum, String[] evidence, String[] vals) {
		int configIndex = 0;
	    int step = 1;

	    for (int i = 0; i < parents.size(); i++) {
	        BayesNode parent = parents.get(parents.size() - (i + 1));
	        int parentIndex = -1;

	        for (int j = 0; j < evidence.length; j++) { //identify index of parent in evidence
	            if (parent.varName.equals(evidence[j])) {
	                parentIndex = j;
	                break;
	            }
	        }
	        
	        int parentOutcome = parent.getOutcome(vals[parentIndex]);
	        configIndex += parentOutcome * step;
	        step *= parent.numOutcomes();
	    }
	    return probs.get(configIndex * outcomeVals.size() + outcomeNum);
	}
	
	public int randEvent(BayesNode[] order, int[] priorEvents) {
		int[] parentEvents = new int[parents.size()];
		int parentEventsIndex = 0;
		for (int i = 0; i < order.length; i++) {
			if (parents.contains(order[i])) {
				parentEvents[parentEventsIndex++] = priorEvents[i];
			}
		}
		int probLocMin = 0;
		for (int i = 0; i < parentEvents.length; i++) {
			int blockSize = (int) Math.pow(parents.get(i).numOutcomes(), parents.size() - i);
			probLocMin = probLocMin + (parentEvents[i] * blockSize);
		}
		double rnd = Math.random();
		if (rnd <= probs.get(probLocMin)) {
			return 0;
		} else {
			return 1;
		}
	}
}
