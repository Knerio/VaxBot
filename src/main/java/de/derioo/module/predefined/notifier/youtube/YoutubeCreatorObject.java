package de.derioo.module.predefined.notifier.youtube;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Getter
@Setter
public class YoutubeCreatorObject {

    private final String name;
    private final String id;

}
