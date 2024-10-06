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

        MessageStats messageStats;
        VoiceStats voiceStats;


        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @Builder
        public static class MessageStats {

            int chars;
            int words;
            int messageCount;
            long xp;

        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @Builder
        public static class VoiceStats {

            // In ms
            long totalTime;

            int voiceJoins;

            long activeVoiceChannelId;
            long voiceChannelJoinTimestamp;

            public long getLifeTotalTime() {
                return getTotalTime() + (getVoiceChannelJoinTimestamp() == -1 ? 0 : (System.currentTimeMillis() - getVoiceChannelJoinTimestamp()));
            }

        }
    }


}
