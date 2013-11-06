package cs224n.coref;

import cs224n.util.Pair;

import java.util.Set;

/**
 * @author Gabor Angeli (angeli at cs.stanford)
 */
public interface Feature {

  //-----------------------------------------------------------
  // TEMPLATE FEATURE TEMPLATES
  //-----------------------------------------------------------
  public static class PairFeature implements Feature {
    public final Pair<Feature,Feature> content;
    public PairFeature(Feature a, Feature b){ this.content = Pair.make(a, b); }
    public String toString(){ return content.toString(); }
    public boolean equals(Object o){ return o instanceof PairFeature && ((PairFeature) o).content.equals(content); }
    public int hashCode(){ return content.hashCode(); }
  }

  public static abstract class Indicator implements Feature {
    public final boolean value;
    public Indicator(boolean value){ this.value = value; }
    public boolean equals(Object o){ return o instanceof Indicator && o.getClass().equals(this.getClass()) && ((Indicator) o).value == value; }
    public int hashCode(){ 
    	return this.getClass().hashCode() ^ Boolean.valueOf(value).hashCode(); }
    public String toString(){ 
    	return this.getClass().getSimpleName() + "(" + value + ")"; }
  }

  public static abstract class IntIndicator implements Feature {
    public final int value;
    public IntIndicator(int value){ this.value = value; }
    public boolean equals(Object o){ return o instanceof IntIndicator && o.getClass().equals(this.getClass()) && ((IntIndicator) o).value == value; }
    public int hashCode(){ 
    	return this.getClass().hashCode() ^ value; 
    }
    public String toString(){ return this.getClass().getSimpleName() + "(" + value + ")"; }
  }

  public static abstract class BucketIndicator implements Feature {
    public final int bucket;
    public final int numBuckets;
    public BucketIndicator(int value, int max, int numBuckets){
      this.numBuckets = numBuckets;
      bucket = value * numBuckets / max;
      if(bucket < 0 || bucket >= numBuckets){ throw new IllegalStateException("Bucket out of range: " + value + " max="+max+" numbuckets="+numBuckets); }
    }
    public boolean equals(Object o){ return o instanceof BucketIndicator && o.getClass().equals(this.getClass()) && ((BucketIndicator) o).bucket == bucket; }
    public int hashCode(){ return this.getClass().hashCode() ^ bucket; }
    public String toString(){ return this.getClass().getSimpleName() + "(" + bucket + "/" + numBuckets + ")"; }
  }

  public static abstract class Placeholder implements Feature {
    public Placeholder(){ }
    public boolean equals(Object o){ return o instanceof Placeholder && o.getClass().equals(this.getClass()); }
    public int hashCode(){ return this.getClass().hashCode(); }
    public String toString(){ return this.getClass().getSimpleName(); }
  }

  public static abstract class StringIndicator implements Feature {
    public final String str;
    public StringIndicator(String str){ this.str = str; }
    public boolean equals(Object o){ return o instanceof StringIndicator && o.getClass().equals(this.getClass()) && ((StringIndicator) o).str.equals(this.str); }
    public int hashCode(){ return this.getClass().hashCode() ^ str.hashCode(); }
    public String toString(){ return this.getClass().getSimpleName() + "(" + str + ")"; }
  }

  public static abstract class SetIndicator implements Feature {
    public final Set<String> set;
    public SetIndicator(Set<String> set){ this.set = set; }
    public boolean equals(Object o){ return o instanceof SetIndicator && o.getClass().equals(this.getClass()) && ((SetIndicator) o).set.equals(this.set); }
    public int hashCode(){ return this.getClass().hashCode() ^ set.hashCode(); }
    public String toString(){
      StringBuilder b = new StringBuilder();
      b.append(this.getClass().getSimpleName());
      b.append("( ");
      for(String s : set){
        b.append(s).append(" ");
      }
      b.append(")");
      return b.toString();
    }
  }
  
  /*
   * TODO: If necessary, add new feature types
   */

  //-----------------------------------------------------------
  // REAL FEATURE TEMPLATES
  //-----------------------------------------------------------

  public static class CoreferentIndicator extends Indicator {
    public CoreferentIndicator(boolean coreferent){ super(coreferent); }
  }

