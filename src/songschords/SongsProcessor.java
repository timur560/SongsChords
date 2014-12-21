/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package songschords;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author qwer
 */
public class SongsProcessor {
    private static final String PATH = "./songschords";
    
    public static final String HTML_HEADER = "<html><body style=\"margin:10px;font-family:"
            + "Tahoma,Arial;font-size:12px;\">";
    public static final String HTML_FOOTER = "</body></html>";
    
    // <[author] <[title], [filename,text]>>
    private static Map<String, Map<String, String[]>> songs = new HashMap<>();
    
    public static final String getSongsPath() {
        File songsDir = new File(PATH);
        
        if (!songsDir.exists() || !songsDir.isDirectory()) {
            songsDir.mkdir();
        }
        
        return PATH;
    }
    
    public static final String songText2Html(String songText) {
        String text = "";
        for (String line : songText.split("\n")) {
            if (line.indexOf(">") == 0) {
                line = line.substring(1).replaceAll("([^ |	]+)", "<span style=\"color:#33B537\"><b>$1</b></span>");
            }
            text += line + "<br>";
        }
        
        String html = HTML_HEADER;
               
        html += text;
        
        html += HTML_FOOTER;
        return html;
    }
    
    public static final String getSongTextByAuthorAndTitle(String author, String title) {
        Map<String, Map<String, String[]>> songs = SongsProcessor.getSongs();
        
        if (songs.get(author) != null) {
            String songText = songs.get(author).get(title)[1];
            if (songText != null) {
                return songText;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static final Map<String, Map<String, String[]>> getSongs(boolean refresh) {
        // author1
        //  song1 - text
        //  song2 - text
        // author2
        // ...
        
        if (!SongsProcessor.songs.isEmpty() && !refresh) {
            return songs;
        }
        
        Map<String, Map<String, String[]>> songs = new HashMap<>();
        
        File songsDir = new File(SongsProcessor.getSongsPath());
        Scanner scanner;
        String s, author, title, songText;
        
        try {
            for (File songFile : songsDir.listFiles()) {
                scanner = new Scanner(songFile);
                author = title = songText = "";
                while (scanner.hasNext()) {
                    s = scanner.nextLine();
                    if (s.indexOf("Author: ") == 0) {
                        author = title = s.substring(8);
                    } else if (s.indexOf("Title: ") == 0) {
                        title = s.substring(7);
                    } else {
                        songText += s + "\n";
                    }
                }
                
                if (author.isEmpty() || title.isEmpty()) {
                    continue;
                }
                
                if (songs.get(author) == null) {
                    songs.put(author, new HashMap<String, String[]>());
                }
                
                songs.get(author).put(title, new String[]{songFile.getName(), songText});
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SongsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        SongsProcessor.songs = songs;
        
        return songs;
    }

    public static final Map<String, Map<String, String[]>> getSongs() {
        return SongsProcessor.getSongs(false);
    }

    public static final boolean exists(String author, String title) {
        Map<String, Map<String, String[]>> songs = SongsProcessor.getSongs();
        
        if (songs.get(author) != null && songs.get(author).get(title) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public static final String getFilenameByAuthorAndTitle(String author, String title) {
        Map<String, Map<String, String[]>> songs = SongsProcessor.getSongs();
        
        if (songs.get(author) != null && songs.get(author).get(title) != null) {
            return songs.get(author).get(title)[0];
        }
                
        return null;
    }

    public static final SongResult validateSong(String author, String title, String song) {
        // TODO: 
        // 1. check if all fields not empty
        // 2. check max length
        
        return new SongResult(true);
    }

    public static final SongResult editSong(String filename, String author, String title, String song) {
        SongResult result = SongsProcessor.validateSong(author, title, song);
        
        if (!result.status) {
            return result;
        }

        String songsDir = SongsProcessor.getSongsPath();
        
        if (filename.length() == 0) {
            filename = System.currentTimeMillis() + ".sng";
        }

        try {
            try (PrintWriter out = new PrintWriter(new File(songsDir + "/" + filename))) {
                out.print("Author: " + author + "\nTitle: " + title + "\n" + song);
                out.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SongsProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new SongResult(true);
    }
}

