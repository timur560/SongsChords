/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package songschords;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author qwer
 */
public class SongsProcessor {
    private static final String PATH = "./songschords";
    
    public static final String HTML_HEADER_TPL = "<html><body style=\"margin:10px;font-family:"
            + "Tahoma,Arial;font-size:%dpx;\">";
    public static final String HTML_FOOTER = "</body></html>";
    
    public static int FONT_SIZE = 12;
    
// <[author] <[title], [filename,text]>>
    private static Map<String, Map<String, String[]>> songs = new HashMap<>();
    
    public static final Set<String> getAuthorsSet() {
        return getSongs().keySet();
    }
    
    public static final Set<String> getAuthorTitlesSet(String author) {
        if (getSongs().get(author) != null) {
            return getSongs().get(author).keySet();
        } else {
            return new HashSet<String>();
        }
    }
    
    public static final String getSongsPath() {
        File songsDir = new File(PATH);
        
        if (!songsDir.exists() || !songsDir.isDirectory()) {
            songsDir.mkdir();
        }
        
        return PATH;
    }
    
    public static final String getHtmlHeader() {
        return String.format(HTML_HEADER_TPL, FONT_SIZE);
    }
    
    public static final String songText2Html(String songText) {
        String text = "";
        for (String line : songText.split("\n")) {
            if (line.indexOf(">") == 0) {
                line = line.substring(1).replaceAll("([^ |	]+)", "<a style=\"color:#33B537\" href=\"#\"><b>$1</b></a>");
            }
            text += line + "<br>";
        }
        
        String html = getHtmlHeader();
               
        html += text;
        
        html += HTML_FOOTER;
        return html;
    }
    
    public static final String getSongTextByAuthorAndTitle(String author, String title) {
        Map<String, Map<String, String[]>> songs = getSongs();
        
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

    public static final Map<String, Map<String, String[]>> getSongsFiltered(String filter) {
        Map<String, Map<String, String[]>> songsFiltered = new HashMap<>();
        
        Map<String, String[]> authorSongsMap = new HashMap<>();
        
        for (String author : getSongs().keySet()) {
            if (author.toLowerCase().contains(filter.toLowerCase())) {
                songsFiltered.put(author, getSongs().get(author));
                continue;
            }

            authorSongsMap.clear();
            
            for (String title : getSongs().get(author).keySet()) {
                if (title.toLowerCase().contains(filter.toLowerCase())) {
                    authorSongsMap.put(title, getSongs().get(author).get(title));
                    continue;
                }
            }
            
            if (!authorSongsMap.isEmpty()) {
                songsFiltered.put(author, authorSongsMap);
            }
        }
        
        return songsFiltered;
    }
    
    public static final Map<String, Map<String, String[]>> getSongs(boolean refresh) {
        // author1
        //  song1 - text
        //  song2 - text
        // author2
        // ...
        
        if (!songs.isEmpty() && !refresh) {
            return songs;
        }
        
        Map<String, Map<String, String[]>> songs = new HashMap<>();
        
        File songsDir = new File(getSongsPath());
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
        return getSongs(false);
    }

    public static final boolean exists(String author, String title) {
        Map<String, Map<String, String[]>> songs = getSongs();
        
        if (songs.get(author) != null && songs.get(author).get(title) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public static final String getFilenameByAuthorAndTitle(String author, String title) {
        Map<String, Map<String, String[]>> songs = getSongs();
        
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
        SongResult result = validateSong(author, title, song);
        
        if (!result.status) {
            return result;
        }

        String songsDir = getSongsPath();
        
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
    
    public static BufferedImage getChordImage(String chord) throws IOException {
        /*
                E -> E_0.gif
                Em -> E7_0.gif
                E# -> Ew_0.gif
                E#m -> Ewm_0.gif
                E7 -> E7_0.gif
        */

        URL resource = SongsProcessor.class.getResource("/images/chords/" + chord.replaceAll("#", "w") + "_0.gif");
        
        if (resource != null) {
            return ImageIO.read(resource);
        } else {
            return null;
        }
    }
    
    public static void removeSong(String author, String title) {
        File songFile = new File(PATH + "/" + getSongs().get(author).get(title)[0]);
        songFile.delete();
    }
    
    public static boolean importFile(File inputFile) {
        try {
            Scanner scanner;
            scanner = new Scanner(inputFile);
            String author = "", title = "", songText = "", s;
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

            if (author.isEmpty() || title.isEmpty() || songText.isEmpty()) {
                return false;
            }

            if (getSongs().get(author) != null && getSongs().get(author).get(title) != null) { // song already exists
                return false;
            }
            
            Files.copy(inputFile.toPath(), new File(PATH + "/" + System.currentTimeMillis() + ".sng").toPath());

            return true;
        } catch (Exception ex) {
            Logger.getLogger(SongsProcessor.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }
}

