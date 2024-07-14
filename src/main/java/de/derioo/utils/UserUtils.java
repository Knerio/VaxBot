package de.derioo.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserUtils {

    public static @NotNull String getMention(User user) {
        if (user == null) {
            return "Nicht bekannt";
        }
        return user.getAsMention() + "(@" + user.getEffectiveName() + ")";
    }

    public static @NotNull String getMention(Member member) {
        return getMention(member == null ? null : member.getUser());
    }

}
