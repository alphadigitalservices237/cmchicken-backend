package com.limiter.demo.api;

import com.limiter.demo.models.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
//

import org.springframework.beans.factory.annotation.Autowired;

public class Trials {
    public static void main(String[] args) {
       
        
        List<Obj> p = new ArrayList<>();
        List<String> lm = new ArrayList<>();
        p.add(new Obj("bea",25));
        p.add(new Obj("leo",20));


//         List<Obj> objects=  p.stream().filter(obj->
//                   obj.getName().equals("dave"))
//                   .collect(Collectors.toList());
        Optional<Obj> objects = p.stream().max(Comparator.comparingInt(Obj::getAge));
        System.out.println(objects.get());
        MyInterface myInterface = (name)->"hello"+name;
//         objects.forEach(System.out::println);
//        System.out.println();
    }
}
@Data
@AllArgsConstructor
class Obj{
    String name;
    int age;
}
