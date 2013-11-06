package cs224n.corefsystems;

import cs224n.coref.*;
import cs224n.coref.Feature.FixedIsButCandidateIsNotPronoun;
import cs224n.coref.Feature.FixedIsNoun;
import cs224n.coref.Feature.FixedMentionSize;
import cs224n.coref.Feature.FixedWordLength;
import cs224n.coref.Feature.IsCapitalizedFixed;
import cs224n.coref.Feature.IsLeafFixed;
import cs224n.coref.Feature.IsMentionBetweenFixedAndCandidate;
import cs224n.coref.Feature.IsPhrasalFixed;
import cs224n.coref.Feature.IsPossessiveFixed;
import cs224n.coref.Feature.IsPretermFixed;
import cs224n.coref.Feature.NeitherArePronouns;
import cs224n.coref.Pronoun.Speaker;
import cs224n.coref.Pronoun.Type;
import cs224n.util.Pair;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearClassifierFactory;
import edu.stanford.nlp.classify.RVFDataset;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Triple;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import edu.stanford.nlp.util.logging.StanfordRedwoodConfiguration;
import cs224n.corefsystems.RuleBased;
import java.text.DecimalFormat;
import java.util.*;

import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public class ClassifierBased implements CoreferenceSystem {

	private static <E> Set<E> mkSet(E[] array){
		Set<E> rtn = new HashSet<E>();
		Collections.addAll(rtn, array);
		return rtn;
	}

	private static final Set<Object> ACTIVE_FEATURES = mkSet(new Object[]{

			/*
			 * TODO: Create a set of active features
			 */

			//////////////////////////////////////
			// 5 Low Hanging Fruit Features    //
			//////////////////////////////////////
			
			/*Feature.ExactMatch.class,
			Feature.IsSameSex.class, // doesnt matter
			Feature.IsMentionBetweenFixedAndCandidate.class,
			Feature.SameHeadWord.class, // matter a lot
			Feature.IsHobbs.class,
			
			
			
			

			//////////////////////////////////////
			// Attempts to fix Pronoun issues   //
			//////////////////////////////////////
			
			//Feature.FixedIsPronoun.class, 
			//Feature.CandidateIsPronoun.class, 
			/*Feature.NeitherArePronouns.class, 
			//Pair.make(Feature.ExactMatch.class, Feature.SameNumber.class),
			//Pair.make(Feature.SameSpeaker.class, Feature.SameHeadWord.class),
			Feature.IsFirstPersonSpeakerCandidate.class,
			Pair.make(Feature.IsHobbs.class, Feature.IsFirstPersonSpeakerCandidate.class),
			Pair.make(Feature.IsHobbs.class, Feature.FixedIsButCandidateIsNotPronoun.class),
			//Feature.BothArePronouns.class,
			//Feature.FixedIsButCandidateIsNotPronoun.class,
			//Feature.CandidateIsButFixedIsNotPronoun.class,
			
			
			
			
			
			//////////////////////////////////////
			// Attempts to fix Exact Match issues   //
			//////////////////////////////////////

			
			
			//Pair.make(Feature.BothAreCapitalizedAndNotPronouns.class, Feature.ExactMatch.class),
			
			/*Feature.IsCapitalizedFixed.class, 
			Feature.IsCapitalizedCandidate.class, 
			Feature.IsPossessiveFixed.class, 
			Feature.IsPossessiveCandidate.class,*/
			//Feature.BothAreCapitalizedAndNotPronouns.class,
			//
			
			
			
			
			//////////////////////////////////////
			// other features to try out       //
			//////////////////////////////////////
			/*Feature.JaccardBuckets.class, // at 10 buckets matters a bit
			Feature.SameNumber.class,
			
			Feature.MentionDistance.class,  // a little bit but reallllly low
			Feature.SameSpeaker.class,
			Feature.SamePOS.class,
			Feature.SameNER.class,
			
			Feature.HeadWordMentionDistance.class, // doesnt matter
			Feature.NamedEntityTypeCandidate.class, // candidates REALLY dont matter
			Feature.NamedEntityTypeFixed.class, // fixed matter alittle
			Feature.SpeakerTypeCandidate.class,
			Feature.POSTypeFixed.class,
			Feature.POSTypeCandidate.class,
			
			Feature.SpeakerTypeFixed.class,
			Feature.IsProperNounFixed.class, //doesnt matter
			Feature.IsProperNounCandidate.class,//doesnt matter
			Feature.IsSameNPersonSpeaker.class, //doesnt matter
			Feature.IsNthPersonSpeakerFixed.class,
			
			Feature.IsNthPersonSpeakerCandidate.class,
			Feature.NthTypeFixed.class,
			Feature.NthTypeCandidate.class,
			Feature.MentionDistanceViaMention.class,
			Feature.MentionDistanceViaSentence.class,
			
			Feature.IsLeafFixed.class, 
			Feature.IsLeafCandidate.class, 
			//Pair.make(Feature.IsLeafFixed.class, Feature.IsLeafCandidate.class),
			
			Feature.IsPhrasalFixed.class, 
			Feature.IsPhrasalCandidate.class, 
			Feature.IsPretermFixed.class, 
			Feature.IsPretermCandidate.class,
			Feature.ParseLabelFixed.class, // REALLY MATTERED!
			Feature.ParseLabelCandidate.class, // YES! 
			Feature.FixedMentionSize.class, // matters alittle
			
			
			//Feature.IsQuotedFixed.class,
			//Feature.IsQuotedCandidate.class, // not  important
			/*Feature.IsCapitalizedFixed.class, 
			Feature.IsCapitalizedCandidate.class, 
			Feature.IsPossessiveFixed.class, 
			Feature.IsPossessiveCandidate.class,*/
			//Feature.BothAreCapitalizedAndNotPronouns.class,
			//Pair.make(Feature.BothAreCapitalizedAndNotPronouns.class, Feature.ExactMatch.class),
			//Feature.CandidateMentionSize.class, // this and the 1 above made our performance go down!!!!   */
			//Feature.IsMentionBetweenFixedAndCandidate.class,
			//Feature.IsFirstPersonSpeakerFixed.class,
			//Feature.IsFirstPersonSpeakerCandidate.class,
			//Pair.make(Feature.IsFirstPersonSpeakerFixed.class, Feature.IsFirstPersonSpeakerCandidate.class),
			//Feature.FixedIsNoun.class,
			//Feature.CandidateIsNoun.class, 
			//Feature.FixedWordLength.class, 
			//Feature.CandidateWordLength.class,
			//Pair.make(Feature.CandidateIsNoun.class, Feature.FixedIsNoun.class),
			//Feature.PronounTypeCandidate.class,
			//Feature.PronounTypeFixed.class,
			/*Feature.ExactMatch.class,
			Feature.IsSameSex.class, // doesnt matter
			Feature.IsMentionBetweenFixedAndCandidate.class,
			Feature.SameHeadWord.class, // matter a lot
			Feature.IsHobbs.class,
			
			//Good!*/
			//Feature.ExactMatch.class
			//Feature.SameHeadWord.class, 
			//Feature.MentionDistance.class,
			
			//skeleton for how to create a pair feature
			//Pair.make(Feature.IsFeature1.class, Feature.IsFeature2.class),
			
			
			
			
			//////////////////////////////////////
			// final list of good features     //
			//////////////////////////////////////
			Feature.ExactMatch.class,
			Feature.IsSameSex.class, // doesnt matter
			Feature.FixedIsPronoun.class, 
			Feature.CandidateIsPronoun.class, 
			Feature.SameNumber.class,
			Feature.NeitherArePronouns.class, 
			//Pair.make(Feature.ExactMatch.class, Feature.SameNumber.class),
			//Pair.make(Feature.SameSpeaker.class, Feature.SameHeadWord.class),
			Feature.IsHobbs.class,
			Pair.make(Feature.IsHobbs.class, Feature.IsFirstPersonSpeakerCandidate.class),
			Pair.make(Feature.IsHobbs.class, Feature.FixedIsButCandidateIsNotPronoun.class),
			
			//Feature.BothArePronouns.class,
			Feature.FixedIsButCandidateIsNotPronoun.class,
			Feature.CandidateIsButFixedIsNotPronoun.class,
			Feature.SameHeadWord.class, // matter a lot
			Feature.MentionDistance.class,  // a little bit but reallllly low
			Feature.SameSpeaker.class,
			Feature.SamePOS.class,
			Feature.SameNER.class,
			
			Feature.HeadWordMentionDistance.class, // doesnt matter
			Feature.NamedEntityTypeCandidate.class, // candidates REALLY dont matter
			Feature.NamedEntityTypeFixed.class, // fixed matter alittle
			Feature.SpeakerTypeCandidate.class,
			Feature.POSTypeFixed.class,
			Feature.POSTypeCandidate.class,
			
			Feature.SpeakerTypeFixed.class,
			Feature.IsProperNounFixed.class, //doesnt matter
			Feature.IsProperNounCandidate.class,//doesnt matter
			Feature.IsSameNPersonSpeaker.class, //doesnt matter
			Feature.IsNthPersonSpeakerFixed.class,
			
			Feature.IsNthPersonSpeakerCandidate.class,
			Feature.NthTypeFixed.class,
			Feature.NthTypeCandidate.class,
			Feature.MentionDistanceViaMention.class,
			Feature.MentionDistanceViaSentence.class,
			
			Feature.IsLeafFixed.class, 
			Feature.IsLeafCandidate.class, 
			//Pair.make(Feature.IsLeafFixed.class, Feature.IsLeafCandidate.class),
			
			Feature.IsPhrasalFixed.class, 
			Feature.IsPhrasalCandidate.class, 
			Feature.IsPretermFixed.class, 
			Feature.IsPretermCandidate.class,
			Feature.ParseLabelFixed.class, // REALLY MATTERED!
			Feature.ParseLabelCandidate.class, // YES! 
			//Feature.IsQuotedFixed.class,
			//Feature.IsQuotedCandidate.class, // not  important
			Feature.FixedMentionSize.class, // matters alittle
	});


	private LinearClassifier<Boolean,Feature> classifier;

	public ClassifierBased(){
		StanfordRedwoodConfiguration.setup();
		RedwoodConfiguration.current().collapseApproximate().apply();
	}

	public FeatureExtractor<Pair<Mention,ClusteredMention>,Feature,Boolean> extractor = new FeatureExtractor<Pair<Mention, ClusteredMention>, Feature, Boolean>() {
		private <E> Feature feature(Class<E> clazz, Pair<Mention,ClusteredMention> input, Option<Double> count){
			
			//--Variables
			Mention onPrix = input.getFirst(); //the first mention (referred to as m_i in the handout)
			Mention candidate = input.getSecond().mention; //the second mention (referred to as m_j in the handout)
			Entity candidateCluster = input.getSecond().entity; //the cluster containing the second mention

			//--Features
			if(clazz.equals(Feature.ExactMatch.class)){
				//(exact string match)
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"exact", (new Feature.ExactMatch(onPrix.gloss().equals(candidate.gloss())).value? 1 : 0));
				return new Feature.ExactMatch(onPrix.gloss().equals(candidate.gloss()));
			//} else if(clazz.equals(Feature.NewFeach.class)) {
				/*
				 * TODO: Add features to return for specific classes. Implement calculating values of features here.
				 */
				//return new Feature.NewFeach( onPrix. );
			} else if(clazz.equals(Feature.IsSameSex.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"same sex",  (new Feature.IsSameSex( onPrix,candidate )).value? 1 : 0);
				return new Feature.IsSameSex( onPrix,candidate );
			
			} else if(clazz.equals(Feature.SameNumber.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"same num", (new Feature.SameNumber( onPrix,candidate )).value? 1 : 0);
				return new Feature.SameNumber( onPrix,candidate );
			
			} else if(clazz.equals(Feature.FixedIsPronoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"FixedIsPron", (new Feature.FixedIsPronoun( Pronoun.isSomePronoun(onPrix.gloss()) )).value? 1 : 0);
				return new Feature.FixedIsPronoun( Pronoun.isSomePronoun(onPrix.gloss()) );
				
			} else if(clazz.equals(Feature.CandidateIsPronoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"CandidateIsPron", (new Feature.CandidateIsPronoun( Pronoun.isSomePronoun(candidate.gloss()) )).value? 1 : 0);
				return new Feature.CandidateIsPronoun( Pronoun.isSomePronoun(candidate.gloss()) );
			
			} else if(clazz.equals(Feature.NeitherArePronouns.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NeitherArePronouns", (new Feature.NeitherArePronouns( !Pronoun.isSomePronoun(onPrix.gloss()) &&  !Pronoun.isSomePronoun(candidate.gloss()) )).value ? 1 : 0);
				return new Feature.NeitherArePronouns( !Pronoun.isSomePronoun(onPrix.gloss()) &&  !Pronoun.isSomePronoun(candidate.gloss()) );
			
			} else if(clazz.equals(Feature.BothArePronouns.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"BothArePronouns", (new Feature.BothArePronouns( Pronoun.isSomePronoun(onPrix.gloss()) &&  Pronoun.isSomePronoun(candidate.gloss()) )).value ? 1 : 0);
				return new Feature.BothArePronouns( Pronoun.isSomePronoun(onPrix.gloss()) &&  Pronoun.isSomePronoun(candidate.gloss()) );
			
			} else if(clazz.equals(Feature.CandidateIsButFixedIsNotPronoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"CandidateIsButFixedIsNotPronoun", (new Feature.CandidateIsButFixedIsNotPronoun( !Pronoun.isSomePronoun(onPrix.gloss()) &&  Pronoun.isSomePronoun(candidate.gloss()) )).value ? 1 : 0);
				return new Feature.CandidateIsButFixedIsNotPronoun( !Pronoun.isSomePronoun(onPrix.gloss()) &&  Pronoun.isSomePronoun(candidate.gloss()) );
			
			} else if(clazz.equals(Feature.FixedIsButCandidateIsNotPronoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"FixedIsButCandidateIsNotPronoun", (new Feature.FixedIsButCandidateIsNotPronoun( Pronoun.isSomePronoun(onPrix.gloss()) &&  !Pronoun.isSomePronoun(candidate.gloss()) )).value ? 1 : 0);
				return new Feature.FixedIsButCandidateIsNotPronoun( Pronoun.isSomePronoun(onPrix.gloss()) &&  !Pronoun.isSomePronoun(candidate.gloss()) );
			
			} else if(clazz.equals(Feature.SameHeadWord.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SameHeadWord", (new Feature.SameHeadWord( onPrix,candidate )).value ? 1 : 0);
				return new Feature.SameHeadWord( onPrix,candidate );
			
			} else if(clazz.equals(Feature.MentionDistance.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"MentionDistance", (new Feature.MentionDistance( onPrix,candidate )).value);
				return new Feature.MentionDistance( onPrix,candidate );
			
			} else if(clazz.equals(Feature.MentionDistanceViaSentence.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"MentionDistanceViaSentence", (new Feature.MentionDistanceViaSentence( onPrix,candidate )).value);
				return new Feature.MentionDistanceViaSentence( onPrix,candidate );
			
			} else if(clazz.equals(Feature.MentionDistanceViaMention.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"MentionDistanceViaMention", (new Feature.MentionDistanceViaMention( onPrix,candidate )).value);
				return new Feature.MentionDistanceViaMention( onPrix,candidate );
			
			} else if(clazz.equals(Feature.SameSpeaker.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SameSpeaker", (new Feature.SameSpeaker( onPrix,candidate )).value?1:0);
				return new Feature.SameSpeaker( onPrix,candidate );
			
			}  else if(clazz.equals(Feature.SamePOS.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SamePOS", (new Feature.SamePOS( onPrix,candidate )).value?1:0);
				return new Feature.SamePOS( onPrix,candidate );
			
			}  else if(clazz.equals(Feature.SameNER.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SameNER", (new Feature.SameNER( onPrix,candidate )).value?1:0);
				return new Feature.SameNER( onPrix,candidate );
			
			} else if(clazz.equals(Feature.HeadWordMentionDistance.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"HeadWordMentionDistance", (new Feature.HeadWordMentionDistance( onPrix,candidate )).value);
				return new Feature.HeadWordMentionDistance( onPrix,candidate );
			
			} else if(clazz.equals(Feature.NamedEntityTypeCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NamedEntityTypeCandidate", (new Feature.NamedEntityTypeCandidate( candidate )).str);
				return new Feature.NamedEntityTypeCandidate( candidate );
			
			}else if(clazz.equals(Feature.POSTypeCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"POSTypeCandidate", (new Feature.POSTypeCandidate( candidate )).str);
				return new Feature.POSTypeCandidate( candidate );
			
			}else if(clazz.equals(Feature.SpeakerTypeCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SpeakerTypeCandidate", (new Feature.SpeakerTypeCandidate( candidate )).str);
				return new Feature.SpeakerTypeCandidate( candidate );
			
			}else if(clazz.equals(Feature.NamedEntityTypeFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NamedEntityTypeFixed", (new Feature.NamedEntityTypeFixed( onPrix )).str);
				return new Feature.NamedEntityTypeFixed( onPrix );
			
			}else if(clazz.equals(Feature.POSTypeFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"POSTypeFixed", (new Feature.POSTypeFixed( onPrix )).str);
				return new Feature.POSTypeFixed( onPrix );
			
			}else if(clazz.equals(Feature.SpeakerTypeFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"SpeakerTypeFixed", (new Feature.SpeakerTypeFixed( onPrix )).str);
				return new Feature.SpeakerTypeFixed( onPrix );
			
			}else if(clazz.equals(Feature.IsProperNounCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsProperNounCandidate", (new Feature.IsProperNounCandidate( candidate )).value?1:0);
				return new Feature.IsProperNounCandidate( candidate );
			
			}else if(clazz.equals(Feature.IsProperNounFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsProperNounFixed", (new Feature.IsProperNounFixed( onPrix )).value?1:0);
				return new Feature.IsProperNounFixed( onPrix );
			
			}else if(clazz.equals(Feature.IsMentionBetweenFixedAndCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsMentionBetweenFixedAndCandidate", (new Feature.IsMentionBetweenFixedAndCandidate( onPrix,candidate )).value?1:0);
				return new Feature.IsMentionBetweenFixedAndCandidate( onPrix,candidate );
			
			}
			else if(clazz.equals(Feature.FixedIsNoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"FixedIsNoun", (new Feature.FixedIsNoun( onPrix.headToken().isNoun() )).value?1:0);
				return new Feature.FixedIsNoun(onPrix.headToken().isNoun() );
			
			}
			else if(clazz.equals(Feature.CandidateIsNoun.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"CandidateIsNoun", (new Feature.CandidateIsNoun( candidate.headToken().isNoun() )).value?1:0);
				return new Feature.CandidateIsNoun(candidate.headToken().isNoun() );
			
			}else if(clazz.equals(Feature.CandidateWordLength.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"CandidateWordLength", (new Feature.CandidateWordLength( candidate.length() )).value);
				return new Feature.CandidateWordLength(candidate.length() );
			
			}else if(clazz.equals(Feature.FixedWordLength.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"FixedWordLength", (new Feature.FixedWordLength( onPrix.length() )).value);
				return new Feature.FixedWordLength(onPrix.length() );
			
			}else if(clazz.equals(Feature.IsHobbs.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsHobbs", (new Feature.IsHobbs(onPrix.equals(RuleBased.hobbsCoreferent(candidate)))).value?1:0);
				return new Feature.IsHobbs(candidate.equals(RuleBased.hobbsCoreferent(onPrix)));
			
			}  
			else if(clazz.equals(Feature.IsFirstPersonSpeakerFixed.class)) {
				boolean m = false;
				if((Pronoun.valueOrNull(onPrix.gloss()) !=null)){
					Speaker s1= Pronoun.valueOrNull(onPrix.gloss()).speaker;
					m=s1.equals("FIRST_PERSON");
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsMentionBetweenFixedAndCandidate", (new Feature.IsFirstPersonSpeakerFixed( m )).value?1:0);
				return new Feature.IsFirstPersonSpeakerFixed( m );
			
			}else if(clazz.equals(Feature.IsFirstPersonSpeakerCandidate.class)) {
				boolean m = false;
				if((Pronoun.valueOrNull(candidate.gloss()) !=null)){
					Speaker s1= Pronoun.valueOrNull(candidate.gloss()).speaker;
					m=s1.equals("FIRST_PERSON");
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsMentionBetweenFixedAndCandidate", (new Feature.IsFirstPersonSpeakerFixed( m )).value?1:0);
				return new Feature.IsFirstPersonSpeakerFixed( m );
			
			}else if(clazz.equals(Feature.IsSameNPersonSpeaker.class)) {
				boolean m = false;
				if((Pronoun.valueOrNull(candidate.gloss()) !=null) && (Pronoun.valueOrNull(onPrix.gloss()) !=null)){
					Speaker s1= Pronoun.valueOrNull(onPrix.gloss()).speaker;
					Speaker s2= Pronoun.valueOrNull(candidate.gloss()).speaker;
					m=s1.equals(s2);
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsSameNPersonSpeaker", m?1:0);
				return new Feature.IsSameNPersonSpeaker( m );
				
			}else if(clazz.equals(Feature.IsNthPersonSpeakerFixed.class)) {
				int m=0;
				if(Pronoun.valueOrNull(onPrix.gloss()) !=null){
					Speaker s1= Pronoun.valueOrNull(onPrix.gloss()).speaker;
					int mycounter=0;
					for(Speaker s:Speaker.values()){
						if (s1.equals(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NthPersonSpeakerFixed", m);
				return new Feature.IsNthPersonSpeakerFixed( m );
			}else if(clazz.equals(Feature.IsNthPersonSpeakerCandidate.class)) {
				int m=0;
				if(Pronoun.valueOrNull(candidate.gloss()) !=null){
					Speaker s1= Pronoun.valueOrNull(candidate.gloss()).speaker;
					int mycounter=0;
					for(Speaker s:Speaker.values()){
						if (s1.equals(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsNthPersonSpeakerCandidate", m);
				return new Feature.IsNthPersonSpeakerCandidate( m );
			}else if(clazz.equals(Feature.NthTypeFixed.class)) {
				int m=0;
				if(Pronoun.valueOrNull(onPrix.gloss()) !=null){
					Type s1= Pronoun.valueOrNull(onPrix.gloss()).type;
					int mycounter=0;
					for(Type s:Type.values()){
						if (s1.equals(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NthTypeFixed", m);
				return new Feature.NthTypeFixed( m );
			}else if(clazz.equals(Feature.NthTypeCandidate.class)) {
				int m=0;
				if(Pronoun.valueOrNull(candidate.gloss()) !=null){
					Type s1= Pronoun.valueOrNull(candidate.gloss()).type;
					int mycounter=0;
					for(Type s:Type.values()){
						if (s1.equals(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"NthTypeFixed", m);
				return new Feature.NthTypeFixed( m );
				
			} else if(clazz.equals(Feature.PronounTypeFixed.class)) {
				int m=0;
				if(Pronoun.valueOrNull(onPrix.gloss()) !=null){
					int mycounter=0;
					for(String s:Pronoun.allPronouns()){
						if (onPrix.gloss().equalsIgnoreCase(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"PronounTypeFixed", m);
				return new Feature.PronounTypeFixed( m );
				
			} else if(clazz.equals(Feature.PronounTypeCandidate.class)) {
				int m=0;
				if(Pronoun.valueOrNull(candidate.gloss()) !=null){
					int mycounter=0;
					for(String s:Pronoun.allPronouns()){
						if (candidate.gloss().equalsIgnoreCase(s)){
						 m = mycounter;
						}
						mycounter++;
					}
				}
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"PronounTypeCandidate", m);
				return new Feature.PronounTypeCandidate( m );
				
			}else if(clazz.equals(Feature.IsLeafCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsLeafCandidate", (new Feature.IsLeafCandidate( candidate )).value?1:0);
				return new Feature.IsLeafCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsLeafFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsLeafFixed", (new Feature.IsLeafFixed( onPrix )).value?1:0);
				return new Feature.IsLeafFixed( onPrix );
			
			} else if(clazz.equals(Feature.IsPhrasalCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPhrasalCandidate", (new Feature.IsPhrasalCandidate( candidate )).value?1:0);
				return new Feature.IsPhrasalCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsPhrasalFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPhrasalFixed", (new Feature.IsPhrasalFixed( onPrix )).value?1:0);
				return new Feature.IsPhrasalFixed( onPrix );
			
			} else if(clazz.equals(Feature.IsPretermCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPretermCandidate", (new Feature.IsPretermCandidate( candidate )).value?1:0);
				return new Feature.IsPretermCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsPretermFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPretermFixed", (new Feature.IsPretermFixed( onPrix )).value?1:0);
				return new Feature.IsPretermFixed( onPrix );
			
			} else if(clazz.equals(Feature.ParseLabelCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"ParseLabelCandidate", (new Feature.ParseLabelCandidate( candidate )).str);
				return new Feature.ParseLabelCandidate( candidate );
			
			} else if(clazz.equals(Feature.ParseLabelFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"ParseLabelFixed", (new Feature.ParseLabelFixed( onPrix )).str);
				return new Feature.ParseLabelFixed( onPrix );
			
			}else if(clazz.equals(Feature.IsQuotedCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsQuotedCandidate", (new Feature.IsQuotedCandidate( candidate )).value?1:0);
				return new Feature.IsQuotedCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsQuotedFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsQuotedFixed", (new Feature.IsQuotedFixed( onPrix )).value?1:0);
				return new Feature.IsQuotedFixed( onPrix );
			
			}else if(clazz.equals(Feature.FixedMentionSize.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"FixedMentionSize", (new Feature.FixedMentionSize( onPrix )).value);
				return new Feature.FixedMentionSize( onPrix );
			
			} else if(clazz.equals(Feature.CandidateMentionSize.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"CandidateMentionSize", (new Feature.CandidateMentionSize( candidate )).value);
				return new Feature.CandidateMentionSize( candidate );
			
			}else if(clazz.equals(Feature.IsCapitalizedCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsCapitalizedCandidate", (new Feature.IsCapitalizedCandidate( candidate )).value?1:0);
				return new Feature.IsCapitalizedCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsPossessiveCandidate.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPossessiveCandidate", (new Feature.IsPossessiveCandidate( candidate )).value?1:0);
				return new Feature.IsPossessiveCandidate( candidate );
			
			} else if(clazz.equals(Feature.IsCapitalizedFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsCapitalizedFixed", (new Feature.IsCapitalizedFixed( onPrix )).value?1:0);
				return new Feature.IsCapitalizedFixed( onPrix );
			
			} else if(clazz.equals(Feature.IsPossessiveFixed.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"IsPossessiveFixed", (new Feature.IsPossessiveFixed( onPrix )).value?1:0);
				return new Feature.IsPossessiveFixed( onPrix );
			
			}   else if(clazz.equals(Feature.BothAreCapitalizedAndNotPronouns.class)) {
				printTheResultsForThisFeature( onPrix.gloss(), candidate.gloss(),"BothAreCapitalizedAndNotPronouns", (new Feature.BothAreCapitalizedAndNotPronouns( onPrix,candidate )).value?1:0);
				return new Feature.BothAreCapitalizedAndNotPronouns( onPrix,candidate );
			
			} else if(clazz.equals(Feature.JaccardBuckets.class)) {
				return new Feature.JaccardBuckets( onPrix,candidate );
			
			}     

			
			
			else {
				throw new IllegalArgumentException("Unregistered feature: " + clazz);
			}
		}
		private void printTheResultsForThisFeature(String mi, String mj, String className, Integer value){
			if(false)
				System.out.printf(" mi: \"%s\" mj:\"%s\"  %s result: \"%d\" \n",    mi,   mj, className, value);
			return;
		}
		private void printTheResultsForThisFeature(String mi, String mj, String className, String value){
			if(false)
				System.out.printf(" mi: \"%s\" mj:\"%s\"  %s result: \"%s\" \n",    mi,   mj, className, value);
			return;
		}
		@SuppressWarnings({"unchecked"})
		@Override
		protected void fillFeatures(Pair<Mention, ClusteredMention> input, Counter<Feature> inFeatures, Boolean output, Counter<Feature> outFeatures) {
			//--Input Features
			for(Object o : ACTIVE_FEATURES){
				if(o instanceof Class){
					//(case: singleton feature)
					Option<Double> count = new Option<Double>(1.0);
					Feature feat = feature((Class) o, input, count);
					if(count.get() > 0.0){
						inFeatures.incrementCount(feat, count.get());
					}
				} else if(o instanceof Pair){
					//(case: pair of features)
					Pair<Class,Class> pair = (Pair<Class,Class>) o;
					Option<Double> countA = new Option<Double>(1.0);
					Option<Double> countB = new Option<Double>(1.0);
					Feature featA = feature(pair.getFirst(), input, countA);
					Feature featB = feature(pair.getSecond(), input, countB);
					if(countA.get() * countB.get() > 0.0){
						inFeatures.incrementCount(new Feature.PairFeature(featA, featB), countA.get() * countB.get());
					}
				}
			}

			//--Output Features
			if(output != null){
				outFeatures.incrementCount(new Feature.CoreferentIndicator(output), 1.0);
			}
		}

		@Override
		protected Feature concat(Feature a, Feature b) {
			return new Feature.PairFeature(a,b);
		}
	};

	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		startTrack("Training");
		//--Variables
		RVFDataset<Boolean, Feature> dataset = new RVFDataset<Boolean, Feature>();
		LinearClassifierFactory<Boolean, Feature> fact = new LinearClassifierFactory<Boolean,Feature>();
		//--Feature Extraction
		startTrack("Feature Extraction");
		for(Pair<Document,List<Entity>> datum : trainingData){
			//(document variables)
			Document doc = datum.getFirst();
			List<Entity> goldClusters = datum.getSecond();
			List<Mention> mentions = doc.getMentions();
			Map<Mention,Entity> goldEntities = Entity.mentionToEntityMap(goldClusters);
			startTrack("Document " + doc.id);
			//(for each mention...)
			for(int i=0; i<mentions.size(); i++){
				//(get the mention and its cluster)
				Mention onPrix = mentions.get(i);
				Entity source = goldEntities.get(onPrix);
				if(source == null){ throw new IllegalArgumentException("Mention has no gold entity: " + onPrix); }
				//(for each previous mention...)
				int oldSize = dataset.size();
				for(int j=i-1; j>=0; j--){
					//(get previous mention and its cluster)
					Mention cand = mentions.get(j);
					Entity target = goldEntities.get(cand);
					if(target == null){ throw new IllegalArgumentException("Mention has no gold entity: " + cand); }
					//(extract features)
					Counter<Feature> feats = extractor.extractFeatures(Pair.make(onPrix, cand.markCoreferent(target)));
					//(add datum)
					dataset.add(new RVFDatum<Boolean, Feature>(feats, target == source));
					//(stop if
					if(target == source){ break; }
				}
				//logf("Mention %s (%d datums)", onPrix.toString(), dataset.size() - oldSize);
			}
			endTrack("Document " + doc.id);
		}
		endTrack("Feature Extraction");
		//--Train Classifier
		startTrack("Minimizer");
		this.classifier = fact.trainClassifier(dataset);
		endTrack("Minimizer");
		//--Dump Weights
		startTrack("Features");
		//(get labels to print)
		Set<Boolean> labels = new HashSet<Boolean>();
		labels.add(true);
		//(print features)
		for(Triple<Feature,Boolean,Double> featureInfo : this.classifier.getTopFeatures(labels, 0.0, true, 300, true)){
			Feature feature = featureInfo.first();
			Boolean label = featureInfo.second();
			Double magnitude = featureInfo.third();
			log(FORCE,new DecimalFormat("0.000").format(magnitude) + " [" + label + "] " + feature);
		}
		end_Track("Features");
		endTrack("Training");
	}

	public List<ClusteredMention> runCoreference(Document doc) {
		//--Overhead
		startTrack("Testing " + doc.id);
		//(variables)
		List<ClusteredMention> rtn = new ArrayList<ClusteredMention>(doc.getMentions().size());
		List<Mention> mentions = doc.getMentions();
		int singletons = 0;
		//--Run Classifier
		for(int i=0; i<mentions.size(); i++){
			//(variables)
			Mention onPrix = mentions.get(i);
			int coreferentWith = -1;
			//(get mention it is coreferent with)
			for(int j=i-1; j>=0; j--){
				ClusteredMention cand = rtn.get(j);
				boolean coreferent = classifier.classOf(new RVFDatum<Boolean, Feature>(extractor.extractFeatures(Pair.make(onPrix, cand))));
				if(coreferent){
					coreferentWith = j;
					break;
				}
			}
			//(mark coreference)
			if(coreferentWith < 0){
				singletons += 1;
				rtn.add(onPrix.markSingleton());
			} else {
				//log("Mention " + onPrix + " coreferent with " + mentions.get(coreferentWith));
				rtn.add(onPrix.markCoreferent(rtn.get(coreferentWith)));
			}
		}
		//log("" + singletons + " singletons");
		//--Return
		endTrack("Testing " + doc.id);
		return rtn;
	}

	private class Option<T> {
		private T obj;
		public Option(T obj){ this.obj = obj; }
		public Option(){};
		public T get(){ return obj; }
		public void set(T obj){ this.obj = obj; }
		public boolean exists(){ return obj != null; }
	}
}
