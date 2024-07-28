package data_structures.ranking_list;

import domain.ParticipantEntry;

import java.util.List;

public interface IRankingList {
    void processParticipantEntry(ParticipantEntry entry);
    List<ParticipantEntry> getEntriesAsList();
}
