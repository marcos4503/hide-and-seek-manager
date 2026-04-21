package xyz.windsoft.hidenseek.utils;

/*
 * This class helps handling Player Scores.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class PlayerScore {

    //Private variables
    private String nickname = "";
    private int score = 0;

    //Public methods

    public PlayerScore(String nickname, int score){
        //Store the nickname
        this.nickname = nickname;
        this.score = score;
    }

    public void IncrementScore(int increment){
        //Increment the score
        score += increment;
    }

    public String GetNickname(){
        //Return the Nickname
        return nickname;
    }

    public int GetScore(){
        //Return the Score
        return score;
    }
}