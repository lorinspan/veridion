package com.veridion.assignment.response;

import com.veridion.assignment.model.Company;

public class MatchResponse {
    private Company matchedCompany;
    private double score;

    // Constructor
    public MatchResponse(Company matchedCompany, double score) {
        this.matchedCompany = matchedCompany;
        this.score = score;
    }

    // Getters and setters
    public Company getMatchedCompany() {
        return matchedCompany;
    }

    public void setMatchedCompany(Company matchedCompany) {
        this.matchedCompany = matchedCompany;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}