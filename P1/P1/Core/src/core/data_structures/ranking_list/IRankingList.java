package core.data_structures.ranking_list;

import network.models.ParticipantEntry;

import java.util.List;

public interface IRankingList {
    void processParticipantEntry(ParticipantEntry entry);
    List<ParticipantEntry> getEntriesAsList();
}
