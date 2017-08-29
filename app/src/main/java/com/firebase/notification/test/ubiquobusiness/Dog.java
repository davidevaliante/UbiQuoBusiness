package com.firebase.notification.test.ubiquobusiness;

/**
 * Created by akain on 28/08/2017.
 */

public class Dog {

    String name;
    Integer age;
    Float weight;


    public Dog (String name, Integer age, Float weight){
        this.name =name;
        this.age=age;
        this.weight=weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }
}
