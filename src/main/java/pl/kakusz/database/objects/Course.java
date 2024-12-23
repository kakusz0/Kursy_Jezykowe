package pl.kakusz.database.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Course {

    private String name;
    private String description;
    private int price;

    public Course(String name, String description, int price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
