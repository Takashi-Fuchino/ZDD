import java.util.*;
import java.util.ArrayList;

public class ZDD {
    // int Var;
    // int Right;
    // int Left;

    class ZDD_element {
	int var;
	int right;
	int left;
	int ref_cnt;
	String state;
	public ZDD_element(int Var,int Right, int Left, int Ref_cnt, String State) {
	    var = Var;
	    right = Right;
	    left = Left;
	    ref_cnt = Ref_cnt;
	    state = State;
	}
	public int getVar(){return var;}
	public int getRight(){return right;}
	public int getLeft(){return left;}
	public int getRef_cnt(){return ref_cnt;}
	public String getState(){return state;}
	public void addRight(int i){
	    right = i;
	}
	public void addLeft(int i){
	    left =i;
	}
	public void addRef_cnt(int Ref_cnt){
	    ref_cnt = Ref_cnt;
	}
	public void minusRef_cnt(){
	    ref_cnt--;
	}
	public void cpelement(ZDD_element base){
	    var = base.var;
	    left = base.left;
	    right = base.right;
	    ref_cnt = base.ref_cnt;
	    state = base.state;
	}
    }

    ArrayList<Integer> dec_ref = new ArrayList<Integer>();
    Integer NUM_OF_NODES;
    ZDD_element ZDD_table[] = {};
    class op_cache_element {
    	String op ;
    	int F;
    	int G;
    	int H;
    	String state;
	public op_cache_element(String OP,int f,int g,int h,String State){
	    op = OP;
	    F = f;
	    G = g;
	    H = h;
	    state = State;
	}
	public void addState(String State){
	    state = State;
	}
    }
    Integer op_cache_table_size ;
    op_cache_element op_cache_table[] = {};
    public void set_op_cache_table_empty(){
    	for(Integer i = 0;i < op_cache_table_size; i++)
    	    op_cache_table[i].addState("empty");
    }
    void reset_op_cache_table(){
        op_cache_table_size = op_cache_table_size*2;
    	op_cache_element O[] = new op_cache_element[op_cache_table_size] ;
    	for(Integer i = 0 ;i < op_cache_table_size; i++){
    	    O[i].addState("empty");
    	}
    	op_cache_table = O;
    }

    void dec_refcounter_rec(Integer k) {
    	if(0 == k || 1 == k)return;
    	ZDD_table[k].minusRef_cnt();
    	if(0 == ZDD_table[k].getRef_cnt()){
    	    dec_refcounter_rec(ZDD_table[k].getLeft());
    	    dec_refcounter_rec(ZDD_table[k].getRight());
    	}
    }
    ArrayList<Integer> unsigned_cell = new ArrayList<Integer>();
    void dec_refcounter() {
    	for(Integer i = 0; i < unsigned_cell.size(); i++)
    	    dec_refcounter_rec(unsigned_cell.get(i));
    	unsigned_cell.clear();
    }
    //int member(const int, const int, const int);
    // void delete(int, int, int);
    // int insert(int, int, int);
    // boolean eq_zdd_table_entry(int, int, int, int);
    Integer hf(int var, int l, int r) { return ((100*var)+(10*l)+r) % NUM_OF_NODES; }
    //Integer hf(int i, int j, int k);
    public boolean eq_zdd_table_entry(int k, int Var, int Left, int Right) {
    	if (ZDD_table[k].var != Var) { return false; }
    	if (ZDD_table[k].left != Left) { return false; }
    	if (ZDD_table[k].right != Right) { return false; }
    	return true;
    }
    public int get_found_number(int var, int left, int right) {
    	int i, k, found = -1;
    	String cstate;
    	k = i = hf(var, left, right);
    	do {
    	    cstate = ZDD_table[k].getState();
	    
    	    if ("empty" == cstate || "deleted" == cstate) {
    		if (found < 0)  /* there is an empty cell */
    		    found = k;
    	    } else {
    		if (eq_zdd_table_entry(k, var, left, right)) /* input node already exists */
    		    return k;
    	    }
    	    k = (k+1) % NUM_OF_NODES;
    	    /* printf("collision occurs\n"); */
    	} while ("empty" != cstate && k != i);
	
    	return found;
    }
    public ZDD_element[] ZDD_table_copy(Integer n){
    	ZDD_element Z[] = new ZDD_element[n];
    	for(Integer i = 0 ; i < n ; ++i){
	    Z[i].cpelement(ZDD_table[i]);
    	    // z[i].var = zdd_table[i].var;
    	    // Z[i].left = zdd_table[i].left;
    	    // Z[i].right = zdd_table[i].right;
    	    // Z[i].ref_cnt = zdd_table[i].ref_cnt;
    	    // Z[i].state = zdd_table[i].state;
    	}
    	return Z;
    }
    
