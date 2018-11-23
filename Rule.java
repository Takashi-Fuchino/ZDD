public class Rule {
    char type;
    int number;
    String cond;

    public Rule (char t, int n, String c) {
	type = t;
	number = n;
	cond = c;
    }
    public Rule (Rule base) {
	type = base.type;
	number = base.number;
	cond = base.cond;
    }
    void setType(char t) { type = t; }
    void setNumber(int n) { number = n; }
    void setCond(String c) { cond = c; }
    char getType() { return type; }
    int getNumber() { return number; }
    String getCond() { return cond; }

    @Override
    public String toString() {
	return "r[" + number + ", " + type + "] = " + cond;
    }
}
