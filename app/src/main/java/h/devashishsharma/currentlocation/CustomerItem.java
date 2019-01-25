package h.devashishsharma.currentlocation;

public class CustomerItem {
    String cusid, title, latlongs, address, contact, email;

    public CustomerItem(String cusid, String title, String latlongs, String address, String contact, String email) {
        this.cusid = cusid;
        this.title = title;
        this.latlongs = latlongs;
        this.address = address;
        this.contact = contact;
        this.email = email;
    }

    public String getCusid() {
        return cusid;
    }

    public String getTitle() {
        return title;
    }

    public String getLatlongs() {
        return latlongs;
    }

    public String getAddress() {
        return address;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }
}
