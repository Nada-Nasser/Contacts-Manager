package com.example.mycontacts;

public class ContactItem
{
    private String Name;
    private String PhoneNumber;

    public ContactItem(String name, String phoneNumber) {
        Name = name;
        PhoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContactItem)) return false;
        ContactItem that = (ContactItem) o;
        return getName().equals(that.getName()) &&
                getPhoneNumber().equals(that.getPhoneNumber());
    }

    @Override
    public String toString() {
        return "ContactItem{" +
                "Name='" + Name + '\'' +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                '}';
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }
}
