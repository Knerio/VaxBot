package de.derioo.module.predefined.level.db;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class LevelPlayerData {

    // UserID + ":" + guildId
    @Id
    String id;


    Stats stats;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LevelPlayerData other)) return false;
        return Objects.equals(this.id, other.id);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    public static class Stats {

        int xp;

        VoiceStats voiceStats;



        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @Builder
        public static class VoiceStats {

            // In ms
            long totalTime;

            long voiceChannelJoinTimestamp;

            public long getLifeTotalTime() {
                return getTotalTime() + (getVoiceChannelJoinTimestamp() == -1 ? 0 : (System.currentTimeMillis() - getVoiceChannelJoinTimestamp()));
            }

        }
    }


}