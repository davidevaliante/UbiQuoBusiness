package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 08/06/2017.
 */



public class Business {

    String contacts, phones;
    String name,adress,number,city,image;
    Long latitude,longitude;
    Integer rating,likes;
    Long openingTime, closingTime;
    String arguments;


    public Business(){

    }

    public Business(String contacts, String phones, String name, String adress, String number, String city, Long latitude, Long longitude, Integer rating, Integer likes, Long openingTime, Long closingTime, String image, String arguments) {
        this.contacts = contacts;
        this.phones = phones;
        this.name = name;
        this.adress = adress;
        this.number = number;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.likes = likes;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.image = image;
        this.arguments = arguments;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getPhones() {
        return phones;
    }

    public void setPhones(String phones) {
        this.phones = phones;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getLikes() {
        return likes;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Long getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(Long openingTime) {
        this.openingTime = openingTime;
    }

    public Long getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(Long closingTime) {
        this.closingTime = closingTime;
    }
}
