package de.derioo.config.local;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(SystemStubsExtension.class)
public class LocalConfigTest {

    @Test
    public void testLoadByENV(EnvironmentVariables vars) {
        vars.set("DISCORD_BOT_TOKEN", "TOKEN");
        vars.set("DB_CONNECTION_STRING", "CONNECTION_STRING");
        vars.set("TIKTOK_TOKEN", "TIKTOK_TOKEN");

        LocalConfig config = LocalConfig.loadByENV();

        assertThat(config.getConnectionString()).isEqualTo("CONNECTION_STRING");
        assertThat(config.getToken()).isEqualTo("TOKEN");
        assertThat(config.getTiktok().getToken()).isEqualTo("TIKTOK_TOKEN");
    }

}
