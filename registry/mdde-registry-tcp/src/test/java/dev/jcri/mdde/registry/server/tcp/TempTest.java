package dev.jcri.mdde.registry.server.tcp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

public class TempTest {
    // TODO: Remove later

    @Test
    public void tempTest(){
        String tmpString = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor " +
                "invidunt ut labore et dolore magna aliqu";

        byte[] tmpBytes = tmpString.getBytes(StandardCharsets.US_ASCII);
        assertEquals(tmpString.length(), tmpBytes.length);

        }
}
