package com.firebase.notification.test.ubiquobusiness;

import java.util.ArrayList;

public class StaticData {

    public ArrayList<String> names;
    public ArrayList<String> numbers;
    public String desc;


    public StaticData(){

    }

    public StaticData (String desc, ArrayList names,ArrayList numbers){

        this.desc=desc;
        this.names=names;
        this.numbers=numbers;
    }


    public ArrayList<String> getNames() {
        return names;
    }

    public void setNames(ArrayList<String> names) {
        this.names = names;
    }

    public ArrayList<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(ArrayList<String> numbers) {
        this.numbers = numbers;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