    public void zdd_table_entry_print() {
    	for(Integer i = 0 ; i < NUM_OF_NODES;++i)
    	    System.out.println("zdd_table[%lld]: var = %d, left = %d, right = %d, ref_cnt = %u, state = %u\n"+" "+ i+" "+ ZDD_table[i].getVar()+" "+ ZDD_table[i].getLeft()+" "+ ZDD_table[i].getRight()+" "+ ZDD_table[i].getRef_cnt()+" "+ZDD_table[i].getState());
    }
    public void increase_zdd_table_slots() {
	ZDD_element w[] = new ZDD_element[ZDD_table.length];
	for(Integer i = 0; i < NUM_OF_NODES;++i){
	    w[i].cpelement(ZDD_table[i]);

	}
	NUM_OF_NODES = NUM_OF_NODES * 2;
	ZDD_table = null;
	for(Integer i = 0;i < NUM_OF_NODES/2;++i){
	    ZDD_table[i].cpelement(w[i]);
	}
	for(Integer i = NUM_OF_NODES/2;i < NUM_OF_NODES;++i){
	    ZDD_table[i].ref_cnt = 0;
	    ZDD_table[i].state = "empty";
	}
    }
    public void increase_slots() {
	increase_zdd_table_slots();
	reset_op_cache_table();
	System.out.println("Increase the slots\n");
	System.out.println("NUM_OF_NODES = %u, OP_CACHE_TABLE_SIZE = %u\n"+" "+ NUM_OF_NODES+" "+ op_cache_table_size);
    }
    int insert(int var, int left, int right) {
	int found = get_found_number(var, left, right);

	while (found < 0) {
	    System.out.println("Dictionary is full. NUM_OF_NODES = "+ NUM_OF_NODES);

	    if (0 != dec_ref.size()) {
		dec_refcounter();
		dec_ref.clear();
		set_op_cache_table_empty();
	    }
    
	    found = get_found_number(var, left, right);
	    if (found < 0) {
		/* FIXME */
		System.out.println("Dictionary is truly full. NUM_OF_NODES = "+ NUM_OF_NODES);
		increase_slots();
		found = get_found_number(var, left, right);
		System.out.println("found = " + found);
	    }
	}
	ZDD_table[found].state = "occupied";
	ZDD_table[found].var = var;
	ZDD_table[found].left = left;
	ZDD_table[found].right = right;
	return found;
    }
    public void delete(int var, int left, int right) {
	int i, k;
	String cstate;

	k = i = hf(var, left, right);
	do {
	    cstate = ZDD_table[k].state;
	    if ("occupied" == cstate) {
		if (eq_zdd_table_entry(k, var, left, right)) { ZDD_table[k].state = "deleted"; return; } /* find the input node and delete it */
	    }
	    k = (k+1) % NUM_OF_NODES;
	    /* printf("collision occurs\n"); */
	} while ("empty" != cstate && k != i);
	return; /* there is no input node */
    }

