import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        MusicPlayer player = new MusicPlayer();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("0 - Exit");
            System.out.println("1 - Show songs");
            System.out.println("2 - Create playlist");
            System.out.println("3 - Select playlist");
            System.out.println("4 - Save playlist");
            System.out.println("5 - Delete playlist");
            System.out.println("6 - Add song to playlist");
            System.out.println("7 - Show playlist");
            System.out.println("8 - Remove song from playlist");
            System.out.println("9 - Play next song");
            System.out.println("10 - Play previous song");
            System.out.println("11 - Repeat current song");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 0:
                    System.out.println("Exiting...");
                    return;
                case 1:
                    player.showSongs();
                    break;
                case 2:
                    System.out.print("Enter playlist name: ");
                    String playlistName = scanner.nextLine();
                    player.createPlaylist(playlistName);
                    break;
                case 3:
                    System.out.print("Enter playlist index: ");
                    int playlistIndex = scanner.nextInt();
                    player.selectPlaylist(playlistIndex);
                    break;
                case 4:
                    System.out.print("Enter playlist index to save: ");
                    int saveIndex = scanner.nextInt();
                    scanner.nextLine(); // consume newline
                    System.out.print("Enter filename: ");
                    String filename = scanner.nextLine();
                    player.savePlaylist(saveIndex, filename);
                    break;
                case 5:
                    System.out.print("Enter playlist index to delete: ");
                    int deleteIndex = scanner.nextInt();
                    player.deletePlaylist(deleteIndex);
                    break;
                case 6:
                    System.out.print("Enter playlist index: ");
                    int plIndex = scanner.nextInt();
                    scanner.nextLine(); // consume newline
                    System.out.print("Enter song title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter song artist: ");
                    String artist = scanner.nextLine();
                    player.addSongToPlaylist(plIndex, title, artist);
                    break;
                case 7:
                    System.out.print("Enter playlist index: ");
                    int showIndex = scanner.nextInt();
                    player.showPlaylist(showIndex);
                    break;
                case 8:
                    System.out.print("Enter playlist index: ");
                    int pIndex = scanner.nextInt();
                    System.out.print("Enter song index to remove: ");
                    int songIndex = scanner.nextInt();
                    player.removeSongFromPlaylist(pIndex, songIndex);
                    break;
                case 9:
                    player.playNextSong();
                    break;
                case 10:
                    player.playPreviousSong();
                    break;
                case 11:
                    player.repeatCurrentSong();
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
