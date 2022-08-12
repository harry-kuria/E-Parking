package com.example.e_parking.classes;

import java.util.Comparator;

public class BookedSlotKey {
    public BookedSlots bookedSlots;
    public String key;

    public BookedSlotKey(){}
    public BookedSlotKey(BookedSlots bookedSlots, String key){
        this.bookedSlots=bookedSlots;
        this.key=key;
    }
}

