package de.derioo;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import static org.assertj.core.api.Assertions.assertThat;


public class MainTest {

    @Test
    public void testAPIOnly() throws IOException, InterruptedException {
        Main.main(new String[]{"apionly"});

        Thread.sleep(2000); // Wait for api startup

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080/ping")
                .get()
                .build();
        String res = client.newCall(request).execute().body().string();
        assertThat(res).isEqualTo("Pong! (Sadge)");
    }

}
