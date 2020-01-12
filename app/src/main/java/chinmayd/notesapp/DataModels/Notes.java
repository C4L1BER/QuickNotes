package chinmayd.notesapp.DataModels;

public class Notes {
    private int id;

    private String title;

    private String data;

    private String timeStamp;

    public Notes(int id, String title, String data, String timeStamp) {
        this.id = id;
        this.title = title;
        this.data = data;
        this.timeStamp = timeStamp;
    }

    public Notes() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
