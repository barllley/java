import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicPlayerUI extends JFrame {
    private final MusicPlayer player;
    private final JLabel currentSongLabel;
    private final JList<Song> songList;
    private final DefaultListModel<Song> songListModel;
    private final DefaultListModel<Playlist> playlistListModel;
    private boolean isPlaying = false;
    private boolean isRepeating = false;
    private Thread playThread;

    private static final Logger LOGGER = Logger.getLogger(MusicPlayerUI.class.getName());

    public MusicPlayerUI() {
        player = new MusicPlayer();
        setTitle("Music Player");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.setBackground(new Color(162, 116, 172));

        JButton playButton = createButton("Play");
        JButton prevButton = createButton("Prev");
        JButton nextButton = createButton("Next");
        JButton repeatButton = createButton("Repeat");

        playButton.addActionListener(e -> playOrPause());
        prevButton.addActionListener(e -> playPrevious());
        nextButton.addActionListener(e -> playNext());
        repeatButton.addActionListener(e -> toggleRepeat());

        controlPanel.add(prevButton);
        controlPanel.add(playButton);
        controlPanel.add(nextButton);
        controlPanel.add(repeatButton);

        currentSongLabel = new JLabel("No song playing");
        currentSongLabel.setForeground(Color.WHITE);
        add(currentSongLabel, BorderLayout.NORTH);
        add(controlPanel, BorderLayout.SOUTH);

        JPanel sidePanel = createSidePanel();

        add(sidePanel, BorderLayout.WEST);

        songListModel = new DefaultListModel<>();
        songList = new JList<>(songListModel);
        songList.setBackground(new Color(195, 195, 218));
        add(new JScrollPane(songList), BorderLayout.CENTER);

        playlistListModel = new DefaultListModel<>();
        JList<Playlist> playlistList = new JList<>(playlistListModel);
        playlistList.setBackground(new Color(195, 195, 218));
        add(new JScrollPane(playlistList), BorderLayout.EAST);

        playlistList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = playlistList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        Playlist selectedPlaylist = playlistListModel.getElementAt(index);
                        showPlaylistContextMenu(e, selectedPlaylist, index);
                    }
                }
            }
        });

        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(162, 116, 172));
        button.setForeground(Color.WHITE);
        return button;
    }

    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(5, 1));
        sidePanel.setBackground(new Color(201, 114, 216));

        JButton chooseFolderButton = createButton("Choose Folder");
        JButton createPlaylistButton = createButton("Create Playlist");
        JButton loadPlaylistButton = createButton("Load Playlist");
        JButton showAllTracksButton = createButton("Show All Tracks");

        chooseFolderButton.addActionListener(e -> chooseFolder());
        createPlaylistButton.addActionListener(e -> createPlaylist());
        loadPlaylistButton.addActionListener(e -> loadPlaylist());
        showAllTracksButton.addActionListener(e -> showAllTracks());

        sidePanel.add(chooseFolderButton);
        sidePanel.add(createPlaylistButton);
        sidePanel.add(loadPlaylistButton);
        sidePanel.add(showAllTracksButton);

        return sidePanel;
    }

    private void showPlaylistContextMenu(MouseEvent e, Playlist playlist, int index) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem deleteItem = new JMenuItem("Delete Playlist");
        deleteItem.addActionListener(event -> deletePlaylist(index));

        JMenuItem openItem = new JMenuItem("Open Playlist");
        openItem.addActionListener(event -> openPlaylist(playlist));

        JMenuItem addSongItem = new JMenuItem("Add Song to Playlist");
        addSongItem.addActionListener(event -> addSongToPlaylist(playlist));

        contextMenu.add(openItem);
        contextMenu.add(deleteItem);
        contextMenu.add(addSongItem);

        contextMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void playOrPause() {
        if (isPlaying) {
            player.stop();
            isPlaying = false;
        } else {
            int selectedSongIndex = songList.getSelectedIndex();
            if (selectedSongIndex != -1) {
                player.playSong(selectedSongIndex);
                isPlaying = true;
            } else if (player.getCurrentPlaylist() != null) {
                player.playNextSongInPlaylist();
                isPlaying = true;
            } else {
                JOptionPane.showMessageDialog(this, "Please select a song or a playlist to play.");
            }
        }
    }

    private void playPrevious() {
        if (player.getCurrentPlaylist() != null) {
            player.playPreviousSongInPlaylist();
        } else {
            player.playPreviousSong();
        }
        updateCurrentSongLabel();
    }

    private void playNext() {
        if (player.getCurrentPlaylist() != null) {
            player.playNextSongInPlaylist();
        } else {
            player.playNextSong();
        }
        updateCurrentSongLabel();
    }

    private void toggleRepeat() {
        isRepeating = !isRepeating;
        JOptionPane.showMessageDialog(this, "Repeat is " + (isRepeating ? "on" : "off"));
    }

    private void chooseFolder() {
        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (folderChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File folder = folderChooser.getSelectedFile();
            player.loadSongsFromFolder(folder);
            songListModel.clear();
            for (Song song : player.getSongs()) {
                songListModel.addElement(song);
            }
            JOptionPane.showMessageDialog(this, "Songs loaded from: " + folder.getPath());
        }
    }

    private void createPlaylist() {
        List<Song> allSongs = player.getSongs();
        if (allSongs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No songs available to add to playlist.");
            return;
        }

        JDialog dialog = new JDialog(this, "Create Playlist", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());

        JList<Song> songList = new JList<>(new DefaultListModel<>());
        DefaultListModel<Song> listModel = (DefaultListModel<Song>) songList.getModel();
        allSongs.forEach(listModel::addElement);

        JButton createButton = createButton("Create");
        createButton.addActionListener(e -> {
            String playlistName = JOptionPane.showInputDialog("Enter playlist name:");
            if (playlistName != null && !playlistName.trim().isEmpty()) {
                List<Song> selectedSongs = songList.getSelectedValuesList();
                player.createPlaylist(playlistName, selectedSongs);
                playlistListModel.addElement(new Playlist(playlistName, selectedSongs));

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File folder = fileChooser.getSelectedFile();
                    player.savePlaylist(playlistName, new File(folder, playlistName + ".txt").getPath());
                }

                dialog.dispose();
            }
        });

        dialog.add(new JScrollPane(songList), BorderLayout.CENTER);
        dialog.add(createButton, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void loadPlaylist() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            List<Song> loadedSongs = new ArrayList<>();
            for (File file : files) {
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".txt") || fileName.endsWith(".m3u")) {
                    loadedSongs.addAll(player.loadPlaylist(file.toPath()));
                } else {
                    LOGGER.log(Level.WARNING, "Skipping non-playlist file: " + file.getPath());
                }
            }
            String playlistName = JOptionPane.showInputDialog("Enter playlist name:");
            if (playlistName != null && !playlistName.trim().isEmpty()) {
                Playlist playlist = new Playlist(playlistName, loadedSongs);
                player.addPlaylist(playlist);
                playlistListModel.addElement(playlist);
            }
        }
    }

    private void showAllTracks() {
        songListModel.clear();
        for (Song song : player.getSongs()) {
            songListModel.addElement(song);
        }
        player.setCurrentPlaylist(null);
    }

    private void deletePlaylist(int index) {
        player.deletePlaylist(index);
        playlistListModel.remove(index);
    }

    private void openPlaylist(Playlist playlist) {
        songListModel.clear();
        for (Song song : playlist.getSongs()) {
            songListModel.addElement(song);
        }
        player.setCurrentPlaylist(playlist);
        currentSongLabel.setText("Playlist - " + playlist.getName());
    }

    private void addSongToPlaylist(Playlist playlist) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getPath();
            String fileName = file.getName();
            String title = fileName.substring(0, fileName.lastIndexOf('.'));
            String artist = "Unknown Artist"; // You can change this to extract artist from file metadata if available
            Song song = new Song(title, artist);
            song.setFilePath(filePath);
            player.addSongToPlaylist(playlist.getName(), song);
            openPlaylist(playlist); // Refresh the playlist view to show the newly added song
        }
    }

    private void updateCurrentSongLabel() {
        Song currentSong = player.getCurrentSong();
        if (currentSong != null) {
            currentSongLabel.setText("Playing: " + currentSong);
        } else {
            currentSongLabel.setText("No song playing");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicPlayerUI::new);
    }

    public class MusicPlayer {
        private final List<Song> songs;
        private final List<Playlist> playlists;
        private Playlist currentPlaylist;
        private int currentSongIndex;
        private Player mp3Player;
        private BufferedInputStream currentInputStream;

        public MusicPlayer() {
            songs = new ArrayList<>();
            playlists = new ArrayList<>();
            currentPlaylist = null;
            currentSongIndex = -1;
        }

        public void addSong(Song song) {
            songs.add(song);
        }

        public List<Song> getSongs() {
            return songs;
        }

        public List<Playlist> getPlaylists() {
            return playlists;
        }

        public void loadSongsFromFolder(File folder) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    Song song = new Song(name.substring(0, name.lastIndexOf('.')), "Unknown Artist");
                    song.setFilePath(file.getPath());
                    addSong(song);
                }
            }
        }

        public void createPlaylist(String name, List<Song> selectedSongs) {
            Playlist playlist = new Playlist(name, selectedSongs);
            playlists.add(playlist);
        }

        public List<Song> loadPlaylist(Path filePath) {
            List<Song> loadedSongs = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(" - ");
                    if (parts.length == 3) {
                        Song song = new Song(parts[1], parts[0]);
                        song.setFilePath(parts[2]);
                        loadedSongs.add(song);
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load playlist from file: " + filePath, e);
            }
            return loadedSongs;
        }

        public void savePlaylist(String playlistName, String filename) {
            Playlist playlist = getPlaylistByName(playlistName);
            if (playlist != null) {
                try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(filename), StandardCharsets.UTF_8))) {
                    for (Song song : playlist.getSongs()) {
                        writer.println(song.getArtist() + " - " + song.getTitle() + " - " + song.getFilePath());
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to save playlist to file: " + filename, e);
                }
            } else {
                LOGGER.log(Level.WARNING, "Playlist not found: " + playlistName);
            }
        }

        protected void playSong(int songIndex) {
            if (mp3Player != null) {
                mp3Player.close();
            }
            try {
                Song song = songs.get(songIndex);
                currentInputStream = new BufferedInputStream(Files.newInputStream(Paths.get(song.getFilePath())));
                playThread = new Thread(() -> {
                    try {
                        mp3Player = new Player(currentInputStream);
                        mp3Player.play();
                        if (isRepeating) {
                            playSong(songIndex);
                        }
                    } catch (JavaLayerException e) {
                        LOGGER.log(Level.SEVERE, "Failed to play song: " + song, e);
                    }
                });
                playThread.start();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to play song", e);
            }
        }

        protected void playCurrentSong() {
            if (currentPlaylist == null) {
                if (currentSongIndex >= 0 && currentSongIndex < songs.size()) {
                    Song song = songs.get(currentSongIndex);
                    playSong(currentSongIndex);
                }
                return;
            }
            if (currentSongIndex >= 0 && currentSongIndex < currentPlaylist.getSongs().size()) {
                Song song = currentPlaylist.getSongs().get(currentSongIndex);
                playSong(currentSongIndex);
            }
        }

        public void playNextSongInPlaylist() {
            if (currentPlaylist == null) {
                return;
            }
            if (currentSongIndex < currentPlaylist.getSongs().size() - 1) {
                currentSongIndex++;
            } else {
                currentSongIndex = 0; // или другая логика для цикла
            }
            playCurrentSong();
        }

        public void playPreviousSongInPlaylist() {
            if (currentPlaylist == null) {
                return;
            }
            if (currentSongIndex > 0) {
                currentSongIndex--;
                playCurrentSong();
            }
        }

        public void playNextSong() {
            if (currentSongIndex < songs.size() - 1) {
                currentSongIndex++;
                playCurrentSong();
            }
        }

        public void playPreviousSong() {
            if (currentSongIndex > 0) {
                currentSongIndex--;
                playCurrentSong();
            }
        }

        public Song getCurrentSong() {
            if (currentPlaylist != null && currentSongIndex >= 0 && currentSongIndex < currentPlaylist.getSongs().size()) {
                return currentPlaylist.getSongs().get(currentSongIndex);
            }
            if (currentSongIndex >= 0 && currentSongIndex < songs.size()) {
                return songs.get(currentSongIndex);
            }
            return null;
        }

        public int getCurrentSongIndex() {
            return currentSongIndex;
        }

        public void deletePlaylist(int index) {
            if (index >= 0 && index < playlists.size()) {
                playlists.remove(index);
            }
        }

        public void addSongToPlaylist(String playlistName, Song song) {
            Playlist playlist = playlists.stream().filter(pl -> pl.getName().equals(playlistName)).findFirst().orElse(null);
            if (playlist != null) {
                playlist.addSong(song);
            } else {
                JOptionPane.showMessageDialog(null, "Playlist not found: " + playlistName);
            }
        }

        public Playlist getPlaylistByName(String name) {
            return playlists.stream().filter(pl -> pl.getName().equals(name)).findFirst().orElse(null);
        }

        public Playlist getCurrentPlaylist() {
            return currentPlaylist;
        }

        public void setCurrentPlaylist(Playlist playlist) {
            currentPlaylist = playlist;
        }

        public void addPlaylist(Playlist playlist) {
            playlists.add(playlist);
        }

        public void stop() {
            if (mp3Player != null) {
                mp3Player.close();
            }
            if (currentInputStream != null) {
                try {
                    currentInputStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "Failed to close input stream", e);
                }
            }
        }
    }

    public static class Playlist {
        private final String name;
        private final List<Song> songs;

        public Playlist(String name, List<Song> songs) {
            this.name = name;
            this.songs = songs;
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

        @Override
        public String toString() {
            return name + " (" + songs.size() + " songs)";
        }
    }

    public static class Song {
        private final String title;
        private final String artist;
        private String filePath;

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

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return title + " by " + artist;
        }
    }
}
