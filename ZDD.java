import java.util.*;
import java.util.ArrayList;

public class ZDD {
    protected enum State { Occupied, Empty, Deleted };

    /* ZDDの本体 */
    class ZddElement {
	int _var;
	int _right;
	int _left;
	int _refCnt;
	State _state;
	ZddElement(final int var, final int right, final int left, final int refCnt, final State state) {
	    _var = _var;
	    _right = right;
	    _left = left;
	    _refCnt = refCnt;
	    _state = state;
	}
	ZddElement(ZddElement base) { 
	    _var = base._var;
	    _right = base._right;
	    _left = base._left;
	    _refCnt = base._refCnt;
	    _state = base._state;
	}
	public int getVar() { return _var; }
	public int getRight() { return _right; }
	public int getLeft(){ return _left; }
	public int getRefCnt(){ return _refCnt; }
	public State getState(){ return _state; }
	public void setVar(final int var) { _var = var; }
	public void setRight(final int right) { _right = right; }
	public void setLeft(final int left) { _left = left; }
	public void setState(final State state) { _state = state; }
	public void setRef_cnt(final int refCnt){ _refCnt = refCnt; }
	public void incRefCnt() { ++_refCnt; }
	public void decRefCnt() { --_refCnt; }

	public void setRefCntZero() { _refCnt = 0; }
	public void setStateEmpty() { _state = State.Empty; }

	@Override
	public String toString() {
	    return "var = " + _var + ", left = " + _left + ", right = " + _right + ", refCnt = " + _refCnt + ", state = " + _state;
	}
    }

    Integer NUMBEROFNODES;
    ZddElement[] zddTable = null;
    ArrayList<Integer> decRef = null;

    void decRefcounterRec(Integer k) {
    	if (0 == k || 1 == k)
	    return;
    	zddTable[k].decRefCnt();
    	if (0 == zddTable[k].getRefCnt()) {
    	    decRefcounterRec(zddTable[k].getLeft());
    	    decRefcounterRec(zddTable[k].getRight());
    	}
    }

    void decRefcounter() {
    	for (Integer i : decRef)
    	    decRefcounterRec(i);
    }

    public boolean eqZddTableEntry(int k, int Var, int Left, int Right) {
    	if (zddTable[k]._var != Var) 
	    return false;
    	if (zddTable[k]._left != Left) 
	    return false;
    	if (zddTable[k]._right != Right)
	    return false;
    	return true;
    }

    Integer hf(int var, int l, int r) { return ((100*var)+(10*l)+r) % NUMBEROFNODES; }

    public int getFoundNumber(int var, int left, int right) {
    	int i, k, found = -1;
    	State cstate;
    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
	    
    	    if (State.Empty == cstate || State.Deleted == cstate) {
    		if (found < 0)  /* there is an empty cell */
    		    found = k;
    	    } else {
    		if (eqZddTableEntry(k, var, left, right)) /* input node already exists */
    		    return k;
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
	
    	return found;
    }

    int insert(final int var, final int left, final int right) {
    	int found = getFoundNumber(var, left, right);

    	while (found < 0) {
    	    // System.out.println("Dictionary is full. NUM_OF_NODES = "+ NUM_OF_NODES);
    	    if (!decRef.isEmpty()) {
    		decRefcounter();
    		decRef = null;
		decRef = new ArrayList<Integer>();
    		setOpCacheTableEmpty();
    	    }
    
    	    found = getFoundNumber(var, left, right);
    	    if (found < 0) {
    		/* FIXME */
    		// System.out.println("Dictionary is truly full. NUM_OF_NODES = "+ NUM_OF_NODES);
    		increaseSlots();
    		found = getFoundNumber(var, left, right);
    		// System.out.println("found = " + found);
    	    }
    	}

    	zddTable[found].setState(State.Occupied);
    	zddTable[found].setVar(var);
    	zddTable[found].setLeft(left);
    	zddTable[found].setRight(right);

    	return found;
    }

    public void delete(final int var, final int left, final int right) {
    	int i, k;
    	State cstate;

    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqZddTableEntry(k, var, left, right)) { /* find the input node and delete it */
		    zddTable[k].setState(State.Deleted);
		    return; 
		} 
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
    	return; /* there is no input node */
    }