  public static class ExactMatch extends Indicator {
    public ExactMatch(boolean exactMatch){ 
    	super(exactMatch);
    	
    	}
  }
  
  public static class FixedIsPronoun extends Indicator {
	    public FixedIsPronoun(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
    }
  }
  public static class CandidateIsPronoun extends Indicator {
	    public CandidateIsPronoun(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
	    }
  }
  public static class CandidateIsButFixedIsNotPronoun extends Indicator {
	    public CandidateIsButFixedIsNotPronoun(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
	    }
  }
  public static class FixedIsButCandidateIsNotPronoun extends Indicator {
	    public FixedIsButCandidateIsNotPronoun(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
	    }
  }
  public static class BothArePronouns extends Indicator {
	    public BothArePronouns(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
	    }
  }
  public static class NeitherArePronouns extends Indicator {
	    public NeitherArePronouns(boolean isPronoun){ 
	    	super(isPronoun); 
	    	
	    }
  }
  public static class IsSameSex extends Indicator {
	    public IsSameSex(Mention mi, Mention mj){ 
	    	super(Util.haveGenderAndAreSameGender(mi,mj).getSecond()); 
	    }
  }
  public static class SameNumber extends Indicator {
	    public SameNumber(Mention mi, Mention mj){ 
	    	super(Util.haveNumberAndAreSameNumber(mi,mj).getSecond()); 
	    }
  }
  public static class SameHeadWord extends Indicator {
	    public SameHeadWord(Mention mi, Mention mj){ 
	    	super(mi.headWord().equals(mj.headWord())); 
	    }
}
  public static class BothArePlural extends Indicator {
	    public BothArePlural(boolean isTrue){ 
	    	super(isTrue); 
	    }
}
  public static class NeitherArePlural extends Indicator {
	    public NeitherArePlural(boolean isTrue){ 
	    	super(isTrue); 
	    }
}
  public static class SameSpeaker extends Indicator {
	    public SameSpeaker(Mention mi, Mention mj){ 
	    	super( mi.headToken().speaker().equals(mj.headToken().speaker())); 
	    	//System.out.printf("speaker  %s %s\n",mi.headToken().speaker(), mj.headToken().speaker() );
	    }
}
  
 
  public static class SamePOS extends Indicator {
	    public SamePOS(Mention mi, Mention mj){ 
	    	super( mi.headToken().posTag().equals(mj.headToken().posTag() )); 
	    	//System.out.printf("posTag  %s %s\n",mi.headToken().posTag(), mj.headToken().posTag() );
	    }
}
  public static class SameNER extends Indicator {
	    public SameNER(Mention mi, Mention mj){ 
	    	super( mi.headToken().nerTag().equals(mj.headToken().nerTag() )); 
	    	//System.out.printf("nerTag  %s %s\n",mi.headToken().nerTag(), mj.headToken().nerTag() );
	    }
}
  
  public static class IsProperNounCandidate extends Indicator {
	    public IsProperNounCandidate(Mention m){ 
	    	super( m.headToken().isProperNoun() ); 
	    	//System.out.printf("nerTag  %s %s\n",mi.headToken().nerTag(), mj.headToken().nerTag() );
	    }
}
  public static class IsProperNounFixed extends Indicator {
	    public IsProperNounFixed(Mention m){ 
	    	super( m.headToken().isProperNoun() ); 
	    	
	    	//System.out.printf("nerTag  %s %s\n",mi.headToken().nerTag(), mj.headToken().nerTag() );
	    }
}
  
  
  
  
  public static class MentionDistanceViaMention extends IntIndicator {
	    public MentionDistanceViaMention(Mention mi, Mention mj){ 
	    	super(Math.abs(mi.doc.indexOfMention(mi) - mi.doc.indexOfMention(mj))); 
	    }
}  
  
  public static class MentionDistanceViaSentence extends IntIndicator {
    public MentionDistanceViaSentence(Mention mi, Mention mj){ 
    	super(Math.abs(mi.doc.indexOfSentence(mi.sentence) - mi.doc.indexOfSentence(mj.sentence))); 
    }
}  
  
