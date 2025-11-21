package com.clipers.clipers.dto;

import java.util.List;

public class TechnicalTestStructure {
    private String companyName;
    private String jobTitle;
    private String description;
    private Integer timeLimit; // in minutes
    private List<Question> questions;
    
    public static class Question {
        private String id;
        private String question;
        private QuestionType type;
        private List<String> options; // For MULTIPLE_CHOICE
        private String correctAnswer; // For auto-grading (optional)
        private Integer points;
        
        public enum QuestionType {
            MULTIPLE_CHOICE,
            SHORT_ANSWER,
            LONG_ANSWER,
            CODE
        }
        
        // Constructors
        public Question() {}
        
        public Question(String id, String question, QuestionType type, Integer points) {
            this.id = id;
            this.question = question;
            this.type = type;
            this.points = points;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        
        public QuestionType getType() { return type; }
        public void setType(QuestionType type) { this.type = type; }
        
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
        
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
    }
    
    // Constructors
    public TechnicalTestStructure() {}
    
    public TechnicalTestStructure(String companyName, String jobTitle, String description) {
        this.companyName = companyName;
        this.jobTitle = jobTitle;
        this.description = description;
    }
    
    // Getters and Setters
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Integer timeLimit) { this.timeLimit = timeLimit; }
    
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
