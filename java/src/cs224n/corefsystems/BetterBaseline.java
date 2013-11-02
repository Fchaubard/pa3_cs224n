package cs224n.corefsystems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Mention;
import cs224n.coref.Pronoun;
import cs224n.util.Pair;

public class BetterBaseline implements CoreferenceSystem {

	Map<String,List<String>> coreferentHeads;

	/**
	 * Build a list of coreferent head words for used in rule based coref system
	 */
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
				coreferentHeads.put(m.headWord(), new ArrayList<String>());
			}
			//--Iterate Over Coreferent Mention Pairs
			for(Entity e : clusters){
				for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
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
		List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
		Map<String,Entity> clusters = new HashMap<String,Entity>();
		//(for each mention...)
		for(Mention m : doc.getMentions()){
			boolean clustered = false;
			//(...get its text)
			String mentionString = m.gloss();
			//(...if we've seen this text before...)
			if(clusters.containsKey(mentionString)){
				//(...add it to the cluster)
				mentions.add(m.markCoreferent(clusters.get(mentionString)));
				clustered=true;
			} else {
				//try to find a head word match to m's head word
				//(for each previous mention...)
				for (int i=doc.indexOfMention(m)-1; i>=0 && !clustered;i--) {
					Mention h = doc.getMentions().get(i);
					//If head words are identical, or head words are coreferent
					if (m.headWord().equalsIgnoreCase(h.headWord()) || 
							(coreferentHeads.containsKey(m.headWord()) && 
									coreferentHeads.get(m.headWord()).contains(h.headWord()))) {
						String otherMentionString = h.gloss();
						//Mark as coreferent
						mentions.add(m.markCoreferent(clusters.get(otherMentionString)));
						//Add mention/entity to cluster/entity map
						clusters.put(mentionString,clusters.get(otherMentionString));
						clustered=true;
					}
				}
			}
			if (!clustered){
				//(...else create a new singleton cluster)
				ClusteredMention newCluster = m.markSingleton();
				mentions.add(newCluster);
				clusters.put(mentionString,newCluster.entity);
			}
		}
		return mentions;
	}

}
