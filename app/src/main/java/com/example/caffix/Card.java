package com.example.caffix;

public class Card {
    private String CAFE;
    private String CATEGORY;
    private String MENU;
    private String SIZE;
    private String OPTION;
    private int CAFFEINE;

    // 생성자, getter 및 setter 메서드 등을 추가하여 데이터베이스의 구조와 일치하도록 수정하세요.

    public Card() {
        // 기본 생성자 필요 (Firebase에서 객체를 직접 생성할 때 필요)
    }

    public Card(String cafe, String category, String menu, String size, String option, int caffeine) {
        this.CAFE = cafe;
        this.CATEGORY = category;
        this.MENU = menu;
        this.SIZE = size;
        this.OPTION = option;
        this.CAFFEINE = caffeine;
    }

    public String getCafe() {
        return CAFE;
    }

    public void setCafe(String cafe) {
        this.CAFE = cafe;
    }

    public String getCategory() {
        return CATEGORY;
    }

    public void setCategory(String category) {
        this.CATEGORY = category;
    }

    public String getMenu() {
        return MENU;
    }

    public void setMenu(String menu) {
        this.MENU = menu;
    }

    public String getSize() {
        return SIZE;
    }

    public void setSize(String size) {
        this.SIZE = size;
    }

    public String getOption() {
        return OPTION;
    }

    public void setOption(String option) {
        this.OPTION = option;
    }

    public int getCaffeine() {
        return CAFFEINE;
    }

    public void setCaffeine(int caffeine) {
        this.CAFFEINE = caffeine;
    }
}

