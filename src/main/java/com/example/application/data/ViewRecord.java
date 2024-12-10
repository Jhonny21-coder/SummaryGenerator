package com.example.application.data;

import jakarta.persistence.*;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class ViewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date; // Tracks specific day
    private int views; // Views for the specific day

    public ViewRecord(LocalDate date, int views) {
    	this.date = date;
    	this.views = views;
    }
}
