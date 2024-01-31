package com.fsre.quiz_app.models;

import com.google.firebase.database.PropertyName;

import java.util.Map;

public class GameQuestion {
    private String question;
    private Options options;

    // Default constructor required for Firebase deserialization
    public GameQuestion() {
        // Default constructor with no parameters
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    // Inner class representing the "options" object
    public static class Options {
        private Option a;
        private Option b;
        private Option c;
        private Option d;

        public Option getA() {
            return a;
        }

        public void setA(Option a) {
            this.a = a;
        }

        public Option getB() {
            return b;
        }

        public void setB(Option b) {
            this.b = b;
        }

        public Option getC() {
            return c;
        }

        public void setC(Option c) {
            this.c = c;
        }

        public Option getD() {
            return d;
        }

        public void setD(Option d) {
            this.d = d;
        }
    }

    // Inner class representing an option
    public static class Option {
        @PropertyName("isCorrect")
        private boolean isCorrect;

        private String text;
        @PropertyName("isCorrect")
        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }
        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    // Method to parse GameQuestion from Map (Firebase)
    public static GameQuestion fromMap(Map<String, Object> map) {
        GameQuestion question = new GameQuestion();
        question.setQuestion((String) map.get("question"));
        question.setOptions(parseOptions((Map<String, Map<String, Object>>) map.get("options")));
        return question;
    }

    // Method to parse Options from Map
    private static Options parseOptions(Map<String, Map<String, Object>> optionsMap) {
        Options options = new Options();
        options.setA(parseOption(optionsMap.get("a")));
        options.setB(parseOption(optionsMap.get("b")));
        options.setC(parseOption(optionsMap.get("c")));
        options.setD(parseOption(optionsMap.get("d")));
        return options;
    }

    // Method to parse Option from Map
    private static Option parseOption(Map<String, Object> optionMap) {
        Option option = new Option();
        option.setCorrect((Boolean) optionMap.get("isCorrect"));
        option.setText((String) optionMap.get("text"));
        return option;
    }
}
