package de.derioo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@AllArgsConstructor
@Getter
public enum Emote {


    PARTY_EMOTE("\uD83C\uDF89"),
    PROGRESS_LEFT_0("<:pgbl0:1292454705158557788>"),
    PROGRESS_LEFT_1("<:pgbl1:1292454706664181795>"),
    PROGRESS_MID_0("<:pgbm0:1292454708191035422>"),
    PROGRESS_MID_1("<:pgbm1:1292454709990395985>"),
    PROGRESS_RIGHT_0("<:pgbr0:1292454711257075804>"),
    PROGRESS_RIGHT_1("<:pgbr1:1292454713052233789>"),
    USER("<:varilx_user:1139957321196376107>"),
    TEXT_CHANNEL("<:varilx_textchannel:1139957022696157294>"),
    CALENDAR("<:varilx_clendar:1139956980576960653>"),
    CLOCK("<:varilx_clock:1139957097522528257>"),
    PEN("<:vax_pen:1140659710836621342>"),
    BOT("<:vax_bot:1140659750724435979>"),
    STAR("<:varilx_star:1139957135707484290>"),
    HYPESQUAD_BALANCE("<:hype1:1140656206747287603>"),
    HYPESQUAD_BRILLIANCE("<:hype2:1140656191886872646>"),
    HYPESQUAD_BRAVERY("<:hype3:1140656174140756049>"),
    ACTIVE_DEVELOPER("<:active_dev:1140656159662030888>"),
    VERIFIED_DEVELOPER("<:dev:1140656146584178770>"),
    VARILX("<:Varilx:1252773252905041942>"),
    PARTNER("<:partner:1140656134504599552>"),
    THUMBS_UP("\uD83D\uDC4D"),
    THUMBS_DOWN("\uD83D\uDC4E"),
    UPVOTE("<:upvote:1294809970264772701>"),
    DOWNVOTE("<:downvote:1294809995396911136>"),
    CONFETTI("\uD83C\uDF89"),
    TRASH("\uD83D\uDDD1"),
    LOCK("\uD83D\uDD12"),
    HANDSHAKE("\uD83E\uDD1D"),
    BUSTS("\uD83D\uDC65"),
    YES("<:yes:1139209857438863471>"),
    DISCORD_LOGO("<:discordlogo11:1094722866903261205>"),
    PUSH_PIN("\uD83D\uDCCC"),
    CHAT_BOX("<:varilxChatbox:1136013753301868555>"),
    DISCORD("<:discord:1048627445735096320>"),
    TUBE_HOSTING("<:TubehostingVarilx:1101657813794693120>");

    private final String data;

    public Emoji getFormatted() {
        return Emoji.fromFormatted(this.data);
    }

    public Emoji unicode() {
        return Emoji.fromUnicode(this.data);
    }

}