    public int member( int var,int left,int right) {
	int i, k;
	String cstate;

	k = i = hf(var, left, right);
	do {
	    cstate = ZDD_table[k].state;
	    if ("occupied" == cstate) {
		if (eq_zdd_table_entry(k, var, left, right)) { return k; } /* find the input node */
	    }
	    k = (k+1) % NUM_OF_NODES;
	    /* printf("collision occurs\n"); */
	} while ("empty" != cstate && k != i);
	return -2; /* there is no input node */
    }
    public void inc_refcounter(int P) { ++ZDD_table[P].ref_cnt; }
    
    /************************************** Operation Cache Table **************************************/
    public boolean eq_op_table_entry(int k, String o, int f, int g) {
	if (op_cache_table[k].op != o) { return false; }
	if (op_cache_table[k].F != f) { return false; }
	if (op_cache_table[k].G != g) { return false; }
	return true;
    }
    public Integer hf_op(String o, int f, int g) {
	int uo = 0;
	if ("change" == o)
	    uo = 1;
	if ("union" == o)
	    uo = 2;
	if ("intersection" == o)
	    uo = 3;
	if ("setminus" == o)
	    uo = 4;
	return ((100*uo) + (10*f) + g) % op_cache_table_size;
    }
    public int member_op(String o,int f, int g) {
	int i, k;
	String cstate;

	k = i = hf_op(o, f, g);
	do {
	    cstate = op_cache_table[k].state;
	    if ("occupied" == cstate) {
		if (eq_op_table_entry(k, o, f, g)) { return op_cache_table[k].H; } /* find the input node */
	    }
	    k = (k+1) % op_cache_table_size;
	    /* printf("collision occurs in op\n"); */
	} while ("empty" != cstate && k != i);
	return -2; /* there is no input node */
    }
    public void delete_op(String o, int f, int g) {
	int i, k;
	String cstate;

	k = i = hf_op(o, f, g);
	do {
	    cstate = op_cache_table[k].state;
	    if ("occupied" == cstate) {
		if (eq_op_table_entry(k, o, f, g)) { op_cache_table[k].state = "deleted"; return; } /* find the input node and delete it */
	    }
	    k = (k+1) % op_cache_table_size;
	    /* printf("collision occurs in op\n"); */
	} while ("empty" != cstate && k != i);
	return; /* there is no input node */
    }
    public void insert_op(String o, int f, int g, int h) {
	int i, k, found = -1;
	String cstate;

	k = i = hf_op(o, f, g);
	do {
	    cstate = op_cache_table[k].state;
	    if ("empty" == cstate || "deleted" == cstate) { if (found < 0) { found = k; } } /* there is an empty cell */
	    else { if (eq_op_table_entry(k, o, f, g)) { return; } } /* input node already exists */
	    k = (k+1) % op_cache_table_size;
	    /* printf("collision occurs in op\n"); */
	} while ("empty" != cstate && k != i);

	if (-1 == found) { found = i; } /* update the tables by new entry */

	op_cache_table[found].state = "occupied";
	op_cache_table[found].op = o;
	op_cache_table[found].F = f;
	op_cache_table[found].G = g;
	op_cache_table[found].H = h;
    }
    class count_cache_element{
	int P;
	int sum;
	String state;
    }
    Integer count_cache_table_size;
    count_cache_element count_cache_table[] ;
    Integer hf_count(int P) {
	return P % op_cache_table_size;
    }