    public int member(final int var, final int left, final int right) {
    	int i, k;
    	State cstate;

    	k = i = hf(var, left, right);
    	do {
    	    cstate = zddTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqZddTableEntry(k, var, left, right)) /* find the input node */
		    return k;
    	    }
    	    k = (k+1) % NUMBEROFNODES;
    	    /* printf("collision occurs\n"); */
    	} while (State.Empty != cstate && k != i);
    	return -2; /* there is no input node */
    }

    public void incRefCounter(final int P) { zddTable[P].incRefCnt(); }

    public ZddElement[] ZddTableCopy(Integer n){
    	ZddElement Z[] = new ZddElement[n];
    	for (Integer i = 0 ; i < n ; ++i)
    	    Z[i] = new ZddElement(zddTable[i]);
    	return Z;
    }

    public void ZddTableEntryPrint() {
    	for (Integer i = 0 ; i < NUMBEROFNODES; ++i)
	    System.out.println("zddTable[" + i + "]: " + zddTable[i]);
    }

    public void increaseZddTableSlots() {
    	ZddElement W[] = new ZddElement[NUMBEROFNODES];
    	for(Integer i = 0; i < NUMBEROFNODES; ++i)
    	    W[i] = new ZddElement(zddTable[i]);
    	NUMBEROFNODES *= 2;
	zddTable = null;
	zddTable = new ZddElement[NUMBEROFNODES];
	for (Integer i = 0; i < NUMBEROFNODES/2; ++i)
	    zddTable[i] = new ZddElement(W[i]);
    	for (Integer i = NUMBEROFNODES/2; i < NUMBEROFNODES; ++i) {
    	    zddTable[i].setRefCntZero();
    	    zddTable[i].setStateEmpty();
    	}
    }

    public void increaseSlots() {
	increaseZddTableSlots();
	resetOpCacheTable();
	resetPathCacheTable();
    }

    /*************** 根節点から1終端節点へのパスを列挙するためのハッシュ ***************/

    class PathCacheElement {
	int _P;
	ArrayList<String> _L;
	State _state;
	PathCacheElement(final int P, final ArrayList<String> L, final State state) {
	    _P = P;
	    _L = new ArrayList<String>(L);
	    _state = state;
	}
	PathCacheElement(PathCacheElement base) {
	    _P = base._P;
	    _L = new ArrayList<String>(base._L);
	    _state = base._state;
	}
	public int getP(){ return _P; }
	public ArrayList<String> getL(){ return _L; }
	public State getState(){ return _state; }
	public void setP(final int P) { _P = P; }
	public void setL(final ArrayList<String> L) { _L = new ArrayList<String>(L); }
	public void setState(final State state) { _state = state; }
    	public void setStateEmpty() { _state = State.Empty; }
    }

    Integer PATHCACHESIZE;
    PathCacheElement[] pathCacheTable = null;

    void resetPathCacheTable() {
	PathCacheElement Q[] = new PathCacheElement[PATHCACHESIZE];
	for (Integer i = 0; i < PATHCACHESIZE; ++i)
	    Q[i] = new PathCacheElement(pathCacheTable[i]);
	pathCacheTable = null;
	PATHCACHESIZE *= 2;
	for (Integer i = 0; i < PATHCACHESIZE/2; ++i)
	    pathCacheTable[i] = new PathCacheElement(Q[i]);
	for (Integer i = PATHCACHESIZE/2; i < PATHCACHESIZE; ++i)
	    pathCacheTable[i].setStateEmpty();
    }

    /*************** オペレーションキャッシュ ***************/

    protected enum Op { Change, Union, Intersection, Setminus };

    class OpCacheElement {
    	Op _op ;
    	int _F;
    	int _G;
    	int _H;
    	State _state;
    	public OpCacheElement(Op op,int f,int g,int h, State state){
    	    _op = op;
    	    _F = f;
    	    _G = g;
    	    _H = h;
    	    _state = state;
    	}
	public Op getOp() { return _op; }
	public int getF() { return _F; }
	public int getG() { return _G; }
	public int getH() { return _H; }
	public State getState() { return _state; }
	public void setOp(Op op) { _op = op; }
	public void setF(int F) { _F = F; }
	public void setG(int G) { _G = G; }
	public void setH(int H) { _H = H; }
    	public void setState(State state) { _state = state; }
    	public void setStateEmpty() { _state = State.Empty; }
    }

    Integer OPCACHETABLESIZE;
    OpCacheElement[] OpCacheTable = null;

    public Integer hfOp(final Op o, final int f, final int g) {
    	int uo = 0;
    	if (Op.Change == o)
    	    uo = 1;
    	if (Op.Union == o)
    	    uo = 2;
    	if (Op.Intersection == o)
    	    uo = 3;
    	if (Op.Setminus == o)
    	    uo = 4;
    	return ((100*uo) + (10*f) + g) % OPCACHETABLESIZE;
    }

    public boolean eqOpTableEntry(final int k, final Op o, final int f, final int g) {
    	if (OpCacheTable[k].getOp() != o) 
	    return false;
    	if (OpCacheTable[k].getF() != f) 
	    return false;
    	if (OpCacheTable[k].getG() != g) 
	    return false;
    	return true;
    }

    public int memberOp(Op o, int f, int g) {
    	int i, k;
    	State cstate;

    	k = i = hfOp(o, f, g);
    	do {
    	    cstate = OpCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqOpTableEntry(k, o, f, g)) 
		    return OpCacheTable[k].getH(); /* find the input node */
    	    }
    	    k = (k+1) % OPCACHETABLESIZE;
    	    /* printf("collision occurs in op\n"); */
    	} while (State.Empty != cstate && k != i);
    	return -2; /* there is no input node */
    }

    public void deleteOp(final Op o, final int f, final int g) {
    	int i, k;
    	State cstate;

    	k = i = hfOp(o, f, g);
    	do {
    	    cstate = OpCacheTable[k].getState();
    	    if (State.Occupied == cstate) {
    		if (eqOpTableEntry(k, o, f, g)) { /* find the input node and delete it */
		    OpCacheTable[k].setState(State.Deleted); 
		    return; 
		} 
    	    }
    	    k = (k+1) % OPCACHETABLESIZE;
    	    /* printf("collision occurs in op\n"); */
    	} while (State.Empty != cstate && k != i);
    	return; /* there is no input node */
    }

    void setOpCacheTableEmpty() {
	for (Integer i = 0; i < OPCACHETABLESIZE; ++i)
	    OpCacheTable[i].setStateEmpty();
    }

    void resetOpCacheTable() {
	OpCacheTable = null;
	OPCACHETABLESIZE *= 2;
	OpCacheTable = new OpCacheElement[OPCACHETABLESIZE];
	for (Integer i = 0; i < OPCACHETABLESIZE; ++i)
	    OpCacheTable[i].setStateEmpty();
    }
    

    // public void inc_refcounter(int P) { ++ZDD_table[P].ref_cnt; }
    
    // /************************************** Operation Cache Table **************************************/

    // public void insert_op(String o, int f, int g, int h) {
    // 	int i, k, found = -1;
    // 	String cstate;

    // 	k = i = hf_op(o, f, g);
    // 	do {
    // 	    cstate = op_cache_table[k].state;
    // 	    if ("empty" == cstate || "deleted" == cstate) { if (found < 0) { found = k; } } /* there is an empty cell */
    // 	    else { if (eq_op_table_entry(k, o, f, g)) { return; } } /* input node already exists */
    // 	    k = (k+1) % op_cache_table_size;
    // 	    /* printf("collision occurs in op\n"); */
    // 	} while ("empty" != cstate && k != i);

    // 	if (-1 == found) { found = i; } /* update the tables by new entry */

    // 	op_cache_table[found].state = "occupied";
    // 	op_cache_table[found].op = o;
    // 	op_cache_table[found].F = f;
    // 	op_cache_table[found].G = g;
    // 	op_cache_table[found].H = h;
    // }
    // class count_cache_element{
    // 	int P;
    // 	int sum;
    // 	String state;
    // }
    // Integer count_cache_table_size;
    // count_cache_element count_cache_table[] ;
    // Integer hf_count(int P) {
    // 	return P % op_cache_table_size;
    // }

    // public int member_count(int P){
    // 	int i, k;
    // 	String cstate;

    // 	k = i = hf_count(P);
    // 	do {
    // 	    cstate = count_cache_table[k].state;
    // 	    if ("occupied" == cstate) {
    // 		if (count_cache_table[k].P == P) { return count_cache_table[k].sum; } /* find the input node */
    // 	    }
    // 	    k = (k+1) % count_cache_table_size;
    // 	} while ("empty" != cstate && k != i);
    // 	return -2; /* there is no input node */
    // }
    // class path_cache_element{
    // 	int P;
    // 	String L[];
    // 	String state;
    // }
    // Integer path_cache_table_size;
    // path_cache_element path_cache_table[];
    // String member_path[];
    // public int  hf_path(int P) {
    // 	return P % op_cache_table_size;
    // }
    // String[] member_path(int P) {
    // 	int i, k;
    // 	String cstate;

    // 	k = i = hf_path(P);
    // 	do {
    // 	    cstate = path_cache_table[k].state;
    // 	    if ("occupied" == cstate) {
    // 		if (path_cache_table[k].P == P)
    // 		    return path_cache_table[k].L; /* find the input node */
    // 	    }
    // 	    k = (k+1) % path_cache_table_size;
    // 	} while ("empty" != cstate && k != i);
    // 	return null; /* there is no input node */
    // }
    // public void delete_path(int P) {
    // 	int i, k;
    // 	String cstate;

    // 	k = i = hf_path(P);
    // 	do {
    // 	    cstate = path_cache_table[k].state;
    // 	    if ("occupied" == cstate) {
    // 		if (path_cache_table[k].P == P) { /* find the input node and delete it */
    // 		    count_cache_table[k].state = "deleted";
    // 		    return;
    // 		} 
    // 	    }
    // 	    k = (k+1) % count_cache_table_size;
    // 	} while ("empty" != cstate && k != i);
    // 	return; /* there is no input node */
    // }
    // public void insert_path(int P, String[] LS) {
    // 	int i, k, found = -1;
    // 	String cstate;

    // 	k = i = hf_path(P);
    // 	do {
    // 	    cstate = path_cache_table[k].state;
    // 	    if ("empty" == cstate || "deleted" == cstate) {
    // 		if (found < 0)
    // 		    found = k;  /* there is an empty cell */
    // 	    }
    // 	    else {
    // 		if (path_cache_table[k].P == P)
    // 		    return;  /* input node already exists */
    // 	    }
    // 	    k = (k+1) % path_cache_table_size;
    // 	} while ("empty" != cstate && k != i);

    // 	if (-1 == found) { found = i; } /* update the tables by new entry */

    // 	path_cache_table[found].state = "occupied";
    // 	path_cache_table[found].L = LS;
    // }
    // /******************************************* ZDD manipulating routine *******************************************/

    // public void zdd_init(Integer size, int n) {
	
    // 	dec_ref.clear();
	      
    // 	op_cache_table_size = size;
    // 	op_cache_table = null;

    // 	/* COUNT_CACHE_TABLE_SIZE = size; */
    // 	/* count_cache_table = (count_cache_element*)calloc(size, sizeof(count_cache_element)); */

    // 	path_cache_table_size = size;
    // 	path_cache_table = null;
  
    // 	NUM_OF_NODES = size;
    // 	ZDD_table = null;
    // 	ZDD_table[0].var = ZDD_table[1].var = n;
    // 	ZDD_table[0].left = ZDD_table[1].left = -1;
    // 	ZDD_table[0].right = ZDD_table[1].right = -1;
    // 	ZDD_table[0].state = ZDD_table[1].state = "occupied";

    // 	op_cache_table[0].state = op_cache_table[1].state = "empty";
    // 	/* count_cache_table[0].state = count_cache_table[1].state = empty; */
    // 	path_cache_table[0].state = path_cache_table[1].state = "empty";
    //    	for (Integer i = 2; i < size; ++i) {
    // 	    /* printf("i = %lld, size = %lld\n", i, size); */
    // 	    ZDD_table[i].state = "empty";
    // 	    op_cache_table[i].state = "empty";
    // 	    /* count_cache_table[i].state = empty; */
    // 	    path_cache_table[i].state = "empty";
    // 	}
    // 	System.out.println("initialize the ZDD table and the operation cache table\n");
    // }
    // public int topvar(int P) { return ZDD_table[P].var; }
    // public int get_left(int P) { return ZDD_table[P].left; }
    // public int get_right(int P) { return ZDD_table[P].right; }

    // public int getnode(int var, int left, int right) {
    // 	if (0 == right) { return left; }

    // 	int P = member(var, left, right);
    // 	/* printf("(mem) var = %d, left = %d, right = %d, P = %d\n", var, left, right, P); */
    // 	if (-2 != P)
    // 	    return P;

    // 	inc_refcounter(left);
    // 	inc_refcounter(right);
    // 	P = insert(var, left, right);
    // 	/* printf("(ins) var = %d, left = %d, right = %d, P = %d\n", var, left, right, P); */
    // 	return P;
    // }
    // public int change(int P, int v) {
    // 	if (v == ZDD_table[P].var)
    // 	    return getnode(v, ZDD_table[P].left, ZDD_table[P].right);
    // 	if (v < ZDD_table[P].var)
    // 	    return getnode(v, 0, P);
    // 	int R = member_op(_change, P, v);
    // 	if (-2 != R)
    // 	    return R;
    // 	R = getnode(ZDD_table[P].var, change(ZDD_table[P].left, v), change(ZDD_table[P].right, v));
    // 	insert_op(_change, P, v, R);
    // 	return R;
    // }
}
