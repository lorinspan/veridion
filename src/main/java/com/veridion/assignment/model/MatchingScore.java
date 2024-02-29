package com.veridion.assignment.model;

public class MatchingScore {
    private Company company;
    private double score;

    public MatchingScore(Company company, double score) {
        this.company = company;
        this.score = score;
    }

    public Company getCompany() {
        return company;
    }

    public double getScore() {
        return score;
    }
}