import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MusicPlayerTest {
    private MusicPlayerUI.MusicPlayer player;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        player = Mockito.spy(new MusicPlayerUI().new MusicPlayer());
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void testAddSong() {
        MusicPlayerUI.Song song = new MusicPlayerUI.Song("Title", "Artist");
        player.addSong(song);
        assertEquals(1, player.getSongs().size());
    }

    @Test
    void testShowSongs() {
        MusicPlayerUI.Song song1 = new MusicPlayerUI.Song("Title1", "Artist1");
        MusicPlayerUI.Song song2 = new MusicPlayerUI.Song("Title2", "Artist2");
        player.addSong(song1);
        player.addSong(song2);
        player.getSongs().forEach(System.out::println);
        String expectedOutput = "Title1 by Artist1\nTitle2 by Artist2\n";
        assertEquals(expectedOutput.trim(), outContent.toString().trim());
    }

    @Test
    void testCreatePlaylist() {
        ArrayList<MusicPlayerUI.Song> emptySongs = new ArrayList<>();
        player.createPlaylist("Test Playlist", emptySongs);
        assertEquals(1, player.getPlaylists().size());
    }

    @Test
    void testSelectPlaylist() {
        ArrayList<MusicPlayerUI.Song> emptySongs = new ArrayList<>();
        player.createPlaylist("Test Playlist", emptySongs);
        player.setCurrentPlaylist(player.getPlaylists().get(0));
        assertEquals("Test Playlist", player.getCurrentPlaylist().getName());
    }

    @Test
    void testShowPlaylist() {
        ArrayList<MusicPlayerUI.Song> emptySongs = new ArrayList<>();
        player.createPlaylist("Test Playlist", emptySongs);
        player.setCurrentPlaylist(player.getPlaylists().get(0));
        player.addSongToPlaylist("Test Playlist", new MusicPlayerUI.Song("Title", "Artist"));
        player.getCurrentPlaylist().getSongs().forEach(System.out::println);
        String expectedOutput = "Title by Artist\n";
        assertEquals(expectedOutput.trim(), outContent.toString().trim());
    }

    @Test
    void testPlayNextSong() {
        ArrayList<MusicPlayerUI.Song> emptySongs = new ArrayList<>();
        player.createPlaylist("Test Playlist", emptySongs);
        player.addSongToPlaylist("Test Playlist", new MusicPlayerUI.Song("Title1", "Artist1"));
        player.addSongToPlaylist("Test Playlist", new MusicPlayerUI.Song("Title2", "Artist2"));
        player.setCurrentPlaylist(player.getPlaylists().get(0));

        doNothing().when(player).playCurrentSong();

        player.playNextSongInPlaylist();
        assertEquals(0, player.getCurrentSongIndex());
        player.playNextSongInPlaylist();
        assertEquals(1, player.getCurrentSongIndex());
    }

    @Test
    void testPlayPreviousSong() {
        ArrayList<MusicPlayerUI.Song> emptySongs = new ArrayList<>();
        player.createPlaylist("Test Playlist", emptySongs);
        player.addSongToPlaylist("Test Playlist", new MusicPlayerUI.Song("Title1", "Artist1"));
        player.addSongToPlaylist("Test Playlist", new MusicPlayerUI.Song("Title2", "Artist2"));
        player.setCurrentPlaylist(player.getPlaylists().get(0));

        doNothing().when(player).playCurrentSong();

        player.playNextSongInPlaylist(); // index becomes 0
        player.playNextSongInPlaylist(); // index becomes 1
        player.playPreviousSongInPlaylist();
        assertEquals(0, player.getCurrentSongIndex());
    }
}
