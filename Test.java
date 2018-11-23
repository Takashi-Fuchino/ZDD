import java.util.ArrayList;

public class Test {    
    public static void main(String[] args) {
	ZDD zdd = new ZDD();
	zdd.zddInit(100, 4);

	Rule r[] = new Rule[7];
	r[0] = new Rule('P', 1, "*0*1");
	r[1] = new Rule('P', 2, "0000");
	r[2] = new Rule('P', 3, "0*00");
	r[3] = new Rule('D', 4, "0*1*");
	r[4] = new Rule('P', 5, "*1*1");
	r[5] = new Rule('P', 6, "***1");
	r[6] = new Rule('D', 7, "****");
	ArrayList<Rule> R = new ArrayList<Rule>();
	for (int i = 0; i < 7; ++i)
	    R.add(r[i]);
	System.out.println(R);
	int policy = mkZDDRepresentingPolicyOfRuleList(R, zdd);
	zdd.zddPrint(policy);
    }
    static int mkZDDRepresentingPolicyOfRuleList(ArrayList<Rule> R, ZDD zdd) {
	int P = 1;
	int Q;
	for (int i = R.size()-1; i > -1; --i) {
	    Q = makeZDDfor01mString(R.get(i).getCond(), zdd);
	    if ('D' == R.get(i).getType())
		P = zdd.setminus(P, Q);
	    else
		P = zdd.union(P, Q);
	}
	return P;
    }
    static int makeZDDfor01mString(String s, ZDD z) {
	int P = 1;
	for (int i = s.length()-1; i > -1; --i) {
	    if ('1' == s.charAt(i))
		P = z.getnode(i, 0, P);
	    if ('*' == s.charAt(i))
		P = z.getnode(i, P, P);
	}
	z.incRefCounter(P);
	return P;
    }
}
