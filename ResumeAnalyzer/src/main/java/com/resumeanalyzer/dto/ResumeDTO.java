package com.resumeanalyzer.dto;

import java.util.List;

public class ResumeDTO {
    private String name;
    private String email;
    private String phone;
    private List<String> skills;
    private List<String> education;
    private List<String> projects;
    private String summary;
    private String experience;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    
    public List<String> getEducation() { return education; }
    public void setEducation(List<String> education) { this.education = education; }
    
    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
}