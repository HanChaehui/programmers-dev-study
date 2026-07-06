package com.example.spring.springbootapi;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Getter @Setter @ToString
public class WeatherResponse {
    private Response response;
}

@Getter @Setter @ToString
class Response {
    private Header header;
    private Body body;
}

@Getter @Setter @ToString
class Header {
    private String resultCode;
    private String resultMsg;
}

@Getter @Setter @ToString
class Body {
    private Items items;
    private int pageNo;
    private int numOfRows;
    private int totalCount;
}

@Getter @Setter @ToString
class Items {
    private List<Item> item;
}

@Getter @Setter @ToString
class Item {
    private String baseDate;
    private String baseTime;
    private String category;
    private int nx;
    private int ny;
    private String obsrValue;
}
