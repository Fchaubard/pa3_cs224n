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

public class OneCluster implements CoreferenceSystem {

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

		  /**
		   * Find mentions that are exact matches of each other, and mark them as coreferent.
		   * @param doc The document to run coreference on
		   * @return The list of clustered mentions to return
		   */
		  public List<ClusteredMention> runCoreference(Document doc) {
		    //(variables)
		    List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
		    Map<String,Entity> clusters = new HashMap<String,Entity>();
		    if (doc.getMentions().size()>0 ){
			    ClusteredMention mainCluster = doc.getMentions().get(0).markSingleton();
			    mentions.add(mainCluster);
			    //(for each mention...)
			    for(int i=1; i<doc.getMentions().size(); i++){
			      //(...get its text)
			      Mention m = doc.getMentions().get(i);
			      ClusteredMention newCluster = m.markCoreferent(mainCluster);
			      mentions.add(newCluster);
			    }
			}
		    //(return the mentions)
		    return mentions;
		  }

}
