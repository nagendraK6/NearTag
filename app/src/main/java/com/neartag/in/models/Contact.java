package com.neartag.in.models;

public class Contact {
    private String Name;
    private String Phone;
    private Boolean IsSelected;

    public Contact(String name, String phone) {
        this.Name = name;
        this.Phone = phone;
        this.IsSelected = false;
    }

    public String getName() {
        return Name;
    }


    public String getPhone() {
        return Phone;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public Boolean getSelected() {
        return IsSelected;
    }

    public void setSelected(Boolean selected) {
        IsSelected = selected;
    }
}
