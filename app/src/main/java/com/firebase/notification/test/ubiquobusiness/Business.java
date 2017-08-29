package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 08/06/2017.
 */



public class Business {

    public String contacts, phones, id;
    public String name,adress,number,city,image;
    public Double latitude,longitude;
    public Integer rating,likes;
    public String openingTime, closingTime;
    public String arguments;
    public String token;
    public Long iscrizione;


    public Business(){

    }

    public Business(String id, String contacts, String phones, String name, String adress, String number, String city, Double latitude, Double longitude, Integer rating, Integer likes, String openingTime, String closingTime, String image, String arguments, String token, Long iscrizione) {
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
        this.token = token;
        this.iscrizione = iscrizione;
    }

    public Long getIscrizione() {
        return iscrizione;
    }

    public void setIscrizione(Long iscrizione) {
        this.iscrizione = iscrizione;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
    }
}
