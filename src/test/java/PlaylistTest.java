import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class PlaylistTest {
    private Playlist playlist;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        playlist = new Playlist("Test Playlist");
    }

    @Test
    public void testAddSong() {
        Song song = new Song("Title", "Artist");
        playlist.addSong(song);
        assertEquals(1, playlist.getSongs().size());
    }

    @Test
    public void testRemoveSong() {
        Song song = new Song("Title", "Artist");
        playlist.addSong(song);
        playlist.removeSong(0);
        assertEquals(0, playlist.getSongs().size());
    }

    @Test
    public void testToString() {
        Song song = new Song("Title", "Artist");
        playlist.addSong(song);
        String expected = "Playlist: Test Playlist\n1. Artist - Title\n";
        assertEquals(expected.trim(), playlist.toString().trim());
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }
}
