package network.models;

import java.io.Serializable;

public class ParticipantEntry implements Serializable {
    private int id;
    private int score;
    private int countryNum;

    public ParticipantEntry(int id, int score, int countryNum) {
        this.id = id;
        this.score = score;
        this.countryNum = countryNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCountryNum() {
        return countryNum;
    }

    public void setCountryNum(int countryNum) {
        this.countryNum = countryNum;
    }
}
