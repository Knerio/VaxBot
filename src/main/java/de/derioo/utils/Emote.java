package de.derioo.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.emoji.Emoji;

@AllArgsConstructor
@Getter
public enum Emote {


    PARTY_EMOTE("<a:Vaxparty:1139957321196376107>"),
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
    PARTNER("<:partner:1140656134504599552>"),
    THUMBS_UP("\uD83D\uDC4D"),
    THUMBS_DOWN("\uD83D\uDC4E"),
    CONFETTI("\uD83C\uDF89"),
    TRASH("\uD83D\uDDD1"),
    LOCK("\uD83D\uDD12"),
    HANDSHAKE("\uD83E\uDD1D"),
    BUSTS("\uD83D\uDC65"),
    PUSH_PIN("\uD83D\uDCCC");

    private final String data;

    public Emoji getFormatted() {
        return Emoji.fromFormatted(this.data);
    }

    public Emoji unicode() {
        return Emoji.fromUnicode(this.data);
    }

}