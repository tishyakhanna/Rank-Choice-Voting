/**
* @author Tishya Khanna
*/
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* An Election consists of the candidates running for office, the ballots that
* have been cast, and the total number of voters.  This class implements the
* ranked choice voting algorithm.
*
* Ranked choice voting uses this process:
* <ol>
* <li>Rather than vote for a single candidate, a voter ranks all the
* candidates.  For example, if 3 candidates are running on the ballot, a voter
* identifies their first choice, second choice, and third choice.
* <li>The first-choice votes are tallied.  If any candidate receives &gt; 50%
* of the votes, that candidate wins.
* <li>If no candidate wins &gt; 50% of the votes, the candidate(s) with the
* lowest number of votes is(are) eliminated.  For each ballot in which an
* eliminated candidate is the first choice, the 2nd ranked candidate is now
* the top choice for that ballot.
* <li>Steps 2 &amp; 3 are repeated until a candidate wins, or all remaining
* candidates have exactly the same number of votes.  In the case of a tie,
* there would be a separate election involving just the tied candidates.
* </ol>
*/
public class Election {
  // All candidates that were in the election initially.  If a candidate is
  // eliminated, they will still stay in this array.
  private final Candidate[] candidates;

  // The next slot in the candidates array to fill.
  private int nextCandidate;

  // The total number of ballots.
  private int totalBallots;

  /**
  * Create a new Election object.  Initially, there are no candidates or
  * votes.
  * @param numCandidates the number of candidates in the election.
  */
  public Election (int numCandidates) {
    this.candidates = new Candidate[numCandidates];
    this.totalBallots = 0;
  }

  /**
  * Adds a candidate to the election
  * @param name the candidate's name
  */
  public void addCandidate (String name) {
    candidates[nextCandidate] = new Candidate (name);
    nextCandidate++;
  }

  /**
  * Adds a completed ballot to the election.
  * @param ranks A correctly formulated ballot will have exactly 1
  * entry with a rank of 1, exactly one entry with a rank of 2, etc.  If
  * there are n candidates on the ballot, the values in the rank array
  * passed to the constructor will be some permutation of the numbers 1 to
  * n.
  * @throws IllegalArgumentException if the ballot is not valid.
  */
  public void addBallot (int[] ranks) {
    if (!isBallotValid(ranks)) {
      throw new IllegalArgumentException("Invalid ballot");
    }
    Ballot newBallot = new Ballot(ranks);
    assignBallotToCandidate(newBallot);
    totalBallots++;
  }

  /**
  * Checks that the ballot is the right length and contains a permutation
  * of the numbers 1 to n, where n is the number of candidates.
  * @param ranks the ballot to check
  * @return true if the ballot is valid.
  */
  private boolean isBallotValid(int[] ranks) {
    if (ranks.length != candidates.length) {
      return false;
    }
    int[] sortedRanks = Arrays.copyOf(ranks, ranks.length);
    Arrays.sort(sortedRanks);
    for (int i = 0; i < sortedRanks.length; i++) {
      if (sortedRanks[i] != i + 1) {
        return false;
      }
    }
    return true;
  }

  /**
  * Determines which candidate is the top choice on the ballot and gives the
  * ballot to that candidate.
  * @param newBallot a ballot that is not currently assigned to a candidate
  */
  private void assignBallotToCandidate(Ballot newBallot) {
    int candidate = newBallot.getTopCandidate();
    candidates[candidate].addBallot(newBallot);
  }

  /**
  * Checks for a tie
  * @return true if it's a tie, false if not
  */
  private boolean tie(){
    int count = 0;
    for(int k=0;k<candidates.length;k++){
      if(candidates[k].isEliminated()==false){
        count++;
      }
    }
    for(int k=0;k<candidates.length;k++){
      if(candidates[k].isEliminated()==false && totalBallots%count == 0){
        return true;
      }
    }
    return false;
  }


  /**
  * Apply the ranked choice voting algorithm to identify the winner.
  * @return If there is a winner, this method returns a list containing just
  * the winner's name is returned.  If there is a tie, this method returns a
  * list containing the names of the tied candidates.
  */
  public List<String> selectWinner () {
    List<String> winners = new ArrayList<>();
    int totalCadidates = candidates.length;
    int fiftyPercentVotes = totalBallots/2;
    int minVotes=totalBallots;
    int remainingCandidates=totalCadidates;

    //the loop runs until winners are found
    while(winners.isEmpty()){

      //determining minumum votes for the round
      minVotes=totalBallots;
      for(int i=0;i<candidates.length;i++){
        if (candidates[i].getVotes()<=minVotes && candidates[i].isEliminated()==false){
          minVotes=candidates[i].getVotes();
        }
      }

      //determing number of candidates to eliminate
      int count=0;
      for(int i=0;i<candidates.length;i++){
        if (candidates[i].getVotes()==minVotes){
          count++;
        }
      }

      //determing who to eliminate and adding the id to an array
      int toEl[]=new int[count];
      int j=0;
      for(int i=0;i<candidates.length;i++){
        if (candidates[i].getVotes()==minVotes){
          toEl[j]=i;
          j++;
        }
      }

      //eliminating the candidates
      int a;
      List<Ballot> toEliminate;
      for(int i=0;i<toEl.length;i++){
        toEliminate=candidates[toEl[i]].eliminate();
        for(Ballot b: toEliminate){
          b.eliminateCandidate(toEl[i]);
          a=b.getTopCandidate();
          if(!candidates[a].isEliminated()){
            candidates[a].addBallot(b);
          }
        }
      }

      //calculating the remaining candidates
      remainingCandidates-=toEl.length;

      //checking for winners
      for(int i=0;i<candidates.length;i++){
        //if anyone has 50%+ votes
        if (candidates[i].getVotes()>fiftyPercentVotes){
          winners.add(candidates[i].getName());
        }
        //if there is only 1 candidate left who has less than 50% votes
        else if (remainingCandidates==1 && candidates[i].isEliminated()==false){
          winners.add(candidates[i].getName());
        }
      }
      //if the remaining candidates have the same votes and it's a tie
      if(remainingCandidates>1 && tie()==true){
        for(int b=0;b<candidates.length;b++){
          if(candidates[b].isEliminated()==false){
            winners.add(candidates[b].getName());
          }
        }
      }

    }
    return winners;
  }
}
