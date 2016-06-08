public class Token {
    private String name;
    private String value;
    private int priority;

    public Token(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override

    public String toString() {
        return "Token(" +
                "name = '" + name + '\'' +
                ", value = '" + value + '\'' +
                ')';
    }
}
