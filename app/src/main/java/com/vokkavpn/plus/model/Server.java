package com.vokkavpn.plus.model;

public class Server {

    public String ID;
    public String File;
    public String Country;
    public String Image;


    public String GetID() {
        return ID;
    }
    public void SetID(String ID) {
        this.ID = ID;
    }

    public String GetFileID() {
        return File;
    }
    public void SetFileID(String FileID) {
        this.File = FileID;
    }

    public String GetCountry() {
        return Country;
    }
    public void SetCountry(String Country) {
        this.Country = Country;
    }

    public String GetImage() {
        return Image;
    }
    public void SetImage(String Image) {
        this.Image = Image;
    }


}
