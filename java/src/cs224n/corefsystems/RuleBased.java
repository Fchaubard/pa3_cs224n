package cs224n.corefsystems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Mention;
import cs224n.coref.Name;
import cs224n.coref.Pronoun;
import cs224n.coref.Sentence;
import cs224n.coref.Sentence.Token;
import cs224n.ling.Tree;
import cs224n.ling.Trees.PennTreeRenderer;
import cs224n.util.Pair;

public class RuleBased implements CoreferenceSystem {
	Map<String,List<String>> coreferentHeads;

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		coreferentHeads = new HashMap<String,List<String>>();
		//For each mention cluster, get list of coreferent mention heads
	    for(Pair<Document, List<Entity>> pair : trainingData){
	        //--Get Variables
	        Document doc = pair.getFirst();
	        List<Entity> clusters = pair.getSecond();
	        List<Mention> mentions = doc.getMentions();
	        //--Iterate over mentions
	        for(Mention m : mentions){
	        	if (!checkPronoun(m.headWord(),m.headToken()))
	        		coreferentHeads.put(m.headWord(), new ArrayList<String>());
	        }
	        //--Iterate Over Coreferent Mention Pairs
	        for(Entity e : clusters){
	          for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
	        	   	if (!checkPronoun(mentionPair.getFirst().headWord(),mentionPair.getFirst().headToken()) && 
	        	   			!checkPronoun(mentionPair.getSecond().headWord(),mentionPair.getSecond().headToken()))
	        	   		coreferentHeads.get(mentionPair.getFirst().headWord()).add(mentionPair.getSecond().headWord());
	          }
	        }
	      }
	}

	/**
	 * Baseline rule based coreference. Mark coreferent mentions that are exact matches, 
	 * and mentions that have the same head word, or coreferent head words
	 * @param doc The document to run coreference on
	 * @return The list of clustered mentions to return
	 */
	@Override
	public List<ClusteredMention> runCoreference(Document doc) {
		//(variables)
		//Put all in one hash, then build at the end....
		Map<Mention,Mention> corefs = new HashMap<Mention,Mention>();
		//if mention isnt in corefs, add it as a singleton cluster
		//Otherwise markcoref, and add to clusters
		
		Map<String,Mention> stringMatches = new HashMap<String,Mention>();
		//Map of string to mention...
		
		//Map<Mention,Entity> pronounClusters = new HashMap<Mention,Entity>();
		boolean clustered;
		//Evaluate all non pronoun mentions
		for(Mention m : doc.getMentions()){
			clustered = false;
			//(...get its text)
			String mentionString = m.gloss();
			//If its not a pronoun
			if (!checkPronoun(m.headWord(),m.headToken())) {
				//(...if we've seen this text before...)
				if(stringMatches.containsKey(mentionString)){
					//Mark coreferent
					corefs.put(m,stringMatches.get(mentionString));
					clustered=true;
				} else {
					//try to find a head word match to m's head word
					//(for each previous mention...)
					for (int i=doc.indexOfMention(m)-1; i>=0 && !clustered;i--) {
						Mention h = doc.getMentions().get(i);
						String otherMention = h.gloss();
						//If head words are identical, or head words are coreferent
						if (m.headWord().equalsIgnoreCase(h.headWord()) || 
								(coreferentHeads.containsKey(m.headWord()) && 
										coreferentHeads.get(m.headWord()).contains(h.headWord()))) {
							//Mark as coreferent
							stringMatches.put(mentionString, m);
							corefs.put(m,stringMatches.get(otherMention));
							clustered=true;
						}
					}
				}
				if (!clustered){
					//(...else create a new singleton cluster)
					stringMatches.put(mentionString, m);
				}
			}
		}
		for(Mention m : doc.getMentions()){
			//If it is a pronoun of interest
			if (checkPronoun(m.headWord(),m.headToken())) {
				Mention hco = hobbsCoreferent(m);
				if (hco!=null && !hco.equals(m)){
					String hcoString = hco.gloss();
					//System.out.printf("Found coreference %s\n", hcoString);
					if (stringMatches.containsKey(hcoString)){
						//mentions.add(m.markCoreferent(stringClusters.get(hcoString)));
						corefs.put(m,stringMatches.get(hcoString));
					} else{
						//Add as coreferent
						corefs.put(m, hco);
					}
				} 
			}
		}

		//Build list of mentions
		List<ClusteredMention> cMentions = new ArrayList<ClusteredMention>();
		Map<Mention,ClusteredMention> clusters = new HashMap<Mention,ClusteredMention>();
		List<Mention> mentions = new ArrayList<Mention>(doc.getMentions());
		while (!mentions.isEmpty()){
			Mention m = mentions.get(0);
			if (corefs.containsKey(m) && corefs.get(m)!=null){
				Mention h = corefs.get(m);
				if(!clusters.containsKey(h)) {
					int hIndex = mentions.indexOf(h);
					mentions.set(0, h);
					mentions.set(hIndex,m);
					if (hIndex > 0){
						ClusteredMention newCluster = h.markSingleton();
						cMentions.add(newCluster);
						mentions.remove(hIndex);
						clusters.put(h,newCluster);
						cMentions.add(m.markCoreferent(clusters.get(h)));
						mentions.remove(0);
						clusters.put(m,clusters.get(h));
					}
				} else {
				cMentions.add(m.markCoreferent(clusters.get(h)));
				mentions.remove(0);
				clusters.put(m,clusters.get(h));
				}
			} else {
				ClusteredMention newCluster = m.markSingleton();
				cMentions.add(newCluster);
				mentions.remove(0);
				clusters.put(m,newCluster);
			}
		}
		return cMentions;
	}

	//Returns if a pronoun is not first or second person
	boolean checkPronoun(String headWord, Token token){
		if (Pronoun.isSomePronoun(headWord)){
			Pronoun pn = Pronoun.valueOrNull(headWord);
			if (pn==null)
				return false;
			if (pn.speaker == Pronoun.Speaker.FIRST_PERSON || pn.speaker == Pronoun.Speaker.SECOND_PERSON)
				return false;
			else
				return true;
		} 
		return false;
	}
	
	//Finds the coreferent entity for the given pronoun
	public static Mention hobbsCoreferent(Mention m){
		Tree<String> sTree = m.sentence.parse;
		
		//System.out.printf("Searching for hobbs coreference for %s\n", m.gloss());
		//System.out.printf("Sentence %s\n", m.sentence.toString());
		//System.out.printf("Sentence Parse Tree:\n");
		//System.out.println(PennTreeRenderer.render(sTree));
		
		//Proposed node
		Mention proposedMention;

		//Get m's index in sentence
		int mIndex = m.sentence.tokens.indexOf(m.headToken());
		//Get path to m
		LinkedList<Pair<String,Integer>> pathToM = sTree.pathToIndex(mIndex);
		//Get NP above M
		int npIndex = pathToM.size()-1;
		while (npIndex>0 && !pathToM.get(npIndex).getFirst().equals("NP")){
			npIndex--;
		}
		if (npIndex==0)
			return null;
		
		//System.out.printf("Mention Parse Tree:\n");
		//System.out.printf("Top label: %s\n",pathToM.get(npIndex).getFirst());
		//System.out.println(PennTreeRenderer.render(sTree.getSubTree(pathToM.subList(0, npIndex+1))));
		

		//Go up to first NP or s
		int xIndex = npIndex-1;
		while (xIndex>0 && !pathToM.get(xIndex).getFirst().equals("NP") && !isSLabel(pathToM.get(xIndex).getFirst())){
			xIndex--;
		}
		
		//System.out.printf("First X \n");
		//System.out.printf("Top label: %s\n",pathToM.get(xIndex).getFirst());
		//System.out.println(PennTreeRenderer.render(sTree.getSubTree(pathToM.subList(0, xIndex+1))));
		
		//Search for antecedents below X to the left of p
		Tree<String> X = sTree.getSubTree(pathToM.subList(0, xIndex+1));
		proposedMention = bfsNPLeft(X,sTree.getSubTree(pathToM.subList(0, xIndex+2)),m, false);

		if (proposedMention != null)
			return proposedMention;
		//While not at the top S in the sentence
		while (!X.getLabel().equals("ROOT") && !pathToM.get(xIndex).getFirst().equals("ROOT")){
			//Go up to next NP or s
			do {
				//Move up an X
				xIndex--;
				X = sTree.getSubTree(pathToM.subList(0, xIndex+1));
			}while (!(isSLabel(X.getLabel()) || X.getLabel().equals("NP") || X.getLabel().equals("ROOT")));

			//System.out.printf("Next X \n");
			//System.out.println(PennTreeRenderer.render(X));
			
			//If path to X came from non-head phrase of X.....
			if (X.getLabel().equals("NP") && !m.parse.equals(X) && getMentionFromTree(X,m.sentence,m.doc)!=null){
				Mention propM = getMentionFromTree(X,m.sentence,m.doc);
				if (validPronoun(propM, Pronoun.valueOrNull(m.gloss())))
						return propM;
			}
			//Search for antecedents below X to the left of p
			proposedMention = bfsNPLeft(X,sTree.getSubTree(pathToM.subList(0, xIndex+2)),m,false);
			if (proposedMention != null)
				return proposedMention;
			//If X is an S node go to the right of path, but dont' go below and NP or S
			if (isSLabel(X.getLabel()) ){
				proposedMention = bfsNPRight(X,sTree.getSubTree(pathToM.subList(0, xIndex+2)),m);
				if (proposedMention != null)
					return proposedMention;
			}
		}
		//If X is highest S in sentence, traverse previous sentences doing BFS
		//Look at all previous sentences
		for (int j=m.doc.indexOfSentence(m.sentence);j<0;j++){
			proposedMention = bfsNP(m.doc.sentences.get(j).parse,m,m.doc.sentences.get(j),true);
			if (proposedMention != null)
				return proposedMention;
		}
		return null;
	}

	public static Mention bfsNPLeft(Tree<String> X, Tree<String> p, Mention m, boolean npInBetween){
		//Breadth first search for NP from each child of X left of p
		for (int j=0;j<X.getChildren().size();j++){
			if (X.getChildren().get(j).equals(p))
				return null;
			Mention propMention=bfsNP(X.getChildren().get(j),m,m.sentence,true);
			if (propMention!=null){
				if (npInBetween){
					//Check for intermediate NP
					List<Tree<String>> preT = X.getPreOrderTraversal();
					int propIndex=0, lastNPindex=0;
					for (int k=0;k<preT.size();k++){
						if (preT.get(k).equals(p))
							break;
						else if (propMention.parse.equals(preT.get(k)))
							propIndex = k;
						else if (preT.get(k).getLabel().equals("NP") || isSLabel(preT.get(k).getLabel()))
							lastNPindex = k;
					}
					if (lastNPindex > propIndex) {
						return propMention;
					}
				} else {
					return propMention;
				}
			}
		}
		return null;
	}

	public static boolean isSLabel(String label){
		return label.equals("S") || label.equals("SBARQ");
	}
	
	public static Mention bfsNPRight(Tree<String> X, Tree<String> p, Mention m){
		//Breadth first search for NP from each child of X right of p
		int i=0;
		while(i<X.getChildren().size() && !X.getChildren().get(i).equals(p)) i++;
		for (int j=i+1;j<X.getChildren().size();j++) {
			Mention propMention=bfsNP(X.getChildren().get(j),m,m.sentence,false);
			if (propMention!=null){
				return propMention;
			}
		}
		return null;
	}

	public static Mention bfsNP(Tree<String> tree, Mention m, Sentence s, boolean deepSearch){
		//Breadth first search of tree for an NP node
		Queue<Tree<String>> queue = new LinkedList<Tree<String>>();
		queue.add(tree);
		while (!queue.isEmpty()){
			Tree<String> r = queue.remove();
			if (r.getLabel().equals("NP")){
				Mention propM = getMentionFromTree(r,s,m.doc);
				if (validPronoun(propM,Pronoun.valueOrNull(m.gloss())))
					return propM;
			}
			for (Tree<String> child : r.getChildren()){
				if (deepSearch || !(r.getLabel().equals("NP") || isSLabel(r.getLabel())))
						queue.add(child);
			}
		}
		return null;
	}

	public static Mention getMentionFromTree(Tree<String> tree, Sentence s, Document doc){
		for (Mention m : doc.getMentions()){
			if (m.sentence.equals(s) && m.parse.equals(tree))
				return m;
		}
		return null;
	}
	
	//Check if pronoun assignment is valid
	public static boolean validPronoun (Mention m, Pronoun p){
		if (p==null || m ==null)
			return false;
		//System.out.printf("Checkeing pronoun %s\n",p.toString());
		//System.out.printf("Checking head word %s \n",m.headWord());
		//See if mention is a pronoun
		Pronoun p2 = Pronoun.valueOrNull(m.headWord());
		if (p2!=null){
			//Check plurality
			if (!xor(p.plural,p2.plural)){
				//System.out.println("Plural failed");
				return false;
			}
			//Check gender
			if (!p.gender.isCompatible(p2.gender)){
				//System.out.println("gender failed");
				return false;
			}
			//Check speaker
			if (!p.speaker.equals(p2.speaker)){
				//System.out.println("speaker failed");
				return false;
			}
		} else {
			//Check plurality
			if (m.headToken().isNoun() && !xor(p.plural,m.headToken().isPluralNoun())){
				//System.out.println("Plural failed");
				return false;
			}
			if (Name.isName(m.headWord())) {
				if(!p.gender.isCompatible(Name.get(m.headWord()).gender))
				{
					//System.out.println("gender failed");
					return false;
				}
			}
		}
		return true;
	}

	public static boolean xor(boolean first, boolean second){
		return (first && second) || (!first && !second);
	}
}