  public static class MentionDistance extends IntIndicator {
	    public MentionDistance(Mention mi, Mention mj){ 
	    	super(Math.min(Math.abs(mi.endIndexExclusive-mj.beginIndexInclusive),Math.abs(mj.endIndexExclusive-mi.beginIndexInclusive))); 
	    }
}  
  public static class HeadWordMentionDistance extends IntIndicator {
	    public HeadWordMentionDistance(Mention mi, Mention mj){ 
	    	super(Math.abs(mi.headWordIndex - mj.headWordIndex)); 
	    }
}   
  
  public static class NamedEntityTypeCandidate extends StringIndicator {
	    public NamedEntityTypeCandidate(Mention m){ 
	    	super(m.headToken().nerTag()); 
	    }
}  
  public static class POSTypeCandidate extends StringIndicator {
	    public POSTypeCandidate(Mention m){ 
	    	super(m.headToken().posTag()); 
	    	
	    }
}    
  public static class SpeakerTypeCandidate extends StringIndicator {
	    public SpeakerTypeCandidate(Mention m){ 
	    	super(m.headToken().speaker()); 
	    }
}  
  
  public static class NamedEntityTypeFixed extends StringIndicator {
	    public NamedEntityTypeFixed(Mention m){ 
	    	super(m.headToken().nerTag()); 
	    }
}  
public static class POSTypeFixed extends StringIndicator {
	    public POSTypeFixed(Mention m){ 
	    	super(m.headToken().posTag()); 
	    }
}    
public static class SpeakerTypeFixed extends StringIndicator {
	    public SpeakerTypeFixed(Mention m){ 
	    	super(m.headToken().speaker()); 
	    }
}  

public static class IsSameNPersonSpeaker extends Indicator {
    public IsSameNPersonSpeaker(boolean m){ 
    	super(m); 
    }
}  
public static class IsNthPersonSpeakerCandidate extends IntIndicator {
    public IsNthPersonSpeakerCandidate(int m){ 
    	super(m); 
    }
}    

public static class IsNthPersonSpeakerFixed extends IntIndicator {
    public IsNthPersonSpeakerFixed(int m){ 
    	super(m); 
    }
}  
public static class NthTypeFixed extends IntIndicator {
    public NthTypeFixed(int m){ 
    	super(m); 
    }
} 
public static class NthTypeCandidate extends IntIndicator {
    public NthTypeCandidate(int m){ 
    	super(m); 
    }
} 


public static class IsLeafFixed extends Indicator {
    public IsLeafFixed(Mention m){ 
    	super(m.parse.isLeaf()); 
    }
}  
public static class IsLeafCandidate extends Indicator {
    public IsLeafCandidate(Mention m){ 
    	super(m.parse.isLeaf()); 
    }
}  
public static class IsPhrasalFixed extends Indicator {
    public IsPhrasalFixed(Mention m){ 
    	super(m.parse.isPhrasal() ); 
    }
}  
public static class IsPhrasalCandidate extends Indicator {
    public IsPhrasalCandidate(Mention m){ 
    	super(m.parse.isPhrasal()); 
    }
}  
public static class IsPretermFixed extends Indicator {
    public IsPretermFixed(Mention m){ 
    	super(m.parse.isPreTerminal() ); 
    }
}  
public static class IsPretermCandidate extends Indicator {
    public IsPretermCandidate(Mention m){ 
    	super(m.parse.isPreTerminal()); 
    }
}  
public static class ParseLabelFixed extends StringIndicator {
    public ParseLabelFixed(Mention m){ 
    	super(m.parse.getLabel() ); 
    }
}  
public static class ParseLabelCandidate extends StringIndicator {
    public ParseLabelCandidate(Mention m){ 
    	super(m.parse.getLabel());
    }
}  

public static class IsQuotedFixed extends Indicator {
    public IsQuotedFixed(Mention m){ 
    	super(m.headToken().isQuoted()); 
    }
}  

public static class IsQuotedCandidate extends Indicator {
    public IsQuotedCandidate(Mention m){ 
    	super(m.headToken().isQuoted()); 
    }
}  

public static class CandidateMentionSize extends IntIndicator {
    public CandidateMentionSize(Mention m){ 
    	super(m.text().size()); 
    }
}  
public static class FixedMentionSize extends IntIndicator {
    public FixedMentionSize(Mention m){ 
    	super(m.text().size()); 
    }
}   
  /*
   * TODO: Add values to the indicators here.
   */

}
