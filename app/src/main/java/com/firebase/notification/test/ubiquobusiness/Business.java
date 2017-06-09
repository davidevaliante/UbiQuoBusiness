package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 08/06/2017.
 */



public class Business {

    String contacts, phones, id;
    String name,adress,number,city,image;
    Double latitude,longitude;
    Integer rating,likes;
    Long openingTime, closingTime;
    String arguments;


    public Business(){

    }

    public Business(String id, String contacts, String phones, String name, String adress, String number, String city, Double latitude, Double longitude, Integer rating, Integer likes, Long openingTime, Long closingTime, String image, String arguments) {
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
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArguments() {
        return arguments;
    }

    public void setArguments(String arguments) {
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
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
