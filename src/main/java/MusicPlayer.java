import java.util.*;
import java.io.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

class Song {
    private String title;
    private String artist;

    public Song(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    @Override
    public String toString() {
        return artist + " - " + title;
    }
}

class Playlist {
    private String name;
    private List<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void removeSong(int index) {
        if (index >= 0 && index < songs.size()) {
            songs.remove(index);
        } else {
            System.out.println("Invalid song index.");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Playlist: ").append(name).append("\n");
        for (int i = 0; i < songs.size(); i++) {
            sb.append(i + 1).append(". ").append(songs.get(i)).append("\n");
        }
        return sb.toString();
    }
}

public class MusicPlayer {
    private List<Song> songs;
    private List<Playlist> playlists;
    private Playlist currentPlaylist;
    private int currentSongIndex;
    private Player mp3Player;
    private Thread playThread;

    public MusicPlayer() {
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        currentPlaylist = null;
        currentSongIndex = -1;
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public void showSongs() {
        for (int i = 0; i < songs.size(); i++) {
            System.out.println((i + 1) + ". " + songs.get(i));
        }
    }

    public void createPlaylist(String name) {
        playlists.add(new Playlist(name));
    }

    public void selectPlaylist(int index) {
        if (index >= 0 && index < playlists.size()) {
            currentPlaylist = playlists.get(index);
            currentSongIndex = -1;
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void showPlaylist(int index) {
        if (index >= 0 && index < playlists.size()) {
            System.out.println(playlists.get(index));
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void savePlaylist(int index, String filename) {
        if (index >= 0 && index < playlists.size()) {
            try (PrintWriter writer = new PrintWriter(new File(filename))) {
                Playlist playlist = playlists.get(index);
                for (Song song : playlist.getSongs()) {
                    writer.println(song.getArtist() + " - " + song.getTitle());
                }
            } catch (IOException e) {
                System.out.println("Error saving playlist: " + e.getMessage());
            }
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void deletePlaylist(int index) {
        if (index >= 0 && index < playlists.size()) {
            playlists.remove(index);
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void addSongToPlaylist(int playlistIndex, String title, String artist) {
        if (playlistIndex >= 0 && playlistIndex < playlists.size()) {
            Playlist playlist = playlists.get(playlistIndex);
            playlist.addSong(new Song(title, artist));
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void removeSongFromPlaylist(int playlistIndex, int songIndex) {
        if (playlistIndex >= 0 && playlistIndex < playlists.size()) {
            Playlist playlist = playlists.get(playlistIndex);
            playlist.removeSong(songIndex);
        } else {
            System.out.println("Invalid playlist index.");
        }
    }

    public void playNextSong() {
        if (currentPlaylist == null) {
            System.out.println("No playlist selected.");
            return;
        }
        if (currentSongIndex < currentPlaylist.getSongs().size() - 1) {
            currentSongIndex++;
            playCurrentSong();
        } else {
            System.out.println("End of playlist.");
        }
    }

    public void playPreviousSong() {
        if (currentPlaylist == null) {
            System.out.println("No playlist selected.");
            return;
        }
        if (currentSongIndex > 0) {
            currentSongIndex--;
            playCurrentSong();
        } else {
            System.out.println("Already at the beginning of the playlist.");
        }
    }

    protected void repeatCurrentSong() {
        playCurrentSong();
    }

    private void playCurrentSong() {
        if (currentPlaylist == null || currentSongIndex < 0 || currentSongIndex >= currentPlaylist.getSongs().size()) {
            System.out.println("No song to play.");
            return;
        }
        Song song = currentPlaylist.getSongs().get(currentSongIndex);
        try {
            if (mp3Player != null) {
                mp3Player.close();
            }
            playThread = new Thread(() -> {
                try {
                    mp3Player = new Player(new FileInputStream(new File(song.getTitle() + ".mp3")));
                    mp3Player.play();
                } catch (JavaLayerException | IOException e) {
                    e.printStackTrace();
                }
            });
            playThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Song> getSongs() {
        return songs;
    }

    public List<Playlist> getPlaylists() {
        return playlists;
    }

    public Playlist getCurrentPlaylist() {
        return currentPlaylist;
    }

    public void loadPlaylist(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(filename)))) {
            String line;
            Playlist playlist = new Playlist(filename);
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" - ");
                if (parts.length == 2) {
                    playlist.addSong(new Song(parts[1], parts[0]));
                }
            }
            playlists.add(playlist);
        } catch (IOException e) {
            System.out.println("Error loading playlist: " + e.getMessage());
        }
    }
}
