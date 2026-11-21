name: Norman Sacket

email: nsackett@u.rochester.edu

The BayesNet and BayesNode classes contain the data structures used to represent Bayesian networks. bnInferencer performs exact inference using the Enumeration-Ask algorithm from Russel and Norvig's AI: a Modern Approach. Compiling and running bnInferencer:

javac bnInferencer.java

java bnInferencer nets/[xml file] [query variable] (variable) (evidence)

Variable-evidence must be in pairs, but any number of pairs can be used, including zero (provided the variable and evidence values are both present in the network)



bnApproxInferencer performs approximate inference with Rejection Sampling. In my testing, it took around 100,000 samples to get within 1% of the actual probability, with 10,000 getting within 2% (using many queries and evidence for the aima-alarm network). Compiling and running bnApproxInferencer:

javac bnApproxInferencer.java

java bnApproxInferencer [samples] nets/[xml file] [query variable] (variable) (evidence)

The variables and evidence work the same way as they do with bnInferencer. The approximate inference uses an additional sample number, which selects the number of simulations of the network are done.
