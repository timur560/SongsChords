package songschords;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author qwer
 */
public class SongResult {
    public boolean status;
    public String message = "";
    
    public SongResult(boolean status) {
        this.status = status;
    }

    public SongResult(boolean status, String message) {
        this.status = status;
        this.message = message;
    }
}