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
import cs224n.util.Pair;

public class AllSingleton implements CoreferenceSystem {

	  public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		    for(Pair<Document, List<Entity>> pair : trainingData){
		      //--Get Variables
		      Document doc = pair.getFirst();
		      List<Entity> clusters = pair.getSecond();
		      List<Mention> mentions = doc.getMentions();
		      //--Print the Document
//		      System.out.println(doc.prettyPrint(clusters));
		      //--Iterate over mentions
		      for(Mention m : mentions){
//		        System.out.println(m);
		      }
		      //--Iterate Over Coreferent Mention Pairs
		      for(Entity e : clusters){
		        for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
//		          System.out.println(""+mentionPair.getFirst() + " and " + mentionPair.getSecond() + " are coreferent");
		        }
		      }
		    }
		  }

		  
		  public List<ClusteredMention> runCoreference(Document doc) {
		    //(variables)
		    List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
		    Map<String,Entity> clusters = new HashMap<String,Entity>();
		    //(for each mention...)
		    for(Mention m : doc.getMentions()){
		      //(...get its text)
		      String mentionString = m.gloss();
		      ClusteredMention newCluster = m.markSingleton();
		      mentions.add(newCluster);
		      clusters.put(mentionString,newCluster.entity);
		    }
		    //(return the mentions)
		    return mentions;
		  }

}
