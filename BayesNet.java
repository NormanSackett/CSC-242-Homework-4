import java.util.ArrayList;
import java.util.List;

public class BayesNet {
	public String networkName;
	private List<BayesNode> nodes = new ArrayList<BayesNode>();
	
	public BayesNet() {
		networkName = "Bayesian Network";
	}
	
	public void setName(String n) {
		networkName = n;
	}
	
	public BayesNode getNode(int i) {
		return nodes.get(i);
	}
	
	public int size() {
		return nodes.size();
	}
	
	public void addNode(String nodeName) {
		nodes.add(new BayesNode(nodeName));
	}
	
	public void setOutcomes(String nodeName, List<String> outcomes) {
		nodes.get(findNode(nodeName)).setOutcomes(outcomes);
	}
	
	public void setProbs(String nodeName, double[] probs) {
		nodes.get(findNode(nodeName)).setProbs(probs);
	}
	
	public void setParents(String childNode, List<String> parents) {
		int childIndex = findNode(childNode);
		for (int i = 0; i < parents.size(); i++) {
			nodes.get(childIndex).addParent(nodes.get(findNode(parents.get(i))));
		}
	}
	
	public int findNode(String name) {
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).varName.equals(name)) {
				return i;
			}
		}
		return -1;
	}
	
	public BayesNode[] topologicalOrder() {
		BayesNode[] order = new BayesNode[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			//System.out.println("node " + i + ": " + nodes.get(i).varName);
			int orderIndex = indexOf(order, nodes.get(i));
			if (orderIndex == -1) {
				order[i] = nodes.get(i);
				List<BayesNode> p = nodes.get(i).getParents();
				//System.out.println(p.size());
				for (int j = 0; j < p.size(); j++) {
					//System.out.println("parent " + j + ": " + p.get(j).varName);
					int parentIndex = indexOf(order, p.get(j));
					if (parentIndex == -1) {
						BayesNode temp = order[i];
						order[i] = p.get(j);
						order[++i] = temp;
					}
				}
			}
		}
		return order;
	}
	
	private int indexOf(BayesNode[] order, BayesNode n) {
		for (int i = 0; i < order.length; i++) {
			if (order[i] != null && order[i].equals(n)) {
				return i;
			}
		}
		return -1;
	}
}
