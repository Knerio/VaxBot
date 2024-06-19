package de.derioo.utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class UserUtils {

    public static String getMention(@NotNull User user) {
        return user.getAsMention() + "(@" + user.getEffectiveName() + ")";
    }

    public static String getMention(@NotNull Member member) {
        return getMention(member.getUser());
    }

}
