package poc.cevt.hmi.com.musicplayerapp;

public class User {
    private String firstName;

    private String lastName;

    private String username;

    private String email;

    Queue queue;

    private Song currentSong;

    private Song prevSong;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    void setCurrentSong(Song currentSong) {
        this.currentSong = currentSong;
    }

    void setPrevSong(Song prevSong) {
        this.prevSong = prevSong;
    }

    Song getCurrentSong() {
        return currentSong;
    }

    Song getPrevSong() {
        return prevSong;
    }
}
