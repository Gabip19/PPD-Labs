package network.models;

import java.io.Serializable;

public class FinalResultsDto implements Serializable {
    private Entry[] countriesResults;
    private ParticipantEntry[] finalResults;

    public FinalResultsDto(Entry[] countriesResults, ParticipantEntry[] finalResults) {
        this.countriesResults = countriesResults;
        this.finalResults = finalResults;
    }

    public Entry[] getCountriesResults() {
        return countriesResults;
    }

    public void setCountriesResults(Entry[] countriesResults) {
        this.countriesResults = countriesResults;
    }

    public ParticipantEntry[] getFinalResults() {
        return finalResults;
    }

    public void setFinalResults(ParticipantEntry[] finalResults) {
        this.finalResults = finalResults;
    }
}
