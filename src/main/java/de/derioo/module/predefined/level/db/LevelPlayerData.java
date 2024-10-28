package de.derioo.module.predefined.level.db;

import eu.koboo.en2do.repository.entity.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;

import java.util.Formatter;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@ToString
public class LevelPlayerData {


    @Id
    ObjectId id;

    String guildId;
    String userId;

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
    @ToString
    public static class Stats {

        int xp;

        VoiceStats voiceStats;




        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @FieldDefaults(level = AccessLevel.PRIVATE)
        @Builder
        @ToString
        public static class VoiceStats {

            // In ms
            long totalTime;

            long voiceChannelJoinTimestamp;

            public long getLifeTotalTime() {
                return getTotalTime() + (getVoiceChannelJoinTimestamp() == -1 ? 0 : (System.currentTimeMillis() - getVoiceChannelJoinTimestamp()));
            }

            public String getLifeTotalTimeFormatted() {
                long joinDiff = getLifeTotalTime();


                long seconds = (joinDiff / 1000) % 60;
                long minutes = (joinDiff / (1000 * 60)) % 60;
                long hours = (joinDiff / (1000 * 60 * 60)) % 24;
                long days = joinDiff / (1000 * 60 * 60 * 24);

                return new Formatter().format("`%s` Tage, `%s` Stunden, `%s` Minuten, `%s` Sekunden", days, hours, minutes, seconds).toString();
            }


        }
    }


}