    public int member_count(int P){
	int i, k;
	String cstate;

	k = i = hf_count(P);
	do {
	    cstate = count_cache_table[k].state;
	    if ("occupied" == cstate) {
		if (count_cache_table[k].P == P) { return count_cache_table[k].sum; } /* find the input node */
	    }
	    k = (k+1) % count_cache_table_size;
	} while ("empty" != cstate && k != i);
	return -2; /* there is no input node */
    }
    class path_cache_element{
	int P;
	String L[];
	String state;
    }
    Integer path_cache_table_size;
    path_cache_element path_cache_table[];
    String member_path[];
    public int  hf_path(int P) {
	return P % op_cache_table_size;
    }
    String[] member_path(int P) {
	int i, k;
	String cstate;

	k = i = hf_path(P);
	do {
	    cstate = path_cache_table[k].state;
	    if ("occupied" == cstate) {
		if (path_cache_table[k].P == P)
		    return path_cache_table[k].L; /* find the input node */
	    }
	    k = (k+1) % path_cache_table_size;
	} while ("empty" != cstate && k != i);
	return null; /* there is no input node */
    }
    public void delete_path(int P) {
	int i, k;
	String cstate;

	k = i = hf_path(P);
	do {
	    cstate = path_cache_table[k].state;
	    if ("occupied" == cstate) {
		if (path_cache_table[k].P == P) { /* find the input node and delete it */
		    count_cache_table[k].state = "deleted";
		    return;
		} 
	    }
	    k = (k+1) % count_cache_table_size;
	} while ("empty" != cstate && k != i);
	return; /* there is no input node */
    }
    public void insert_path(int P, String[] LS) {
	int i, k, found = -1;
	String cstate;

	k = i = hf_path(P);
	do {
	    cstate = path_cache_table[k].state;
	    if ("empty" == cstate || "deleted" == cstate) {
		if (found < 0)
		    found = k;  /* there is an empty cell */
	    }
	    else {
		if (path_cache_table[k].P == P)
		    return;  /* input node already exists */
	    }
	    k = (k+1) % path_cache_table_size;
	} while ("empty" != cstate && k != i);

	if (-1 == found) { found = i; } /* update the tables by new entry */

	path_cache_table[found].state = "occupied";
	path_cache_table[found].L = LS;
    }
    /******************************************* ZDD manipulating routine *******************************************/

    public void zdd_init(Integer size, int n) {
	
	dec_ref.clear();
	      
	op_cache_table_size = size;
	op_cache_table = null;

	/* COUNT_CACHE_TABLE_SIZE = size; */
	/* count_cache_table = (count_cache_element*)calloc(size, sizeof(count_cache_element)); */

	path_cache_table_size = size;
	path_cache_table = null;
  
	NUM_OF_NODES = size;
	ZDD_table = null;
	ZDD_table[0].var = ZDD_table[1].var = n;
	ZDD_table[0].left = ZDD_table[1].left = -1;
	ZDD_table[0].right = ZDD_table[1].right = -1;
	ZDD_table[0].state = ZDD_table[1].state = "occupied";

	op_cache_table[0].state = op_cache_table[1].state = "empty";
	/* count_cache_table[0].state = count_cache_table[1].state = empty; */
	path_cache_table[0].state = path_cache_table[1].state = "empty";
       	for (Integer i = 2; i < size; ++i) {
	    /* printf("i = %lld, size = %lld\n", i, size); */
	    ZDD_table[i].state = "empty";
	    op_cache_table[i].state = "empty";
	    /* count_cache_table[i].state = empty; */
	    path_cache_table[i].state = "empty";
	}
	System.out.println("initialize the ZDD table and the operation cache table\n");
    }
    public int topvar(int P) { return ZDD_table[P].var; }
    public int get_left(int P) { return ZDD_table[P].left; }
    public int get_right(int P) { return ZDD_table[P].right; }

    public int getnode(int var, int left, int right) {
	if (0 == right) { return left; }

	int P = member(var, left, right);
	/* printf("(mem) var = %d, left = %d, right = %d, P = %d\n", var, left, right, P); */
	if (-2 != P)
	    return P;

	inc_refcounter(left);
	inc_refcounter(right);
	P = insert(var, left, right);
	/* printf("(ins) var = %d, left = %d, right = %d, P = %d\n", var, left, right, P); */
	return P;
    }
    public int change(int P, int v) {
	if (v == ZDD_table[P].var)
	    return getnode(v, ZDD_table[P].left, ZDD_table[P].right);
	if (v < ZDD_table[P].var)
	    return getnode(v, 0, P);
	int R = member_op(change, P, v);
	if (-2 != R)
	    return R;
	R = getnode(ZDD_table[P].var, change(ZDD_table[P].left, v), change(ZDD_table[P].right, v));
	insert_op(change, P, v, R);
	return R;
    }
}
